package com.example.ajilore.code.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import android.provider.Settings;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



/**
 * Fragment representing an inbox screen.
 * Use the  factory method to
 * create an instance with specific parameters.
 */


public class InboxFragment extends Fragment {

    private String userProfileUrl;

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
    private String userId;

    private NotificationAdapter.OnNotificationActionListener listener;

    /**
     * Default public constructor for {@link InboxFragment}.
     * Required by the Android system for fragment instantiation.
     */
    public InboxFragment() {
        // Required empty public constructor
    }


    /**
     * Factory method to create a new instance of {@link InboxFragment}
     * using the provided parameters.
     *
     * @param inflater The first initialization parameter.
     * @param savedInstanceState The second initialization parameter.
     * @return A new instance of fragment {@link InboxFragment}.
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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Use same ID as EventDetailsFragment
        userId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Could not get device ID", Toast.LENGTH_SHORT).show();
        }

        notificationList = new ArrayList<>();
        archivedList = new ArrayList<>();


        listener = new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onDismiss(NotificationModel notification) {
                if (userId == null || userId.isEmpty()) return;

                String eventId = notification.getEventId();
                String docId   = notification.getFirestoreDocId();

                // Per-user path
                DocumentReference userInboxRef = db.collection("users")
                        .document(userId)
                        .collection("registrations")
                        .document(eventId)
                        .collection("inbox")
                        .document(docId);

                // Old org_events path
                DocumentReference orgInboxRef = db.collection("org_events")
                        .document(eventId)
                        .collection("waiting_list")
                        .document(userId)              // same userId used when creating inbox docs
                        .collection("inbox")
                        .document(docId);

                WriteBatch batch = db.batch();
                batch.update(userInboxRef, "archived", true);
                batch.update(orgInboxRef,  "archived", true);

                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            // Just remove from current inbox list.
                            // Snapshot listener will add it to archivedList when 'archived' becomes true.
                            notificationList.remove(notification);
                            adapter.updateList(getCurrentList(), showingArchived);
                            updateInboxBadgeFromLists();
                            Toast.makeText(getContext(), "Notification archived", Toast.LENGTH_SHORT).show();
                        })

                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(),
                                        "Failed to archive: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
            }



            @Override
            public void onAction(NotificationModel notification) {
                markNotificationAsRead(notification);
                updateInboxBadgeFromLists();
                String eventId = notification.getEventId();
                if (eventId == null || eventId.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No event attached to this notification",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openEventDetailsFromInbox(eventId);
                }
            }

        };

        adapter = new NotificationAdapter(getContext(), getCurrentList(), listener, showingArchived);
        recyclerNotifications.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(v -> markAllRead());
        btnFilterUnread.setOnClickListener(v -> toggleUnreadFilter());
        btnViewArchived.setOnClickListener(v -> toggleArchiveView());

        //signInAnonymouslyIfNeeded();
        loadUserProfileThenNotifications();

        return view;
    }

    private void loadUserProfileThenNotifications() {
        // If we somehow don't have a device ID, just proceed with notifications
        if (userId == null || userId.isEmpty()) {
            signInAnonymouslyIfNeeded();
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        userProfileUrl = doc.getString("profilepicture");
                    }
                    // avatar is loaded, start FirebaseAuth + notifications
                    signInAnonymouslyIfNeeded();
                })
                .addOnFailureListener(e -> {
                    // Even if profile fetch fails, we still want notifications
                    signInAnonymouslyIfNeeded();
                });
    }


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

    private void loadUserNotifications() {
        FirebaseUser user = auth.getCurrentUser();
        //Use ID as the key, still require that FirebaseAuth is not null
        if (user == null || userId == null || userId.isEmpty()) return;

        db.collection("users")
                .document(userId)
                .collection("registrations")
                .get()
                .addOnSuccessListener(regSnapshots -> {
                    if (regSnapshots == null || regSnapshots.isEmpty()) {
                        adapter.updateList(getCurrentList(), showingArchived);
                        updateInboxBadgeFromLists();
                        return;
                    }

                    for (DocumentSnapshot regDoc : regSnapshots) {
                        final String eventId = regDoc.getId();

                        regDoc.getReference()
                                .collection("inbox")
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .addSnapshotListener((snapshots, e) -> {
                                    if (e != null || snapshots == null) return;
                                    if (!isAdded()) return;

                                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                                        DocumentSnapshot inboxDoc = change.getDocument();

                                        String message = inboxDoc.getString("message");
                                        String type = inboxDoc.getString("type") != null
                                                ? inboxDoc.getString("type")
                                                : "general";
                                        Boolean readFlag = inboxDoc.getBoolean("read");
                                        boolean read = readFlag != null && readFlag;
                                        Boolean archivedFlag = inboxDoc.getBoolean("archived");
                                        boolean archived = archivedFlag != null && archivedFlag;
                                        String docId = inboxDoc.getId();

                                        // for time: get createdAt and format it
                                        Timestamp ts = inboxDoc.getTimestamp("createdAt");
                                        String timeString = "";
                                        if (ts != null) {
                                            Date date = ts.toDate();
                                            // e.g. "Nov 27, 5:36 PM"
                                            SimpleDateFormat sdf =
                                                    new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                                            timeString = sdf.format(date);
                                        }


                                        if (message != null) {
                                            NotificationModel notification = new NotificationModel(
                                                    eventId,
                                                    docId, // Firestore document ID
                                                    message,
                                                    timeString,  // from createdAt
                                                    userProfileUrl != null ? userProfileUrl : "",
                                                    read,
                                                    getString(R.string.see_details),
                                                    type
                                            );

                                            if (archived) {
                                                if (!archivedList.contains(notification)) {
                                                    archivedList.add(notification);
                                                }
                                            } else {
                                                if (!notificationList.contains(notification)) {
                                                    notificationList.add(notification);
                                                }
                                            }
                                        }
                                    }

                                    adapter.updateList(getCurrentList(), showingArchived);
                                    updateInboxBadgeFromLists();
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load notifications: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }


    private void markAllRead() {
        for (NotificationModel n : notificationList) n.setRead(true);
        adapter.updateList(getCurrentList(), showingArchived);
        updateInboxBadgeFromLists();
        Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    private void markNotificationAsRead(@NonNull NotificationModel notification) {
        if (userId == null || userId.isEmpty()) return;

        String eventId = notification.getEventId();
        String docId   = notification.getFirestoreDocId();
        if (eventId == null || docId == null) return;

        // Firestore: mark as read in user inbox path
        DocumentReference userInboxRef = db.collection("users")
                .document(userId)
                .collection("registrations")
                .document(eventId)
                .collection("inbox")
                .document(docId);

        userInboxRef.update("read", true);

        // Local model: mark as read so UI + badge update instantly
        notification.setRead(true); // make sure NotificationModel has setRead(boolean)

        adapter.updateList(getCurrentList(), showingArchived);
        updateInboxBadgeFromLists();   //  update  red indicator
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



    private void updateInboxBadgeFromLists() {
        int unreadCount = 0;
        // Only count non-archived notifications
        for (NotificationModel n : notificationList) {
            if (!n.isRead()) unreadCount++;
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateInboxBadge(unreadCount);
        }
    }

}
