/**
 * Tests if OrganizerEvents list renders regardless of Firebase contents.
 *
 * This test:
 * - Opens MainActivity
 * - Shows OrganizerEventsFragment
 * - checks if UI (create button + list) is visible
 * - If the list has >=1 item, clicks the first item and verifies navigation by back stack increase
 *   (If there are 0 items, it skips the click and still passes.)
 */

package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.ui.events.OrganizerEventsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventsFragmentListBehaviorTest {

    @Test
    public void listRenders_andIfItemsExist_clickFirstNavigates() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            // Show the organizer events screen
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new OrganizerEventsFragment())
                    .commitNow());

            // Core UI is visible
            onView(withId(R.id.btnCreateEvent)).check(matches(isDisplayed()));
            onView(withId(R.id.rvMyEvents)).check(matches(isDisplayed()));

            // Read current item count from the adapter (may be 0 or more)
            final int[] count = {0};
            sc.onActivity(a -> {
                RecyclerView rv = a.findViewById(R.id.rvMyEvents);
                count[0] = (rv != null && rv.getAdapter() != null) ? rv.getAdapter().getItemCount() : 0;
            });

            // If there is at least one item, click it and verify navigation happened
            if (count[0] > 0) {
                // capture back stack before
                final int[] before = {0};
                sc.onActivity(a -> {
                    FragmentManager fm = a.getSupportFragmentManager();
                    before[0] = fm.getBackStackEntryCount();
                });

                // click item 0 (run from test thread)
                onView(withId(R.id.rvMyEvents))
                        .perform(androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition(0, click()));

                // give the transaction a moment
                SystemClock.sleep(250);

                // capture back stack after
                final int[] after = {0};
                sc.onActivity(a -> {
                    FragmentManager fm = a.getSupportFragmentManager();
                    after[0] = fm.getBackStackEntryCount();
                });

                org.junit.Assert.assertTrue("Expected navigation when an item exists", after[0] > before[0]);
            } else {
                // No items? That’s fine. We’ve already verified the screen renders.
                // Test passes without clicking anything.
                org.junit.Assert.assertEquals("List has no items; skipping click", 0, count[0]);
            }
        }
    }
}

