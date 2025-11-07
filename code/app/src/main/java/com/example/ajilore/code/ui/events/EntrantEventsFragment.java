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
 * Fragment for entrants to view all available events in a scrollable list.
 * Events are fetched live from Firestore and shown in chronological order.
 */
public class EntrantEventsFragment extends Fragment {

    private RecyclerView rvEvents;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private UserEventsAdapter adapter;
    private final List<EventRow> eventsList = new ArrayList<>();
    private FirebaseFirestore db;

    private String currentUserId = "user_temp_id"; // TODO: Replace with actual user ID from auth

    /**
     * Inflate the events list layout for entrants.
     *
     * @param inflater  The LayoutInflater object.
     * @param container The parent ViewGroup.
     * @param savedInstanceState Saved instance state Bundle.
     * @return The fragment's root view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_events, container, false);
    }

    /**
     * Initializes the RecyclerView, adapter, and starts the event loading process.
     *
     * @param view The fragment's root view after inflation.
     * @param savedInstanceState Previously saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        rvEvents = view.findViewById(R.id.rvEntrantEvents);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserEventsAdapter(R.layout.item_event, this::onEventClick);
        rvEvents.setAdapter(adapter);

        loadAvailableEvents();
    }

    /**
     * Loads all available events from Firestore and updates the list UI.
     * Shows or hides a progress bar and "empty" message as appropriate.
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

            List<EventRow> newList = new ArrayList<>();
            if (snapshot != null && !snapshot.isEmpty()) {
                Date now = new Date();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String eventId = doc.getId();
                    String title = doc.getString("title");
                    String location = doc.getString("location");
                    String status = doc.getString("status");
                    Timestamp startsAt = doc.getTimestamp("startsAt");
                    String posterUrl = doc.getString("posterUrl");

                    // Format date
                    String dateText = (startsAt != null)
                            ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(startsAt.toDate())
                            : "Date TBA";

                    newList.add(new EventRow(
                            eventId,
                            title != null ? title : "Untitled Event",
                            location != null ? location : "TBA",
                            dateText,
                            mapPoster(posterUrl),  // fallback resource
                            posterUrl,             // Cloudinary URL or null
                            status != null ? status : ""
                    ));
                }
            }

            eventsList.clear();
            eventsList.addAll(newList);
            adapter.replaceAll(eventsList);

            tvEmptyState.setVisibility(eventsList.isEmpty() ? View.VISIBLE : View.GONE);
            if (eventsList.isEmpty()) {
                tvEmptyState.setText("No events available at the moment");
            }
        });
    }

    /**
     * Maps a poster key to a drawable resource for the event poster.
     *
     * @param key String resource key.
     * @return Drawable resource ID fallback.
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
     *
     * @param item The {@link EventRow} that was tapped.
     */
    private void onEventClick(EventRow item) {
        Fragment detailsFragment = EventDetailsFragment.newInstance(
                item.id, // eventId
                item.title // eventTitle
        );
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
}
