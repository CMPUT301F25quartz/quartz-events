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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * US 01.01.04: Fragment for displaying and filtering events
 * Events are fetched live from Firestore and can be filtered by:
 * - Date range
 * - Location
 * - Category
 * - Availability
 *
 * Also provides sorting by status (Open -> Published -> Closed)
 */
public class EventsFragment extends Fragment implements FilterEventsDialogFragment.OnFiltersAppliedListener {

    private RecyclerView rvEvents;
    private ProgressBar progress;
    private TextView emptyView;
    private UserEventsAdapter adapter;
    private FirebaseFirestore db;

    private List<EventRow> allEvents = new ArrayList<>();
    private FilterEventsDialogFragment.EventFilters currentFilters;
    private FloatingActionButton btnFilter;
    private ImageButton btnScanQr;

    // User location for distance filtering
    private Double userLatitude;
    private Double userLongitude;
    private String deviceId;

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

        // Get device ID for user location lookup
        deviceId = android.provider.Settings.Secure.getString(
                requireContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );

        // Initialize views
        rvEvents = view.findViewById(R.id.rvEvents);
        progress = view.findViewById(R.id.progress);
        emptyView = view.findViewById(R.id.emptyView);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnScanQr = view.findViewById(R.id.btnScanQR);

        // Setup RecyclerView
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserEventsAdapter(R.layout.item_event, this::onEventClick);
        rvEvents.setAdapter(adapter);

        // Setup QR scan button
        if (btnScanQr != null) {
            btnScanQr.setOnClickListener(v -> {
                Fragment fragment = new ScanQrFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Setup filter button
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                android.util.Log.d("EventsFragment", "Filter button clicked!");
                openFilterDialog();
            });
        }

        // Load user location, then load events
        loadUserLocation();
    }

    /**
     * Loads the current user's location from Firestore
     */
    private void loadUserLocation() {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        userLatitude = doc.getDouble("latitude");
                        userLongitude = doc.getDouble("longitude");

                        if (userLatitude != null && userLongitude != null) {
                            android.util.Log.d("EventsFragment",
                                    "User location loaded: " + userLatitude + ", " + userLongitude);
                        } else {
                            android.util.Log.d("EventsFragment",
                                    "User location not available");
                        }
                    }
                    // Load events regardless of whether location was found
                    loadAvailableEvents();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EventsFragment",
                            "Failed to load user location: " + e.getMessage());
                    // Load events anyway
                    loadAvailableEvents();
                });
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
                    // Skip flagged events
                    String dbStatus = doc.getString("status");
                    if ("flagged".equals(dbStatus)) {
                        continue;
                    }

                    EventRow eventRow = toEventRow(doc);
                    allEvents.add(eventRow);

                    android.util.Log.d("Firebase", "Event: " + eventRow.title);
                    android.util.Log.d("EventsCheck", "Event ID: " + eventRow.id);
                }
            }

            applyFilters();
        });
    }

    /**
     * Converts a Firestore document to an EventRow object
     */
    private EventRow toEventRow(DocumentSnapshot doc) {
        String id = doc.getId();
        String title = safe(doc.getString("title"), "Untitled Event");
        String location = safe(doc.getString("location"), "TBA");
        String category = doc.getString("category");
        String posterUrl = doc.getString("posterUrl");

        Timestamp startsAt = doc.getTimestamp("startsAt");
        Timestamp regOpens = doc.getTimestamp("regOpens");
        Timestamp regCloses = doc.getTimestamp("regCloses");

        String dateText;
        if (startsAt != null) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
            dateText = df.format(startsAt.toDate());
        } else {
            dateText = "Date TBA";
        }

        Date now = new Date();
        String status = computeStatus(regOpens, regCloses, now);

        EventRow eventRow = new EventRow(
                id,
                title,
                location,
                dateText,
                mapPoster(posterUrl),
                posterUrl,
                status
        );

        // Store additional data for filtering
        eventRow.category = category;
        eventRow.startsAt = startsAt != null ? startsAt.toDate() : null;
        eventRow.regOpens = regOpens != null ? regOpens.toDate() : null;
        eventRow.regCloses = regCloses != null ? regCloses.toDate() : null;

        // Store event location coordinates if available
        eventRow.latitude = doc.getDouble("latitude");
        eventRow.longitude = doc.getDouble("longitude");

        return eventRow;
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
     * Apply filters to the event list and sort by status
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

        // Sort by status: Open -> Published -> Closed
        Collections.sort(filtered, (a, b) -> {
            int rankA = statusRank(a.status);
            int rankB = statusRank(b.status);
            return Integer.compare(rankA, rankB);
        });

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

        // Location filter
        if (currentFilters.locationRange != null) {
            if (userLatitude == null || userLongitude == null) {
                // User location not available, skip this filter
                android.util.Log.d("EventsFragment",
                        "Location filter requested but user location unavailable");
            } else if (event.latitude != null && event.longitude != null) {
                try {
                    // Parse the location range string (e.g., "10km" -> 10.0)
                    String rangeStr = currentFilters.locationRange.toLowerCase().replace("km", "").trim();
                    double rangeKm = Double.parseDouble(rangeStr);

                    // Calculate distance between user and event
                    double distance = calculateDistance(
                            userLatitude, userLongitude,
                            event.latitude, event.longitude
                    );

                    android.util.Log.d("EventsFragment",
                            "Event: " + event.title + ", Distance: " + String.format("%.2f km", distance) +
                                    ", Range: " + rangeKm + " km");

                    // Filter out events beyond the specified range
                    if (distance > rangeKm) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    android.util.Log.e("EventsFragment",
                            "Invalid location range format: " + currentFilters.locationRange);
                }
            } else {
                // Event doesn't have location data, include it by default
                android.util.Log.d("EventsFragment",
                        "Event " + event.title + " has no location data");
            }
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

    /**
     * Helper function to compute the status based on registration window
     */
    private String computeStatus(Timestamp regStartTime, Timestamp regEndTime, Date now) {
        if (regStartTime == null || regEndTime == null) {
            return "Open";
        }
        Date start = regStartTime.toDate();
        Date end = regEndTime.toDate();
        if (now.before(start)) {
            return "Upcoming";
        } else if (now.after(end)) {
            return "Closed";
        } else {
            return "Open";
        }
    }

    /**
     * Helper function to sort the events based on their status
     * @param status The status string
     * @return The rank of the status (lower is higher priority)
     */
    private int statusRank(String status) {
        if (status == null) return 3;
        String s = status.trim().toLowerCase();
        if ("open".equals(s)) return 1;
        if ("published".equals(s)) return 2;
        if ("closed".equals(s)) return 3;
        return 4; // anything else
    }

    /**
     * Returns a string or a default if the value is null or empty.
     * @param s The string to check
     * @param def The default value
     * @return The original string or the default if blank.
     */
    private String safe(String s, String def) {
        return (s == null || s.trim().isEmpty()) ? def : s;
    }

    /**
     * Converts a Firestore poster key to a drawable resource.
     * @param key Poster key.
     * @return The drawable resource ID.
     */
    private int mapPoster(String key) {
        if (key == null) return R.drawable.jazz;
        switch (key) {
            case "jazz":
            case "band":
            case "jimi":
            case "gala":
                return R.drawable.jazz;
            default:
                return R.drawable.jazz;
        }
    }

    /**
     * Handles event click to navigate to details
     */
    private void onEventClick(EventRow item) {
        Fragment detailsFragment = EventDetailsFragment.newInstance(item.id, item.title);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Calculates the distance between two geographic coordinates using the Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}