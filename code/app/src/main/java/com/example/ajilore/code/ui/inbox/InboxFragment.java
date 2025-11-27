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

import com.example.ajilore.code.R;
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
 * Fragment representing an inbox screen.
 * Use the {@link #newInstance(String, String)} factory method to
 * create an instance with specific parameters.
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

        notificationList = new ArrayList<>();
        archivedList = new ArrayList<>();

        listener = new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onDismiss(NotificationModel notification) {
                // Archive notification in Firestore using the correct doc ID
                DocumentReference ref = db.collection("org_events")
                        .document(notification.getEventId())
                        .collection("waiting_list")
                        .document("demoUser") // replace with actual userId
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
        if (user == null) return;

        String userId = "demoUser"; // replace with actual user ID

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
                            if(!isAdded()) return; //Added by Precious

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
                                            docId, // Firestore document ID
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
                            adapter.updateList(getCurrentList(), showingArchived);
                        });
            }
        });
    }

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
