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
 * A simple {@link Fragment} subclass.
 * Use the {@link com.example.ajilore.code.ui.events.EventsFragment#} factory method to
 * create an instance of this fragment.
 */
public abstract class EventsFragment extends Fragment {


    protected abstract @LayoutRes int getRowLayoutId();
    protected abstract Query getEventsQuery();
    protected abstract void onEventClick(@NonNull EventRow row);

    private RecyclerView rvEvents;
    private View progress, emptyView;
    private UserEventsAdapter adapter;
    private FirebaseFirestore db;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }


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

    private String safe(String s, String def){
        return (s == null || s.trim().isEmpty()) ? def:s;

    }

    private void showLoading(boolean show){
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

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