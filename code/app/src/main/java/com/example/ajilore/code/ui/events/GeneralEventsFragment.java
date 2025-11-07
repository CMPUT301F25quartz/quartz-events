package com.example.ajilore.code.ui.events;

import androidx.annotation.NonNull;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.list.EventRow;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Fragment for the main "Events" tab.
 * Displays a scrollable, general list of events visible to all users,
 * using 'item_event.xml' for each list row.
 * Inherits presentation, adapter, and click behavior from EventsFragment.
 */
public class GeneralEventsFragment extends EventsFragment {

    /**
     * Returns the layout resource ID for each event row.
     * Must match the view IDs expected by the list adapter.
     *
     * @return The layout resource for event rows (item_event).
     */
    @Override protected int getRowLayoutId() {
        // Must match the IDs the adapter binds (ivPoster, tvTitle, tvDate, tvLocation)
        return R.layout.item_event;
    }

    /**
     * Builds and returns the Firestore query for general events.
     * This fetches all events ordered by start date (ascending).
     *
     * @return Firestore query for the events collection.
     */
    @Override protected Query getEventsQuery() {
        // Use the same collection you’re writing in CreateEventFragment
        return FirebaseFirestore.getInstance()
                .collection("org_events")
                .orderBy("startsAt", Query.Direction.ASCENDING);
    }

    /**
     * Handles event row clicks by opening the EventDetailsFragment
     * for the selected event.
     *
     * @param row The row that was clicked.
     */
    @Override protected void onEventClick(@NonNull EventRow row) {
        // Navigate to details
        EventDetailsFragment f = EventDetailsFragment.newInstance(row.id, row.title, /* userId */ "demoUser");
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, f)
                .addToBackStack(null)
                .commit();
    }
}
