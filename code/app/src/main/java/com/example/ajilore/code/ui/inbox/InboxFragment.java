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

    private List<NotificationModel> notificationList = new ArrayList<>();
    private List<NotificationModel> archivedList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private MaterialButton btnMarkAllRead, btnFilterUnread, btnViewArchived;

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

        ImageButton btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> openNotificationSettings());
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        listener = new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onDismiss(NotificationModel notification) {
                archiveNotification(notification);
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

        signInAnonymouslyIfNeeded();
        return view;
    }

    // ---------------- Navigation ----------------

    private void openNotificationSettings() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    // ---------------- Firebase Sign-in ----------------

    private void signInAnonymouslyIfNeeded() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            auth.signInAnonymously().addOnSuccessListener(result -> loadUserNotifications())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Auth failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            loadUserNotifications();
        }
    }

    // ---------------- Load Notifications ----------------

    private void loadUserNotifications() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // 🔥 FIX: Use actual user ID instead of hardcoded "demoUser"
        String userId = user.getUid(); // Use real user ID instead of "demoUser"

        db.collection("org_events").get().addOnSuccessListener(eventSnapshots -> {
            for (DocumentSnapshot eventDoc : eventSnapshots) {
                String eventId = eventDoc.getId();

                eventDoc.getReference()
                        .collection("waiting_list")
                        .document(userId) // 🔥 Now uses real user ID
                        .collection("inbox")
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .addSnapshotListener((snapshots, e) -> {
                            if (e != null || snapshots == null) return;

                            for (DocumentChange change : snapshots.getDocumentChanges()) {
                                DocumentSnapshot inboxDoc = change.getDocument();

                                String message = inboxDoc.getString("message");
                                String type = inboxDoc.getString("type") != null ? inboxDoc.getString("type") : "general";
                                boolean read = inboxDoc.getBoolean("read") != null && inboxDoc.getBoolean("read");
                                boolean archived = inboxDoc.getBoolean("archived") != null && inboxDoc.getBoolean("archived");
                                String docId = inboxDoc.getId();

                                if (message != null) {
                                    NotificationModel notification = new NotificationModel(
                                            eventId, docId, message,
                                            "", "", read,
                                            getString(R.string.see_details),
                                            type
                                    );

                                    if (archived) {
                                        if (!archivedList.contains(notification)) archivedList.add(notification);
                                    } else {
                                        if (!notificationList.contains(notification)) notificationList.add(notification);
                                    }
                                }
                            }
                            refreshDisplay();
                        });
            }
        });
    }

    // ---------------- Button Functions ----------------

    private void markAllRead() {
        for (NotificationModel n : notificationList) n.setRead(true);
        // Later we'll update Firebase here
        refreshDisplay();
        Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    private void toggleUnreadFilter() {
        showOnlyUnread = !showOnlyUnread;
        refreshDisplay();
        btnFilterUnread.setText(showOnlyUnread ? R.string.show_all : R.string.show_unread);
    }

    private void toggleArchiveView() {
        showingArchived = !showingArchived;
        refreshDisplay();
        btnViewArchived.setText(showingArchived ? R.string.show_inbox : R.string.view_archived);
    }

    // ---------------- Helper Lists ----------------

    private List<NotificationModel> getCurrentList() {
        List<NotificationModel> source = showingArchived ? archivedList : notificationList;
        List<NotificationModel> filtered = new ArrayList<>();
        for (NotificationModel n : source) {
            if (showOnlyUnread && n.isRead()) continue;
            filtered.add(n);
        }
        return filtered;
    }

    private void refreshDisplay() {
        adapter.updateList(getCurrentList(), showingArchived);
    }

    // ---------------- Archive (Dismiss) ----------------

    private void archiveNotification(NotificationModel notification) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid(); // Use real user ID

        DocumentReference ref = db.collection("org_events")
                .document(notification.getEventId())
                .collection("waiting_list")
                .document(userId) // 🔥 Now uses real user ID
                .collection("inbox")
                .document(notification.getFirestoreDocId());

        ref.update("archived", true).addOnSuccessListener(aVoid -> {
            notificationList.remove(notification);
            archivedList.add(notification);
            refreshDisplay();
            Toast.makeText(getContext(), "Notification archived", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}