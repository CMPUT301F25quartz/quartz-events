package com.example.ajilore.code.ui.events;

import androidx.annotation.NonNull;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.list.EventRow;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * {@code GeneralEventsFragment} displays the main list of events visible to all users.
 * <p>
 * This fragment extends the abstract {@link EventsFragment} and supplies:
 * <ul>
 *     <li>The layout resource used for each event row</li>
 *     <li>The Firestore query that retrieves events</li>
 *     <li>The navigation behavior when a user taps an event</li>
 * </ul>
 *
 * <p>It serves as the default "Events" tab in the application and shows
 * all non-flagged events sorted by their starting date in ascending order.
 * Rendering, list management, and loading indicators are handled by the parent
 * {@link EventsFragment} class.</p>
 *
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
        EventDetailsFragment f = EventDetailsFragment.newInstance(row.id, row.title);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, f)
                .addToBackStack(null)
                .commit();
    }
}
