package com.example.ajilore.code.ui.inbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> allNotifications = new ArrayList<>();

    private boolean showingUnread = false;
    private boolean showingArchived = false;

    private Button btnMarkAllRead, btnFilterUnread, btnViewArchived;

    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        recyclerView = view.findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        btnFilterUnread = view.findViewById(R.id.btnFilterUnread);
        btnViewArchived = view.findViewById(R.id.btnViewArchived);

        prefs = requireContext().getSharedPreferences("inbox_prefs", Context.MODE_PRIVATE);

        loadNotifications();

        adapter = new NotificationAdapter(
                getContext(),
                getFilteredNotifications(),
                new NotificationAdapter.OnNotificationActionListener() {
                    @Override
                    public void onDismiss(NotificationModel item) {
                        item.setArchived(true);
                        saveNotifications();
                        adapter.updateList(getFilteredNotifications());
                    }

                    @Override
                    public void onAction(NotificationModel item) {
                        // Future action handling (e.g., open details)
                    }
                }
        );

        recyclerView.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(v -> markAllRead());
        btnFilterUnread.setOnClickListener(v -> toggleUnreadFilter());
        btnViewArchived.setOnClickListener(v -> toggleArchiveView());

        updateButtonText();

        return view;
    }

    private void loadNotifications() {
        String json = prefs.getString("notifications", null);
        allNotifications.clear();

        if (json != null) {
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    NotificationModel n = new NotificationModel();
                    n.setId(obj.optString("id"));
                    n.setMessage(obj.optString("message"));
                    n.setTime(obj.optString("time", "Just now"));
                    n.setType(obj.optString("type", "general"));
                    n.setRead(obj.optBoolean("isRead", false));
                    n.setArchived(obj.optBoolean("isArchived", false));
                    allNotifications.add(n);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // Dummy data for first-time use
            allNotifications.add(
                    new NotificationModel(
                            "1",
                            "A virtual night of Jazz: Congrats, you're chosen!",
                            "Just now",
                            null,
                            false,
                            "View Details",
                            "lottery_winner",
                            false
                    )
            );
        }
    }

    private void saveNotifications() {
        JSONArray arr = new JSONArray();
        for (NotificationModel n : allNotifications) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", n.getId());
                obj.put("message", n.getMessage());
                obj.put("time", n.getTime());
                obj.put("isRead", n.isRead());
                obj.put("isArchived", n.isArchived());
                obj.put("type", n.getType());
                arr.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString("notifications", arr.toString()).apply();
    }

    private List<NotificationModel> getFilteredNotifications() {
        List<NotificationModel> filtered = new ArrayList<>();
        for (NotificationModel n : allNotifications) {
            if (showingArchived) {
                if (n.isArchived()) filtered.add(n);
            } else if (showingUnread) {
                if (!n.isRead() && !n.isArchived()) filtered.add(n);
            } else {
                if (!n.isArchived()) filtered.add(n);
            }
        }
        return filtered;
    }

    private void toggleUnreadFilter() {
        showingUnread = !showingUnread;
        adapter.updateList(getFilteredNotifications());
        btnFilterUnread.setText(showingUnread ? R.string.show_all : R.string.show_unread);
    }

    private void toggleArchiveView() {
        showingArchived = !showingArchived;
        adapter.updateList(getFilteredNotifications());
        updateButtonText();
    }

    private void markAllRead() {
        for (NotificationModel n : allNotifications) {
            if (!n.isArchived()) n.setRead(true);
        }
        saveNotifications();
        adapter.updateList(getFilteredNotifications());
    }

    private void updateButtonText() {
        btnViewArchived.setText(showingArchived ? R.string.show_inbox : R.string.view_archived);
    }
}
