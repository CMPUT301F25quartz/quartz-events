package com.example.ajilore.code.ui.inbox;

import android.os.Bundle;
import android.util.Log; // Import Log
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
                String userId = AdminAuthManager.getDeviceId(requireContext()); // Get current device ID
                if (userId == null || userId.isEmpty()) {
                    // --- CRITICAL FIX: Check context before showing toast ---
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Cannot archive: Device ID not available", Toast.LENGTH_SHORT).show();
                    }
                    // --- END CRITICAL FIX ---
                    return;
                }

                // Archive using the new structure: /users/{userId}/registrations/{eventId}/inbox/{notificationId}
                DocumentReference ref = db.collection("users")
                        .document(userId)
                        .collection("registrations")
                        .document(notification.getEventId()) // Use the event ID from the notification
                        .collection("inbox")
                        .document(notification.getFirestoreDocId());

                ref.update("archived", true).addOnSuccessListener(aVoid -> {
                    notificationList.remove(notification);
                    archivedList.add(notification);
                    // --- CRITICAL FIX: Check context before updating UI ---
                    if (isAdded() && getContext() != null) {
                        adapter.updateList(getCurrentList(), showingArchived);
                        Toast.makeText(getContext(), "Notification archived", Toast.LENGTH_SHORT).show();
                    }
                    // --- END CRITICAL FIX ---
                }).addOnFailureListener(e -> {
                    // --- CRITICAL FIX: Check context before showing toast ---
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Failed to archive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    // --- END CRITICAL FIX ---
                });
            }

            @Override
            public void onAction(NotificationModel notification) {
                // --- CRITICAL FIX: Check context before showing toast ---
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(),
                            "Open details for: " + notification.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                // --- END CRITICAL FIX ---
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


    /** ------------------- Helper Methods ------------------- **/
    // Navigate to SettingsFragment
    private void openNotificationSettings() {
        // --- CRITICAL FIX: Check if fragment is added before navigating ---
        if (isAdded()) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
        }
        // --- END CRITICAL FIX ---
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
                    .addOnFailureListener(e -> {
                        // --- CRITICAL FIX: Check context before showing toast ---
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Auth failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        // --- END CRITICAL FIX ---
                    });
        } else {
            loadUserNotifications();
        }
    }

    // ------------------- Preference Helper -------------------
    /**
     * Checks if the user has opted in for notifications.
     * @param callback returns true if notifications are enabled, false if opted out
     */
    private void checkNotificationPreference(String userId, PreferenceCallback callback) { // Accept userId
        db.collection("users")
                .document(userId)
                .collection("preferences")
                .document("notifications")
                .get()
                .addOnSuccessListener(doc -> {
                    boolean enabled = true; // default
                    if (doc.exists()) {
                        Boolean pref = doc.getBoolean("enabled");
                        enabled = pref != null ? pref : true;
                    }
                    callback.onResult(enabled);
                })
                .addOnFailureListener(e -> callback.onResult(true));
    }
    private interface PreferenceCallback {
        void onResult(boolean enabled);
    }


    // ------------------- Load Notifications -------------------
    private void loadUserNotifications() {
        // Use the actual device ID from AdminAuthManager to identify the current user
        String userId = AdminAuthManager.getDeviceId(requireContext()); // Get current device ID
        if (userId == null || userId.isEmpty()) {
            // --- CRITICAL FIX: Check context before showing toast ---
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Cannot load notifications: Device ID unavailable", Toast.LENGTH_SHORT).show();
            }
            // --- END CRITICAL FIX ---
            return;
        }

        Log.d("InboxFragment", "Loading notifications for user ID: " + userId); // Log for debugging

        // Listen to the 'registrations' collection for this user
        db.collection("users").document(userId).collection("registrations").addSnapshotListener((registrationsSnapshot, e) -> {
            if (e != null) {
                Log.e("InboxFragment", "Error listening to registrations: ", e);
                return;
            }

            // --- CRITICAL FIX: Check if Fragment is still attached ---
            if (!isAdded() || getContext() == null) {
                // Fragment is detached, stop processing this listener callback
                Log.d("InboxFragment", "Fragment detached, stopping registrations listener callback");
                return;
            }
            // --- END CRITICAL FIX ---

            Log.d("InboxFragment", "Received registrations snapshot update, changes: " + (registrationsSnapshot != null ? registrationsSnapshot.getDocumentChanges().size() : 0)); // Log for debugging

            // Clear lists before adding new data to prevent duplicates or stale data
            notificationList.clear();
            archivedList.clear();

            if (registrationsSnapshot != null) {
                for (DocumentChange change : registrationsSnapshot.getDocumentChanges()) {
                    String eventId = change.getDocument().getId();
                    Log.d("InboxFragment", "Processing registration for event: " + eventId); // Log for debugging

                    // For each registration, listen to its 'inbox' subcollection
                    change.getDocument().getReference()
                            .collection("inbox")
                            .orderBy("createdAt", Query.Direction.DESCENDING) // Order by timestamp
                            .addSnapshotListener((inboxSnapshot, inboxError) -> {
                                if (inboxError != null) {
                                    Log.e("InboxFragment", "Error listening to inbox for event " + eventId + ": ", inboxError);
                                    return;
                                }

                                // --- CRITICAL FIX: Check if Fragment is still attached inside nested listener ---
                                if (!isAdded() || getContext() == null) {
                                    // Fragment is detached, stop processing this nested listener callback
                                    Log.d("InboxFragment", "Fragment detached, stopping nested inbox listener callback for event: " + eventId);
                                    return;
                                }
                                // --- END CRITICAL FIX ---

                                Log.d("InboxFragment", "Received inbox snapshot update for event: " + eventId + ", changes: " + (inboxSnapshot != null ? inboxSnapshot.getDocumentChanges().size() : 0)); // Log for debugging

                                for (DocumentChange inboxChange : inboxSnapshot.getDocumentChanges()) {
                                    DocumentSnapshot inboxDoc = inboxChange.getDocument();

                                    String message = inboxDoc.getString("message");
                                    String type = inboxDoc.getString("type") != null ? inboxDoc.getString("type") : "general";
                                    boolean read = inboxDoc.getBoolean("read") != null && inboxDoc.getBoolean("read");
                                    boolean archived = inboxDoc.getBoolean("archived") != null && inboxDoc.getBoolean("archived");
                                    String docId = inboxDoc.getId();

                                    if (message != null) {
                                        // --- CRITICAL FIX: Use context check for getString ---
                                        String actionText = "See Details"; // Default value
                                        if (getContext() != null) {
                                            try {
                                                actionText = getString(R.string.see_details); // Only call if context is available
                                            } catch (IllegalStateException ise) {
                                                // Context might be gone, use default
                                                Log.w("InboxFragment", "Context unavailable for getString, using default action text", ise);
                                            }
                                        }
                                        // --- END CRITICAL FIX ---

                                        NotificationModel notification = new NotificationModel(
                                                eventId, // Use the event ID from the registration document path
                                                docId,   // Use the notification document ID
                                                message,
                                                "", "", read,
                                                actionText, // Use the retrieved or default text
                                                type
                                        );

                                        if (archived) {
                                            archivedList.add(notification);
                                        } else {
                                            notificationList.add(notification);
                                        }
                                    }
                                }
                                // After processing all inbox changes for this event, update the UI
                                // Check preferences and update UI
                                checkNotificationPreference(userId, enabled -> {
                                    if (!enabled) {
                                        // User opted out: clear notifications
                                        notificationList.clear();
                                        archivedList.clear();
                                        // --- CRITICAL FIX: Check context before updating UI ---
                                        if (isAdded() && getContext() != null) {
                                            adapter.updateList(getCurrentList(), showingArchived);
                                            Toast.makeText(getContext(),
                                                    "Notifications are disabled in Settings",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        // --- END CRITICAL FIX ---
                                        Log.d("InboxFragment", "Notifications disabled, cleared list for: " + userId); // Log for debugging
                                        return;
                                    }
                                    // User enabled notifications: update UI normally
                                    // --- CRITICAL FIX: Check context before updating UI ---
                                    if (isAdded() && getContext() != null) {
                                        adapter.updateList(getCurrentList(), showingArchived);
                                        Log.d("InboxFragment", "Notifications enabled, updated list for: " + userId); // Log for debugging
                                    }
                                    // --- END CRITICAL FIX ---
                                });
                            });
                }
            }
        });
    }


    // ------------------- Button Actions -------------------
    private void markAllRead() {
        for (NotificationModel n : notificationList) n.setRead(true);
        // --- CRITICAL FIX: Check context before updating UI ---
        if (isAdded() && getContext() != null) {
            adapter.updateList(getCurrentList(), showingArchived);
            Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
        }
        // --- END CRITICAL FIX ---
    }

    private void toggleUnreadFilter() {
        showOnlyUnread = !showOnlyUnread;
        // --- CRITICAL FIX: Check context before updating UI ---
        if (isAdded() && getContext() != null) {
            adapter.updateList(getCurrentList(), showingArchived);
            btnFilterUnread.setText(showOnlyUnread ? R.string.show_all : R.string.show_unread);
        }
        // --- END CRITICAL FIX ---
    }

    private void toggleArchiveView() {
        showingArchived = !showingArchived;
        // --- CRITICAL FIX: Check context before updating UI ---
        if (isAdded() && getContext() != null) {
            adapter.updateList(getCurrentList(), showingArchived);
            btnViewArchived.setText(showingArchived ? R.string.show_inbox : R.string.view_archived);
        }
        // --- END CRITICAL FIX ---
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