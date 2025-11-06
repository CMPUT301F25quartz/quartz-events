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
    private FirebaseAuth auth;

    private MaterialButton btnMarkAllRead;
    private MaterialButton btnFilterUnread;
    private MaterialButton btnViewArchived;

    private boolean showOnlyUnread = false;
    private boolean showingArchived = false;

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
        btnViewArchived = view.findViewById(R.id.btnViewArchived); // new button in layout

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationList = new ArrayList<>();
        archivedList = new ArrayList<>();

        adapter = new NotificationAdapter(getContext(), notificationList,
                new NotificationAdapter.OnNotificationActionListener() {
                    @Override
                    public void onDismissClicked(NotificationModel notification) {
                        // Move to archived
                        notificationList.remove(notification);
                        archivedList.add(notification);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Notification archived", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDetailsClicked(NotificationModel notification) {
                        Toast.makeText(getContext(),
                                "Open details for: " + notification.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        recyclerNotifications.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(v -> markAllRead());
        btnFilterUnread.setOnClickListener(v -> toggleUnreadFilter());
        btnViewArchived.setOnClickListener(v -> toggleArchiveView());

        // Ensure Firebase user is available
        signInAnonymouslyIfNeeded();

        return view;
    }

    private void signInAnonymouslyIfNeeded() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            auth.signInAnonymously().addOnSuccessListener(result -> {
                Toast.makeText(getContext(), "Signed in anonymously", Toast.LENGTH_SHORT).show();
                loadUserNotifications();
            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Auth failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } else {
            loadUserNotifications();
        }
    }

    private void loadUserNotifications() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = "demoUser"; // temporary user ID

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
                                String eventTitle = inboxDoc.getString("eventTitle");
                                String type = inboxDoc.getString("type") != null ? inboxDoc.getString("type") : "general";
                                boolean read = inboxDoc.getBoolean("read") != null && inboxDoc.getBoolean("read");

                                if (message != null && eventTitle != null) {
                                    NotificationModel notification = new NotificationModel(
                                            eventId,
                                            eventTitle + ": " + message,
                                            read,
                                            type
                                    );

                                    if (!notificationList.contains(notification)) {
                                        notificationList.add(0, notification);
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Error loading inbox: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void markAllRead() {
        for (NotificationModel n : notificationList) {
            n.setRead(true);
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    private void toggleUnreadFilter() {
        showOnlyUnread = !showOnlyUnread;
        applyFilters();
        btnFilterUnread.setText(showOnlyUnread ? R.string.show_all : R.string.show_unread);
    }

    private void toggleArchiveView() {
        showingArchived = !showingArchived;
        applyFilters();
        btnViewArchived.setText(showingArchived ? R.string.show_inbox : R.string.view_archived);
    }

    private void applyFilters() {
        List<NotificationModel> sourceList = showingArchived ? archivedList : notificationList;
        List<NotificationModel> filteredList = new ArrayList<>();

        for (NotificationModel n : sourceList) {
            if (showOnlyUnread && n.isRead()) continue;
            filteredList.add(n);
        }

        adapter = new NotificationAdapter(getContext(), filteredList, adapter.getListener());
        recyclerNotifications.setAdapter(adapter);
    }
}