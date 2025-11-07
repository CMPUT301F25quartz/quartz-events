package com.example.ajilore.code.ui.events;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.list.EventRow;
import com.example.ajilore.code.ui.events.list.UserEventsAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Base fragment displaying a list of events.
 * Meant to be subclassed for specific event list screens.
 * Handles event list loading, RecyclerView setup, and empty/progress views.
 */
public abstract class EventsFragment extends Fragment {

    /**
     * Provides the layout resource ID for one row in the event list.
     * Subclasses must supply a layout with correct binding IDs.
     * @return Layout resource for an event row.
     */
    protected abstract @LayoutRes int getRowLayoutId();

    /**
     * Supplies the Firestore query used to load events.
     * Subclasses can control ordering and filtering.
     * @return Firestore {@link Query} fetching events.
     */
    protected abstract Query getEventsQuery();

    /**
     * Handles the user tapping an event row in the list.
     * @param row The {@link EventRow} that was clicked.
     */
    protected abstract void onEventClick(@NonNull EventRow row);

    private RecyclerView rvEvents;
    private View progress, emptyView;
    private UserEventsAdapter adapter;
    private FirebaseFirestore db;


    /**
     * Inflates the event list fragment view.
     * @param inflater LayoutInflater for view creation.
     * @param container Parent ViewGroup, if any.
     * @param savedInstanceState Previously saved state, if any.
     * @return The root view for this fragment.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    /**
     * Initializes RecyclerView, starts event loading, binds click handlers, and sets up progress/empty views.
     * @param v Root fragment view after inflation.
     * @param s Saved instance state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        rvEvents  = v.findViewById(R.id.rvEvents);
        progress  = v.findViewById(R.id.progress);
        emptyView = v.findViewById(R.id.emptyView);

        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Adapter uses our item layout and click callback
        adapter = new UserEventsAdapter(R.layout.item_event, row -> {
            Fragment f = EventDetailsFragment.newInstance(row.id, row.title);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, f)
                    .addToBackStack(null)
                    .commit();
        });
        rvEvents.setAdapter(adapter);

        // Load events from /org_events by time
        showLoading(true);
        FirebaseFirestore.getInstance()
                .collection("org_events")
                .orderBy("startsAt", Query.Direction.ASCENDING) // or by createdAt if you prefer
                .addSnapshotListener((snap, e) -> {
                    showLoading(false);
                    if (e != null) {
                        Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<EventRow> rows = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String id        = d.getId();
                            String title     = safe(d.getString("title"), "(Untitled)");
                            String location  = safe(d.getString("location"), "TBA");
                            String status    = safe(d.getString("status"), "Open");
                            String posterKey = d.getString("posterUrl");
                            Timestamp ts     = d.getTimestamp("startsAt");
                            String dateText  = (ts != null)
                                    ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(ts.toDate())
                                    : "";

                            rows.add(new EventRow(
                                    id,
                                    title,
                                    location,
                                    dateText,
                                    mapPoster(posterKey),
                                    posterKey,
                                    status
                            ));
                        }
                    }
                    adapter.replaceAll(rows);
                    emptyView.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }


     //---helpers----
    /**
     * Returns a string or a default if the value is null or empty.
     * @param s The string to check
     * @param def The default value
     * @return The original string or the default if blank.
     */
    private String safe(String s, String def){
        return (s == null || s.trim().isEmpty()) ? def:s;

    }

    /**
     * Shows or hides the progress view.
     * @param show True to show, false to hide.
     */
    private void showLoading(boolean show){
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Converts a Firestore poster key to a drawable resource.
     * @param key Poster key.
     * @return The drawable resource ID.
     */
    private int mapPoster(String key){
        if(key == null) return R.drawable.jazz;
        switch(key){
            case "jazz": return R.drawable.jazz;
            case "band" : return R.drawable.jazz;
            case "jimi" : return R.drawable.jazz;
            case "gala" : return R.drawable.jazz;
            default: return R.drawable.jazz;
        }
    }
}