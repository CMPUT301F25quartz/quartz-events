package com.example.ajilore.code.ui.events;

import androidx.annotation.NonNull;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.list.EventRow;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * This is the CONCRETE fragment for the main "Events" tab.
 * It shows a general list of events that any user can see.
 * It uses the 'item_event.xml' layout for its list items.
 */
public class GeneralEventsFragment extends EventsFragment {

    @Override protected int getRowLayoutId() {
        // Must match the IDs the adapter binds (ivPoster, tvTitle, tvDate, tvLocation)
        return R.layout.item_event;
    }

    @Override protected Query getEventsQuery() {
        // Use the same collection you’re writing in CreateEventFragment
        return FirebaseFirestore.getInstance()
                .collection("org_events")
                .orderBy("startsAt", Query.Direction.ASCENDING);
    }

    @Override protected void onEventClick(@NonNull EventRow row) {
        // Navigate to details
        EventDetailsFragment f = EventDetailsFragment.newInstance(row.id, row.title);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, f)
                .addToBackStack(null)
                .commit();
    }
}
