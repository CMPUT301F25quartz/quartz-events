package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.list.EventRow;
import com.example.ajilore.code.ui.events.list.UserEventsAdapter;
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
 * US 01.01.04: Fragment for displaying and filtering events
 * Events are fetched live from Firestore and can be filtered by:
 * - Date range
 * - Location
 * - Category
 * - Availability
 */
public class EventsFragment extends Fragment implements FilterEventsDialogFragment.OnFiltersAppliedListener {

    private RecyclerView rvEvents;
    private ProgressBar progress;
    private TextView emptyView;
    private UserEventsAdapter adapter;
    private FirebaseFirestore db;

    private List<EventRow> allEvents = new ArrayList<>();
    private FilterEventsDialogFragment.EventFilters currentFilters;
    private ImageButton btnFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.util.Log.d("EventsFragment", "=== Fragment loaded ===");

        db = FirebaseFirestore.getInstance();

        // Initialize views
        rvEvents = view.findViewById(R.id.rvEvents);
        progress = view.findViewById(R.id.progress);
        emptyView = view.findViewById(R.id.emptyView);
        btnFilter = view.findViewById(R.id.btnFilter);

        // Setup RecyclerView
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserEventsAdapter(R.layout.item_event, this::onEventClick);
        rvEvents.setAdapter(adapter);

        // Setup filter button
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                android.util.Log.d("EventsFragment", "Filter button clicked!");
                openFilterDialog();
            });
        }

        // Load events
        loadAvailableEvents();
    }

    /**
     * Loads all available events from Firestore
     */
    private void loadAvailableEvents() {
        progress.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        Query query = db.collection("org_events")
                .orderBy("startsAt", Query.Direction.ASCENDING);

        query.addSnapshotListener((QuerySnapshot snapshot, FirebaseFirestoreException e) -> {
            progress.setVisibility(View.GONE);

            if (e != null) {
                Toast.makeText(requireContext(),
                        "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }

            allEvents.clear();

            if (snapshot != null && !snapshot.isEmpty()) {
                android.util.Log.d("Firebase", "Found " + snapshot.size() + " events");

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String eventId = doc.getId();
                    String title = doc.getString("title");
                    String location = doc.getString("location");
                    String status = doc.getString("status");
                    String category = doc.getString("category");
                    Timestamp startsAt = doc.getTimestamp("startsAt");
                    Timestamp regOpens = doc.getTimestamp("regOpens");
                    Timestamp regCloses = doc.getTimestamp("regCloses");
                    String posterUrl = doc.getString("posterUrl");

                    android.util.Log.d("Firebase", "Event: " + title);
                    android.util.Log.d("EventsCheck", "Event ID: " + eventId);

                    String dateText = (startsAt != null)
                            ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(startsAt.toDate())
                            : "Date TBA";

                    EventRow eventRow = new EventRow(
                            eventId,
                            title != null ? title : "Untitled Event",
                            location != null ? location : "TBA",
                            dateText,
                            mapPoster(posterUrl),
                            posterUrl,
                            status != null ? status : ""
                    );

                    // Store additional data for filtering
                    eventRow.category = category;
                    eventRow.startsAt = startsAt != null ? startsAt.toDate() : null;
                    eventRow.regOpens = regOpens != null ? regOpens.toDate() : null;
                    eventRow.regCloses = regCloses != null ? regCloses.toDate() : null;

                    allEvents.add(eventRow);
                }
            }

            applyFilters();
        });
    }

    /**
     * Opens the filter dialog
     */
    private void openFilterDialog() {
        android.util.Log.d("EventsFragment", "Opening filter dialog");

        try {
            FilterEventsDialogFragment filterDialog = FilterEventsDialogFragment.newInstance();
            filterDialog.setFiltersListener(this);
            filterDialog.show(getChildFragmentManager(), "FilterDialog");
            android.util.Log.d("EventsFragment", "Filter dialog shown");
        } catch (Exception e) {
            android.util.Log.e("EventsFragment", "Error showing filter", e);
            Toast.makeText(requireContext(), "Error opening filter: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFiltersApplied(FilterEventsDialogFragment.EventFilters filters) {
        android.util.Log.d("EventsFragment", "Filters applied");
        currentFilters = filters;
        applyFilters();
    }

    @Override
    public void onFiltersCancelled() {
        // Keep current filters
    }

    /**
     * Apply filters to the event list
     */
    private void applyFilters() {
        List<EventRow> filtered = new ArrayList<>();

        if (currentFilters == null || !currentFilters.hasFilters()) {
            filtered.addAll(allEvents);
        } else {
            for (EventRow event : allEvents) {
                if (matchesFilters(event)) {
                    filtered.add(event);
                }
            }
        }

        adapter.replaceAll(filtered);

        if (filtered.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(currentFilters != null && currentFilters.hasFilters()
                    ? "No events match your filters"
                    : "No upcoming events yet.");
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Check if an event matches the current filters
     */
    private boolean matchesFilters(EventRow event) {
        if (currentFilters == null) return true;

        // Date range filter
        if (currentFilters.startDate != null && currentFilters.endDate != null) {
            if (event.startsAt == null) {
                return false;
            }
            if (event.startsAt.before(currentFilters.startDate) ||
                    event.startsAt.after(currentFilters.endDate)) {
                return false;
            }
        }

        // Category filter
        if (currentFilters.categories != null && !currentFilters.categories.isEmpty()) {
            if (event.category == null || !currentFilters.categories.contains(event.category)) {
                return false;
            }
        }

        // Location filter (placeholder)
        if (currentFilters.locationRange != null) {
            // TODO: Implement distance calculation
        }

        // Availability filter
        if (currentFilters.availabilityFilter != null) {
            Date now = new Date();

            if ("open".equals(currentFilters.availabilityFilter)) {
                if (event.regOpens == null || event.regCloses == null) {
                    return true;
                }
                if (now.before(event.regOpens) || now.after(event.regCloses)) {
                    return false;
                }
            } else if ("waiting".equals(currentFilters.availabilityFilter)) {
                if (event.regOpens == null || event.regCloses == null) {
                    return true;
                }
                if (event.startsAt != null && now.after(event.startsAt)) {
                    return false;
                }
            }
        }

        return true;
    }

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

    private void onEventClick(EventRow item) {
        Fragment detailsFragment = EventDetailsFragment.newInstance(item.id, item.title);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
}