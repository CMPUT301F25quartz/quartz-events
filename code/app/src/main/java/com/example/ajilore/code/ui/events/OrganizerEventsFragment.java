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

import java.util.ArrayList;
import java.util.List;

import com.example.ajilore.code.ui.events.ManageEventsFragment;


public class OrganizerEventsFragment extends Fragment {

    private RecyclerView rv;
    private EventsAdapter adapter;

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

        Button btnCreate = v.findViewById(R.id.btnCreateEvent);
        rv = v.findViewById(R.id.rvMyEvents);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ---- Stub data for now; replace with Firestore later ----
        List<EventItem> data = new ArrayList<>();
        data.add(new EventItem("evt_001", "A virtual evening of smooth jazz", "12 NOV · SAT · 6:00 PM"));
        data.add(new EventItem("evt_002", "International Band Music Concert", "12 DEC · TUES · 4:00 PM"));
        data.add(new EventItem("evt_003", "Collectivity Plays the Music of Jimi", "Mon, Jan 21 · 10:00 PM"));
        data.add(new EventItem("evt_004", "International Gala Music Festival", "10 June · 9:00 PM"));
        // ---------------------------------------------------------

        adapter = new EventsAdapter(data, item -> {
            Fragment f = ManageEventsFragment.newInstance(item.eventId, item.title);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, f)
                    .addToBackStack(null)
                    .commit();
        });
        rv.setAdapter(adapter);


        btnCreate.setOnClickListener(x -> {
            // TODO: navigate to CreateEventFragment
            Toast.makeText(requireContext(), "Create New Event clicked", Toast.LENGTH_SHORT).show();
        });

        // If you want dividers later:
        // rv.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
    }

    // ---------- tiny model ----------
    public static class EventItem {
        public final String eventId;
        public final String title;
        public final String subtitle; // time/date/location line
        public EventItem(String eventId, String title, String subtitle) {
            this.eventId = eventId;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    // ---------- adapter ----------
    interface OnEventClick { void onClick(EventItem item); }

    public static class EventsAdapter extends RecyclerView.Adapter<EventVH> {
        private final List<EventItem> items;
        private final OnEventClick click;

        public EventsAdapter(List<EventItem> items, OnEventClick click) {
            this.items = items;
            this.click = click;
        }

        @NonNull @Override
        public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_organizer_event, parent, false);
            return new EventVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventVH holder, int position) {
            holder.bind(items.get(position), click);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    // ---------- view holder ----------
    public static class EventVH extends RecyclerView.ViewHolder {
        private final android.widget.TextView tvTitle;
        private final android.widget.TextView tvSubtitle;
        private final android.widget.ImageView ivEdit;

        public EventVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            ivEdit  = itemView.findViewById(R.id.ivEdit); // small pencil icon
        }

        void bind(EventItem item, OnEventClick click) {
            tvTitle.setText(item.title);
            tvSubtitle.setText(item.subtitle);
            itemView.setOnClickListener(v -> click.onClick(item));
            ivEdit.setOnClickListener(v -> click.onClick(item)); // same action for now
        }
    }
}
