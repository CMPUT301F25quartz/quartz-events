package com.example.ajilore.code.ui.events.model;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;
    private FirebaseFirestore db;
    private GoogleMap mMap;

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

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.mapEntrants);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadEntrantLocations();
    }

    private void loadEntrantLocations() {
        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .get()
                .addOnSuccessListener(waitlistDocs -> {
                    for (var wlDoc : waitlistDocs) {
                        String userId = wlDoc.getId();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (!userDoc.exists()) return;

                                    Double lat = userDoc.getDouble("lat");
                                    Double lng = userDoc.getDouble("lng");
                                    String name = userDoc.getString("name");

                                    if (lat == null || lng == null) return;

                                    LatLng pos = new LatLng(lat, lng);

                                    mMap.addMarker(new MarkerOptions()
                                            .position(pos)
                                            .title(name != null ? name : "Unknown User"));

                                    // Zoom to first marker
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("MapError", e.getMessage()));
    }
}
