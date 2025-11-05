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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment {

    private RecyclerView recyclerNotifications;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private MaterialButton btnMarkAllRead;
    private MaterialButton btnFilterUnread;

    private boolean showOnlyUnread = false;

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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationList = new ArrayList<>();

        adapter = new NotificationAdapter(getContext(), notificationList,
                new NotificationAdapter.OnNotificationActionListener() {
                    @Override
                    public void onDismissClicked(NotificationModel notification) {
                        notification.setRead(true);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Marked as read", Toast.LENGTH_SHORT).show();
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

        // 🔥 Load from Firestore instead of dummy data
        loadUserNotifications();

        return view;
    }

    // 🔥 Load notifications based on Firebase structure
    private void loadUserNotifications() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "demo-user";

        db.collection("org_events").get().addOnSuccessListener(eventSnapshots -> {
            for (DocumentSnapshot eventDoc : eventSnapshots) {
                String eventId = eventDoc.getId();
                String eventTitle = eventDoc.getString("title") != null ? eventDoc.getString("title") : "Unnamed Event";

                eventDoc.getReference().collection("entrants").document(userId).get()
                        .addOnSuccessListener(entrantDoc -> {
                            if (entrantDoc.exists()) {
                                String status = entrantDoc.getString("status");
                                if (status == null) return;

                                eventDoc.getReference().collection("broadcasts").get()
                                        .addOnSuccessListener(broadcasts -> {
                                            for (DocumentSnapshot b : broadcasts) {
                                                String audience = b.getString("audience");
                                                String message = b.getString("message");

                                                if (audience == null || message == null) continue;

                                                if ((status.equals("selected") && audience.equals("chosen")) ||
                                                        (status.equals("rejected") && audience.equals("not_selected"))) {

                                                    notificationList.add(new NotificationModel(
                                                            eventId,
                                                            eventTitle + ": " + message,
                                                            false,
                                                            "general"
                                                    ));
                                                }
                                            }
                                            adapter.notifyDataSetChanged();
                                        });
                            }
                        });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
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
        applyUnreadFilter();
        btnFilterUnread.setText(showOnlyUnread ? R.string.show_all : R.string.show_unread);
    }

    private void applyUnreadFilter() {
        List<NotificationModel> filteredList = new ArrayList<>();
        for (NotificationModel n : notificationList) {
            if (showOnlyUnread && n.isRead()) continue;
            filteredList.add(n);
        }
        adapter = new NotificationAdapter(getContext(), filteredList, adapter.getListener());
        recyclerNotifications.setAdapter(adapter);
    }
}
