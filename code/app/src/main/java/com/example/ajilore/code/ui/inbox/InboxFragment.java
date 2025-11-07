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

/**
 * Fragment that displays a user's inbox notifications.
 * Supports marking notifications as read, filtering unread notifications,
 * viewing archived notifications, and navigating to notification settings.
 */
public class InboxFragment extends Fragment {

    private RecyclerView recyclerNotifications;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;
    private List<NotificationModel> archivedList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private MaterialButton btnMarkAllRead;
    private MaterialButton btnFilterUnread;
    private MaterialButton btnViewArchived;

    private boolean showOnlyUnread = false;
    private boolean showingArchived = false;

    private NotificationAdapter.OnNotificationActionListener listener;

    public InboxFragment() {}

    /**
     * Inflates the fragment layout and initializes all views, buttons, and Firestore.
     * Also handles anonymous sign-in if no user is currently signed in.
     *
     * @param inflater LayoutInflater
     * @param container Optional parent view
     * @param savedInstanceState Saved state
     * @return The root view for the fragment
     */
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

        // Settings button navigates to SettingsFragment
        ImageButton btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> openNotificationSettings());
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        notificationList = new ArrayList<>();
        archivedList = new ArrayList<>();

        // Listener for RecyclerView notification actions
        listener = new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onDismiss(NotificationModel notification) {
                DocumentReference ref = db.collection("org_events")
                        .document(notification.getEventId())
                        .collection("waiting_list")
                        .document("demoUser")
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

        signInAnonymouslyIfNeeded();

        return view;
    }

    // ------------------- Helper Methods -------------------

    /** Navigate to the SettingsFragment. */
    private void openNotificationSettings() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Signs in the user anonymously if not already signed in.
     * After sign-in, loads notifications for the user.
     */
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

    // ------------------- Preference Helper -------------------

    /**
     * Checks whether notifications are enabled for the user.
     *
     * @param callback Callback returning true if notifications are enabled, false otherwise
     */
    private void checkNotificationPreference(PreferenceCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(true); // default to enabled
            return;
        }

        String userId = user.getUid();

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

    /**
     * Loads all notifications for the current user from all events in Firestore.
     * Handles filtering by read/unread and archives.
     */
    private void loadUserNotifications() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = "demoUser"; // using demoUser for now

        db.collection("org_events").get().addOnSuccessListener(eventSnapshots -> {
            for (DocumentSnapshot eventDoc : eventSnapshots) {
                String eventId = eventDoc.getId();

                eventDoc.getReference()
                        .collection("waiting_list")
                        .document(userId)
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
                                            eventId,
                                            docId,
                                            message,
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

                            // Update UI based on notification preference
                            checkNotificationPreference(enabled -> {
                                if (!enabled) {
                                    notificationList.clear();
                                    archivedList.clear();
                                    adapter.updateList(getCurrentList(), showingArchived);
                                    Toast.makeText(getContext(),
                                            "Notifications are disabled in Settings",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                adapter.updateList(getCurrentList(), showingArchived);
                            });
                        });
            }
        });
    }

    // ------------------- Button Actions -------------------

    /** Marks all notifications in the inbox as read. */
    private void markAllRead() {
        for (NotificationModel n : notificationList) n.setRead(true);
        adapter.updateList(getCurrentList(), showingArchived);
        Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    /** Toggles filter between showing only unread and all notifications. */
    private void toggleUnreadFilter() {
        showOnlyUnread = !showOnlyUnread;
        adapter.updateList(getCurrentList(), showingArchived);
        btnFilterUnread.setText(showOnlyUnread ? R.string.show_all : R.string.show_unread);
    }

    /** Toggles between inbox and archived notifications. */
    private void toggleArchiveView() {
        showingArchived = !showingArchived;
        adapter.updateList(getCurrentList(), showingArchived);
        btnViewArchived.setText(showingArchived ? R.string.show_inbox : R.string.view_archived);
    }

    /** Returns the current list of notifications, filtered by unread/archive settings. */
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
