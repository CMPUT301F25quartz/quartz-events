package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fragment for entrants to view and browse available events
 * US 01.01.03: As an entrant, I want to be able to see a list of events that I can join the waiting list for.
 */
public class EntrantEventsFragment extends Fragment {

    private RecyclerView rvEvents;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private EntrantEventsAdapter adapter;
    private final List<EventItem> eventsList = new ArrayList<>();
    private FirebaseFirestore db;

    // TODO: Replace with actual user ID from authentication
    private String currentUserId = "user_temp_id";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        rvEvents = view.findViewById(R.id.rvEntrantEvents);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EntrantEventsAdapter(eventsList, this::onEventClick);
        rvEvents.setAdapter(adapter);

        // Load events
        loadAvailableEvents();
    }

    /**
     * Load events that are open for registration
     * Filters events where registration is currently open
     */
    private void loadAvailableEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        // Query events ordered by start date
        Query query = db.collection("org_events")
                .orderBy("startsAt", Query.Direction.ASCENDING);

        query.addSnapshotListener((QuerySnapshot snapshot, FirebaseFirestoreException e) -> {
            progressBar.setVisibility(View.GONE);

            if (e != null) {
                Toast.makeText(requireContext(),
                        "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }

            eventsList.clear();

            if (snapshot != null && !snapshot.isEmpty()) {
                Date now = new Date();

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String eventId = doc.getId();
                    String title = doc.getString("title");
                    String description = doc.getString("description");
                    Timestamp startsAt = doc.getTimestamp("startsAt");
                    Timestamp regStartTime = doc.getTimestamp("registrationStartTime");
                    Timestamp regEndTime = doc.getTimestamp("registrationEndTime");
                    Long capacity = doc.getLong("capacity");
                    String posterKey = doc.getString("posterKey");

                    // Check if registration is open
                    boolean isOpen = isRegistrationOpen(regStartTime, regEndTime, now);

                    // Format date
                    String dateText = (startsAt != null)
                            ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(startsAt.toDate())
                            : "Date TBA";

                    EventItem item = new EventItem(
                            eventId,
                            title != null ? title : "Untitled Event",
                            description,
                            dateText,
                            capacity != null ? capacity.intValue() : 0,
                            isOpen,
                            mapPoster(posterKey)
                    );

                    eventsList.add(item);
                }
            }

            // Show empty state if no events
            if (eventsList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("No events available at the moment");
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }

            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Check if registration window is currently open
     */
    private boolean isRegistrationOpen(Timestamp regStart, Timestamp regEnd, Date now) {
        if (regStart == null || regEnd == null) {
            return true; // Default to open if times not set
        }
        Date start = regStart.toDate();
        Date end = regEnd.toDate();
        return now.after(start) && now.before(end);
    }

    /**
     * Handle event card click - navigate to event details
     */
    private void onEventClick(EventItem item) {
        Fragment detailsFragment = EventDetailsFragment.newInstance(
                item.eventId,
                item.title,
                currentUserId
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Map poster key to drawable resource
     */
    private int mapPoster(String key) {
        if (key == null) return R.drawable.jazz;
        switch (key) {
            case "jazz": return R.drawable.jazz;
            case "band": return R.drawable.jazz;
            case "jimi": return R.drawable.jazz;
            case "gala": return R.drawable.jazz;
            default: return R.drawable.jazz;
        }
    }

    // ========== MODEL ==========
    public static class EventItem {
        public final String eventId;
        public final String title;
        public final String description;
        public final String dateText;
        public final int capacity;
        public final boolean isRegistrationOpen;
        public final int posterRes;

        public EventItem(String eventId, String title, String description,
                         String dateText, int capacity, boolean isOpen, int posterRes) {
            this.eventId = eventId;
            this.title = title;
            this.description = description;
            this.dateText = dateText;
            this.capacity = capacity;
            this.isRegistrationOpen = isOpen;
            this.posterRes = posterRes;
        }
    }

    // ========== ADAPTER ==========
    interface OnEventClickListener {
        void onClick(EventItem item);
    }

    public static class EntrantEventsAdapter extends RecyclerView.Adapter<EntrantEventViewHolder> {
        private final List<EventItem> items;
        private final OnEventClickListener clickListener;

        public EntrantEventsAdapter(List<EventItem> items, OnEventClickListener listener) {
            this.items = items;
            this.clickListener = listener;
        }

        @NonNull
        @Override
        public EntrantEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_entrant_event, parent, false);
            return new EntrantEventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EntrantEventViewHolder holder, int position) {
            holder.bind(items.get(position), clickListener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    // ========== VIEW HOLDER ==========
    public static class EntrantEventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvStatus;
        private final android.widget.ImageView ivPoster;

        public EntrantEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvStatus = itemView.findViewById(R.id.tvEventStatus);
            ivPoster = itemView.findViewById(R.id.ivEventPoster);
        }

        void bind(EventItem event, OnEventClickListener listener) {
            tvTitle.setText(event.title);
            tvDate.setText(event.dateText);
            ivPoster.setImageResource(event.posterRes);

            // Set status
            if (event.isRegistrationOpen) {
                tvStatus.setText("Registration Open");
                tvStatus.setTextColor(0xFF17C172); // Green
            } else {
                tvStatus.setText("Registration Closed");
                tvStatus.setTextColor(0xFFFF6B6B); // Red
            }

            itemView.setOnClickListener(v -> listener.onClick(event));
        }
    }
}