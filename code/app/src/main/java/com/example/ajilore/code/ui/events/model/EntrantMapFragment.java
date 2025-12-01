package com.example.ajilore.code.ui.events.model;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code EntrantMapFragment} displays a Google Map containing the geographic
 * locations of all entrants who joined an event’s waiting list and have
 * shared their location.
 *
 * <p>This feature supports the organizer’s ability to visualize participant
 * distribution and is especially useful for events where proximity or travel
 * analysis may matter.</p>
 *
 * <h3>Core Responsibilities</h3>
 * <ul>
 *     <li>Retrieves all entrants in {@code org_events/{eventId}/waiting_list}</li>
 *     <li>Extracts saved latitude/longitude values for each entrant</li>
 *     <li>Places map markers for entrants who provided valid location data</li>
 *     <li>Automatically adjusts the camera to fit all markers</li>
 *     <li>Provides a back navigation button and hides bottom navigation while active</li>
 * </ul>
 *
 * <h3>Navigation</h3>
 * This fragment is normally opened from an event detail screen when the
 * organizer taps “View Entrant Map”.
 *
 * @see com.example.ajilore.code.ui.events.EventDetailsFragment
 * @see com.google.android.gms.maps.SupportMapFragment
 *
 * @author
 *     Temi Akindele
 */
public class EntrantMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;
    private FirebaseFirestore db;
    private GoogleMap myMap;
    private final List<LatLng> entrantLocations = new ArrayList<>();

    /**
     * Factory method for creating a new {@code EntrantMapFragment} tied
     * to a specific event.
     *
     * @param eventId The Firestore document ID of the event.
     * @return A configured instance containing the event ID as an argument.
     */
    public static EntrantMapFragment newInstance(String eventId) {
        EntrantMapFragment f = new EntrantMapFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    /**
     * Inflates the map layout, which contains a {@link SupportMapFragment}
     * for rendering Google Maps.
     *
     * @return The inflated root view of the map fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_map, container, false);
    }

    /**
     * Initializes Firestore, extracts the event ID, hides the bottom
     * navigation bar, wires the back button, and prepares the Google Map
     * by requesting an asynchronous map callback.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState Saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        eventId = getArguments().getString(ARG_EVENT_ID);

        // Hide bottom nav
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNav();
        }

        ImageButton btnBack = view.findViewById(R.id.btnBackMap);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .popBackStack(); //goes back to previous fragment
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.mapEntrants);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Called once the Google Map is fully initialized. Stores the map
     * reference and begins loading entrant locations from Firestore.
     *
     * @param googleMap The fully prepared GoogleMap object.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        loadEntrantLocations();
    }

    /**
     * Fetches all documents under {@code waiting_list} for the event and
     * extracts the latitude/longitude stored for each entrant.
     *
     * <p>For each entrant with valid coordinates:</p>
     * <ul>
     *     <li>A marker is added to the map</li>
     *     <li>The coordinates are stored locally for camera adjustment</li>
     * </ul>
     *
     * Entrants without stored location data are logged and skipped.
     *
     * <p>After all markers are added, the camera is adjusted to include
     * all visible markers.</p>
     */
    private void loadEntrantLocations() {
        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .get()
                .addOnSuccessListener(waitlistDocs -> {

                    if (waitlistDocs.isEmpty()) {
                        Log.d("MapDebug", "No entrants in waiting list.");
                        return;
                    }
                    for (var wlDoc : waitlistDocs) {

                        Double lat = wlDoc.getDouble("latitude");
                        Double lng = wlDoc.getDouble("longitude");

                        if (lat == null || lng == null) {
                            Log.d("MapDebug", "User " + wlDoc.getId() + " has no location saved.");
                            continue;
                        }

                        LatLng pos = new LatLng(lat, lng);
                        entrantLocations.add(pos);

                        myMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title("Entrant"));
                    }

                    fitCameraToAllMarkers();
                })
                .addOnFailureListener(e ->
                        Log.e("MapError", e.getMessage()));
    }

    /**
     * Adjusts the map camera to show all entrant markers.
     *
     * <p>Behavior:</p>
     * <ul>
     *     <li>If no markers exist: do nothing</li>
     *     <li>If exactly one marker exists: zoom in directly on that marker</li>
     *     <li>If multiple markers exist: build a bounding box and adjust
     *         the camera to include all markers with padding</li>
     * </ul>
     */
    private void fitCameraToAllMarkers() {
        if (entrantLocations.isEmpty()) return;

        if (entrantLocations.size() == 1) {
            // One entrant → zoom into them
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(entrantLocations.get(0), 13));
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (LatLng p : entrantLocations) {
            builder.include(p);
        }

        LatLngBounds bounds = builder.build();

        myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
    }
}
