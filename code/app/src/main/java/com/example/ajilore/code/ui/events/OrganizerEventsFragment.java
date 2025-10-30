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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizerEventsFragment extends Fragment {

    private RecyclerView rv;
    private EventsAdapter adapter;
    private final List<EventItem> data = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        db.collection("org_events")
                .limit(1)
                .get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    if (snap.isEmpty()) {
                        Map<String, Object> e = new HashMap<>();
                        e.put("title", "A Virtual Evening of Smooth Jazz");
                        e.put("startsAt", Timestamp.now());
                        e.put("posterKey", "jazz");

                        db.collection("org_events")
                                .add(e)
                                .addOnSuccessListener(ref ->
                                        Toast.makeText(requireContext(), "Seeded one sample event ✅", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(err ->
                                        Toast.makeText(requireContext(), "Seed failed: " + err.getMessage(), Toast.LENGTH_LONG).show());
                    }
                })
                .addOnFailureListener(err ->
                        Toast.makeText(requireContext(), "Check Firestore rules/connection: " + err.getMessage(), Toast.LENGTH_LONG).show());

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

        btnCreate.setOnClickListener(x ->{
                Toast.makeText(requireContext(), "Create New Event clicked", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new CreateEventFragment())
                        .addToBackStack(null)
                        .commit();
                });

        loadEvents();
    }

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
    public static class EventItem {
        public final String eventId, title, dateText;
        public final int posterRes;
        public EventItem(String eventId, String title, String dateText, int posterRes) {
            this.eventId = eventId; this.title = title; this.dateText = dateText; this.posterRes = posterRes;
        }
    }

    // ---------- adapter ----------
    interface OnEventClick { void onClick(EventItem item); }

    public static class EventsAdapter extends RecyclerView.Adapter<EventVH> {
        private final List<EventItem> items;
        private final OnEventClick click;
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
        void bind(EventItem e, OnEventClick click) {
            tvTitle.setText(e.title);
            tvDate.setText(e.dateText);
            ivPoster.setImageResource(e.posterRes);
            itemView.setOnClickListener(v -> click.onClick(e));
            ivEdit.setOnClickListener(v -> click.onClick(e));
        }
    }
}
