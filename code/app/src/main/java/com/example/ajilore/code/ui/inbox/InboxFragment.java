package com.example.ajilore.code.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.settings.SettingsFragment;
import com.example.ajilore.code.utils.AdminAuthManager; // Import AdminAuthManager
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment {

    private RecyclerView recyclerNotifications;
    private NotificationAdapter adapter;

    private List<NotificationModel> notificationList;
    private List<NotificationModel> archivedList;

    private FirebaseFirestore db;
    private FirebaseAuth auth; // Still needed for potential anonymous sign-in for Firestore rules

    private MaterialButton btnMarkAllRead;
    private MaterialButton btnFilterUnread;
    private MaterialButton btnViewArchived;

    private boolean showOnlyUnread = false;
    private boolean showingArchived = false;

    private NotificationAdapter.OnNotificationActionListener listener;

    public InboxFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        recyclerNotifications = view.findViewById(R.id.recyclerNotifications);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        btnFilterUnread = view.findViewById(R.id.btnFilterUnread);
        btnViewArchived = view.findViewById(R.id.btnViewArchived);

        // Settings button navigation
        ImageButton btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> openNotificationSettings());
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance(); // Initialize for potential use
        notificationList = new ArrayList<>();
        archivedList = new ArrayList<>();

        // Listener for RecyclerView actions
        listener = new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onDismiss(NotificationModel notification) {
                // Get the actual device ID
                String userId = AdminAuthManager.getDeviceId(requireContext()); // Use device ID
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(getContext(), "Cannot archive: Device ID not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                DocumentReference ref = db.collection("org_events")
                        .document(notification.getEventId())
                        .collection("waiting_list")
                        .document(userId) // Use actual device ID
                        .collection("inbox")
                        .document(notification.getFirestoreDocId());

                ref.update("archived", true).addOnSuccessListener(aVoid -> {
                    notificationList.remove(notification);
                    archivedList.add(notification);
                    adapter.updateList(getCurrentList(), showingArchived);
                    Toast.makeText(getContext(), "Notification archived", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to archive: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onAction(NotificationModel notification) {
                Toast.makeText(getContext(),
                        "Open details for: " + notification.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        adapter = new NotificationAdapter(getContext(), getCurrentList(), listener, showingArchived);
        recyclerNotifications.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(v -> markAllRead());
        btnFilterUnread.setOnClickListener(v -> toggleUnreadFilter());
        btnViewArchived.setOnClickListener(v -> toggleArchiveView());

        // Still sign in anonymously if needed for Firestore rules
        signInAnonymouslyIfNeeded();
        return view;
    }

    // Helper method to get device ID (replace with your actual implementation from MainActivity)
    private String getDeviceId() {
        // Use the same method as in MainActivity
        String deviceId = AdminAuthManager.getDeviceId(requireContext());
        return deviceId != null ? deviceId : "unknown_device"; // Fallback
    }


    /** ------------------- Helper Methods ------------------- **/
    // Navigate to SettingsFragment
    private void openNotificationSettings() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    // Anonymous sign-in if needed (for Firestore security rules, not for user identification)
    private void signInAnonymouslyIfNeeded() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            auth.signInAnonymously().addOnSuccessListener(result -> {
                        // Ensure device ID user profile exists after sign-in if needed
                        // This part might be handled by the login flow or a separate initialization
                        loadUserNotifications();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Auth failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            loadUserNotifications();
        }
    }

    // ------------------- Load Notifications -------------------
    private void loadUserNotifications() {
        // Use the actual device ID instead of "demoUser"
        String userId = getDeviceId(); // Use device ID here
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Cannot load notifications: Device ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("org_events").get().addOnSuccessListener(eventSnapshots -> {
            for (DocumentSnapshot eventDoc : eventSnapshots) {
                String eventId = eventDoc.getId();

                eventDoc.getReference()
                        .collection("waiting_list")
                        .document(userId) // Use actual device ID
                        .collection("inbox")
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .addSnapshotListener((snapshots, e) -> {
                            if (e != null || snapshots == null) return;

                            // Clear lists before adding new data to prevent duplicates or stale data
                            notificationList.clear();
                            archivedList.clear();

                            for (DocumentChange change : snapshots.getDocumentChanges()) {
                                DocumentSnapshot inboxDoc = change.getDocument();

                                String message = inboxDoc.getString("message");
                                String type = inboxDoc.getString("type") != null ? inboxDoc.getString("type") : "general";
                                boolean read = inboxDoc.getBoolean("read") != null && inboxDoc.getBoolean("read");
                                boolean archived = inboxDoc.getBoolean("archived") != null && inboxDoc.getBoolean("archived");
                                String docId = inboxDoc.getId();

                                if (message != null) {
                                    NotificationModel notification = new NotificationModel(
                                            eventId,
                                            docId,
                                            message,
                                            "", "", read,
                                            getString(R.string.see_details),
                                            type
                                    );

                                    if (archived) {
                                        archivedList.add(notification);
                                    } else {
                                        notificationList.add(notification);
                                    }
                                }
                            }
                            // Update UI after processing all changes
                            adapter.updateList(getCurrentList(), showingArchived);
                        });
            }
        });
    }

    // ------------------- Button Actions -------------------
    private void markAllRead() {
        for (NotificationModel n : notificationList) n.setRead(true);
        adapter.updateList(getCurrentList(), showingArchived);
        Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    private void toggleUnreadFilter() {
        showOnlyUnread = !showOnlyUnread;
        adapter.updateList(getCurrentList(), showingArchived);
        btnFilterUnread.setText(showOnlyUnread ? R.string.show_all : R.string.show_unread);
    }

    private void toggleArchiveView() {
        showingArchived = !showingArchived;
        adapter.updateList(getCurrentList(), showingArchived);
        btnViewArchived.setText(showingArchived ? R.string.show_inbox : R.string.view_archived);
    }

    private List<NotificationModel> getCurrentList() {
        List<NotificationModel> sourceList = showingArchived ? archivedList : notificationList;
        List<NotificationModel> filteredList = new ArrayList<>();
        for (NotificationModel n : sourceList) {
            if (showOnlyUnread && n.isRead()) continue;
            filteredList.add(n);
        }
        return filteredList;
    }
}