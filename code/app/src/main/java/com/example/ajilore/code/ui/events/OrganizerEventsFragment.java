package com.example.ajilore.code.ui.events;
import com.example.ajilore.code.ui.events.list.EventItem;
import com.example.ajilore.code.ui.events.list.OrganizerEventsAdapter;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * OrganizerEventsFragment
 *
 * Purpose: Shows the organizer’s events in a scrollable list. Live-updates from Firestore
 * and routes into ManageEventsFragment when an event is clicked.
 *
 * Pattern: Fragment + RecyclerView adapter (simple list controller).
 *
 */


public class OrganizerEventsFragment extends Fragment {

    private RecyclerView rv;
    private OrganizerEventsAdapter adapter;
    private final List<EventItem> data = new ArrayList<>();
    private FirebaseFirestore db;

    private ImageView ivAvatar;
    private TextView tvName;

    /**
     * Inflates the organizer events screen.
     */
    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_events, container, false);
    }


    /**
     * Sets up RecyclerView, adapter, click handlers, and Firestore listening.
     */

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // back button navigation
        ImageButton btnBack = v.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> requireActivity().onBackPressed());

        db = FirebaseFirestore.getInstance();

        ivAvatar = v.findViewById(R.id.ivAvatar);
        tvName   = v.findViewById(R.id.tvName);
        loadOrganizerHeader();

        Button btnCreate = v.findViewById(R.id.btnCreateEvent);
        rv = v.findViewById(R.id.rvMyEvents);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new OrganizerEventsAdapter(data, item -> {
            Fragment f = ManageEventsFragment.newInstance(item.eventId, item.title);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, f)
                    .addToBackStack(null)
                    .commit();
        });
        rv.setAdapter(adapter);

        btnCreate.setOnClickListener(x -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new CreateEventFragment())
                .addToBackStack(null) .commit()
        );

        loadEvents();
    }


    private void loadOrganizerHeader() {
        // same device ID scheme you use everywhere else
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (deviceId == null || deviceId.isEmpty()) {
            return;
        }

        db.collection("users")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null || !doc.exists()) return;

                    String name = doc.getString("name");
                    String profileUrl = doc.getString("profilepicture");

                    if (name != null && !name.isEmpty() && tvName != null) {
                        tvName.setText(name);
                    }

                    if (profileUrl != null && !profileUrl.isEmpty() && ivAvatar != null) {
                        Glide.with(this)
                                .load(profileUrl)
                                .circleCrop()
                                .placeholder(R.drawable.organizer_profileimage)
                                .error(R.drawable.organizer_profileimage)
                                .into(ivAvatar);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    /**
     * Subscribes to /org_events ordered by startsAt (desc) and updates the list on changes.
     */
    private void loadEvents() {
        Query q = db.collection("org_events")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override public void onEvent(@Nullable QuerySnapshot snap, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                data.clear();
                if (snap != null) {
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String id = d.getId();
                        String title = d.getString("title");
                        Timestamp ts = d.getTimestamp("startsAt");
                        String dateText = (ts != null)
                                ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(ts.toDate())
                                : "";
                        String posterKey = d.getString("posterUrl");

                        //Added by Precious
                        String type = d.getString("type");
                        String location = d.getString("location");

                        Object capacityObj = d.get("capacity");
                        String capacity = "";

                        if (capacityObj instanceof Number) {
                            // If it's already a number, convert it to a string.
                            capacity = String.valueOf(((Number) capacityObj).longValue());
                        } else if (capacityObj instanceof String) {
                            // If it's a string, just use it directly.
                            // (You could also try to parse it, but for display this is fine).
                            capacity = (String) capacityObj;
                        }


                        String subtitle = "";
                        if(type != null && !type.isEmpty()){
                            subtitle = type;
                        }

                        if(location != null && !location.isEmpty()){
                            subtitle = subtitle.isEmpty() ? location : (subtitle + " · " + location);
                        }

                        //I think we can include capacity if its present
                        if(capacity != null && !capacity.isEmpty()){
                            subtitle = subtitle.isEmpty() ? (capacity + " ppl") :
                                    (subtitle + " · " + capacity + " ppl");
                        }

                        data.add(new EventItem(id,
                                title != null ? title : "(untitled)",
                                dateText,
                                mapPoster(posterKey), subtitle, posterKey));

                        android.util.Log.d("OrgList", "subtitle=" + subtitle);

                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    // Map posterKey -> drawable resource id

    /**
     * Maps a posterKey from Firestore to a local drawable resource.
     * @param key string like "jazz", "band", etc. (nullable)
     * @return drawable resource id to show in the list
     */

    private int mapPoster(String key) {
        if (key == null) return R.drawable.jazz;
        switch (key) {
            //they are located in the drawable folder
            //will update/change these images if needed, placeholder for code to work for now
            case "jazz": return R.drawable.jazz;
            case "band": return R.drawable.jazz;
            case "jimi": return R.drawable.jazz;
            case "gala": return R.drawable.jazz;
            default: return R.drawable.jazz;
        }
    }

    // ---------- model ----------



    // ---------- adapter ----------






}
