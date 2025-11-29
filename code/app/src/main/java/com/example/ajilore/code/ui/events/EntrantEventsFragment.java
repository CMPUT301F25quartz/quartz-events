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
import com.example.ajilore.code.ui.events.FilterEventsDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * US 01.01.04: Fragment for entrants to view and filter available events
 * Events are fetched live from Firestore and can be filtered by:
 * - Date range
 * - Location
 * - Category
 * - Availability
 */
public abstract class EntrantEventsFragment extends Fragment implements FilterEventsDialogFragment.OnFiltersAppliedListener {

    private RecyclerView rvEvents;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private UserEventsAdapter adapter;
    private FirebaseFirestore db;

    private List<EventRow> allEvents = new ArrayList<>(); // Store all events
    private FilterEventsDialogFragment.EventFilters currentFilters;
    private ImageButton btnFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_events, container, false);
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        db = FirebaseFirestore.getInstance();
//
//        // Initialize views
//        rvEvents = view.findViewById(R.id.rvEntrantEvents);
//        progressBar = view.findViewById(R.id.progressBar);
//        tvEmptyState = view.findViewById(R.id.tvEmptyState);
//        btnFilter = view.findViewById(R.id.btnFilter);
//
//        // Setup RecyclerView
//        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
//        adapter = new UserEventsAdapter(R.layout.item_event, this::onEventClick);
//        rvEvents.setAdapter(adapter);
//
//        // Setup filter button
//        btnFilter.setOnClickListener(v -> openFilterDialog());
//
//        // Load events
//        loadAvailableEvents();
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.util.Log.d("EntrantEvents", "onViewCreated started");

        db = FirebaseFirestore.getInstance();

        // Initialize views
        rvEvents = view.findViewById(R.id.rvEntrantEvents);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        btnFilter = view.findViewById(R.id.btnFilter);

        android.util.Log.d("EntrantEvents", "btnFilter is null: " + (btnFilter == null));

        // Setup RecyclerView
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserEventsAdapter(R.layout.item_event, this::onEventClick);
        rvEvents.setAdapter(adapter);

        // Setup filter button
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                android.util.Log.d("EntrantEvents", "Filter button CLICKED!");
                openFilterDialog();
            });
            android.util.Log.d("EntrantEvents", "Filter button listener set");
        } else {
            android.util.Log.e("EntrantEvents", "Filter button is NULL!");
        }

        // Load events
        loadAvailableEvents();
    }

    private void openFilterDialog() {
        android.util.Log.d("EntrantEvents", "openFilterDialog() called");

        try {
            FilterEventsDialogFragment filterDialog = FilterEventsDialogFragment.newInstance();
            android.util.Log.d("EntrantEvents", "Dialog fragment created");

            filterDialog.setFiltersListener(this);
            android.util.Log.d("EntrantEvents", "Listener set");

            filterDialog.show(getChildFragmentManager(), "FilterDialog");
            android.util.Log.d("EntrantEvents", "Dialog show() called");

        } catch (Exception e) {
            android.util.Log.e("EntrantEvents", "Error showing dialog", e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Loads all available events from Firestore and updates the list UI.
     */
    private void loadAvailableEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

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

            // Store all events with their full data
            allEvents.clear();

            if (snapshot != null && !snapshot.isEmpty()) {
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

                    // Format date for display
                    String dateText = (startsAt != null)
                            ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(startsAt.toDate())
                            : "Date TBA";

                    // Create EventRow with all necessary data
                    EventRow eventRow = new EventRow(
                            eventId,
                            title != null ? title : "Untitled Event",
                            location != null ? location : "TBA",
                            dateText,
                            mapPoster(posterUrl),
                            posterUrl,
                            status != null ? status : ""
                    );

                    // Store additional data needed for filtering
                    eventRow.category = category;
                    eventRow.startsAt = startsAt != null ? startsAt.toDate() : null;
                    eventRow.regOpens = regOpens != null ? regOpens.toDate() : null;
                    eventRow.regCloses = regCloses != null ? regCloses.toDate() : null;

                    allEvents.add(eventRow);
                }
            }

            // Apply current filters or show all
            applyFilters();
        });
    }
//
//    /**
//     * Opens the filter dialog
//     */
//    private void openFilterDialog() {
//        FilterEventsDialogFragment filterDialog = FilterEventsDialogFragment.newInstance();
//        filterDialog.setFiltersListener(this);
//        filterDialog.show(getChildFragmentManager(), "FilterDialog");
//    }
//
//    /**
//     * US 01.01.04: Called when filters are applied
//     * Updates the displayed events dynamically
//     */
//    @Override
//    public void onFiltersApplied(FilterEventsDialogFragment.EventFilters filters) {
//        currentFilters = filters;
//        applyFilters();
//    }



    @Override
    public void onFiltersCancelled() {
        // Keep current filters - do nothing
    }

    /**
     * US 01.01.04: Apply filters to the event list
     * Results update dynamically based on selected criteria
     */
    private void applyFilters() {
        List<EventRow> filtered = new ArrayList<>();

        if (currentFilters == null || !currentFilters.hasFilters()) {
            // No filters - show all events (AC3: Default view returns to "All Events")
            filtered.addAll(allEvents);
        } else {
            // Apply filters (AC1: Filter by category, date range, location)
            for (EventRow event : allEvents) {
                if (matchesFilters(event)) {
                    filtered.add(event);
                }
            }
        }

        // AC2: Filtered results update dynamically
        adapter.replaceAll(filtered);

        // Update empty state
        if (filtered.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(currentFilters != null && currentFilters.hasFilters()
                    ? "No events match your filters"
                    : "No events available at the moment");
        } else {
            tvEmptyState.setVisibility(View.GONE);
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
                return false; // No date = exclude if date filter applied
            }

            // Check if event date falls within selected range
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

        // Location filter (distance-based)
        if (currentFilters.locationRange != null) {
            // TODO: Implement actual distance calculation
            // For now, this is a placeholder that passes all events
            // You would need to:
            // 1. Get user's current location
            // 2. Calculate distance to event location
            // 3. Compare with selected range (10km, 25km, 50km)

            // Example implementation:
            // double distance = calculateDistance(userLocation, event.location);
            // int rangeKm = Integer.parseInt(currentFilters.locationRange.replace("km", ""));
            // if (distance > rangeKm) return false;
        }

        // Availability filter
        if (currentFilters.availabilityFilter != null) {
            Date now = new Date();

            if ("open".equals(currentFilters.availabilityFilter)) {
                // Filter: Open for registration
                if (event.regOpens == null || event.regCloses == null) {
                    return true; // Always open if no registration window
                }
                // Check if registration is currently open
                if (now.before(event.regOpens) || now.after(event.regCloses)) {
                    return false;
                }
            } else if ("waiting".equals(currentFilters.availabilityFilter)) {
                // Filter: Waiting list available
                // Waiting list is available if registration is open OR closed but not past event date
                if (event.regOpens == null || event.regCloses == null) {
                    return true; // Assume waiting list available
                }
                if (event.startsAt != null && now.after(event.startsAt)) {
                    return false; // Event already happened
                }
            }
        }

        return true; // Passes all filters
    }

    /**
     * Maps a poster key to a drawable resource for the event poster.
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

    /**
     * Handles event row clicks and opens the event details fragment.
     */
    private void onEventClick(EventRow item) {
        Fragment detailsFragment = EventDetailsFragment.newInstance(
                item.id,
                item.title
        );
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
}