package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

/**
 * OrganizerEventsFragment
 *
 * Purpose: Shows the organizer’s events in a scrollable list. Live-updates from Firestore
 * and routes into ManageEventsFragment when an event is clicked.
 *
 * Pattern: Fragment + RecyclerView adapter (simple list controller).
 *
 */


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
    private EventsAdapter adapter;
    private final List<EventItem> data = new ArrayList<>();
    private FirebaseFirestore db;


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

        db = FirebaseFirestore.getInstance();

        Button btnCreate = v.findViewById(R.id.btnCreateEvent);
        rv = v.findViewById(R.id.rvMyEvents);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EventsAdapter(data, item -> {
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


    /**
     * Subscribes to /org_events ordered by startsAt (desc) and updates the list on changes.
     */
    private void loadEvents() {
        Query q = db.collection("org_events")
                .orderBy("startsAt", Query.Direction.DESCENDING);

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
                        String posterKey = d.getString("posterKey");

                        data.add(new EventItem(id,
                                title != null ? title : "(untitled)",
                                dateText,
                                mapPoster(posterKey)));
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

    /**
     * Lightweight UI model for one organizer event row.
     */
    public static class EventItem {
        public final String eventId, title, dateText;
        public final int posterRes;


        /**
         * @param eventId   Firestore document id
         * @param title     event title to display
         * @param dateText  human-readable date/time
         * @param posterRes drawable resource for the list image
         */


        public EventItem(String eventId, String title, String dateText, int posterRes) {
            this.eventId = eventId; this.title = title; this.dateText = dateText; this.posterRes = posterRes;
        }
    }

    // ---------- adapter ----------
    /** Click handler for an event row. */
    interface OnEventClick { void onClick(EventItem item); }


    /**
     * RecyclerView adapter that binds EventItem rows for the organizer list.
     */
    public static class EventsAdapter extends RecyclerView.Adapter<EventVH> {
        private final List<EventItem> items;
        private final OnEventClick click;

        /**
         * @param items backing list (changed when Firestore updates)
         * @param click row click callback to open ManageEventsFragment
         */

        public EventsAdapter(List<EventItem> items, OnEventClick click) {
            this.items = items; this.click = click;
        }
        @NonNull @Override public EventVH onCreateViewHolder(@NonNull ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_organizer_event, p, false);
            return new EventVH(view);
        }
        @Override public void onBindViewHolder(@NonNull EventVH h, int pos) { h.bind(items.get(pos), click); }
        @Override public int getItemCount() { return items.size(); }
    }

    // ---------- view holder ----------
    /**
     * Holds the views for one event row and binds an EventItem into them.
     */
    public static class EventVH extends RecyclerView.ViewHolder {
        private final android.widget.TextView tvTitle, tvDate;
        private final android.widget.ImageView ivEdit, ivPoster;

        public EventVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate  = itemView.findViewById(R.id.tvDate);
            ivEdit  = itemView.findViewById(R.id.ivEdit);
            ivPoster= itemView.findViewById(R.id.ivPoster);
        }


        /**
         * Binds one EventItem to the row and wires up clicks.
         * @param e     event data to show
         * @param click callback for row/edit icon clicks
         */
        void bind(EventItem e, OnEventClick click) {
            tvTitle.setText(e.title);
            tvDate.setText(e.dateText);
            ivPoster.setImageResource(e.posterRes);
            itemView.setOnClickListener(v -> click.onClick(e));
            ivEdit.setOnClickListener(v -> click.onClick(e));
        }
    }
}
