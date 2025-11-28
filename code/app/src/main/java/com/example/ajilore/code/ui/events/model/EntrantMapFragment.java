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

public class EntrantMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;
    private FirebaseFirestore db;
    private GoogleMap myMap;
    private final List<LatLng> entrantLocations = new ArrayList<>();

    public static EntrantMapFragment newInstance(String eventId) {
        EntrantMapFragment f = new EntrantMapFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_map, container, false);
    }

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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        loadEntrantLocations();
    }

    private void loadEntrantLocations() {
        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .get()
                .addOnSuccessListener(waitlistDocs -> {

                    if (waitlistDocs.isEmpty()){
                        Log.d("MapDebug", "No entrants in waiting list.");
                        return;
                    }

                    int total = waitlistDocs.size();
                    final int[] loadedCount = {0};

                    for (var wlDoc : waitlistDocs) {
                        String userId = wlDoc.getId();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {

                                    loadedCount[0]++;
                                    if (!userDoc.exists()) return;

                                    Double lat = userDoc.getDouble("latitude");
                                    Double lng = userDoc.getDouble("longitude");
                                    String name = userDoc.getString("name");

                                    if (lat == null || lng == null) return;

                                    LatLng pos = new LatLng(lat, lng);
                                    entrantLocations.add(pos);


                                    myMap.addMarker(new MarkerOptions()
                                            .position(pos)
                                            .title(name != null ? name : "User"));



                                    if (loadedCount[0] == total) {
                                        fitCameraToAllMarkers();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("MapError", e.getMessage()));
    }
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
