package com.example.ajilore.code;

/**
 * UI Test Suite emulator-free): OrganizerEventsFragment
 *
 *
 * Tests included:
 * 1) rendersCoreUi() — screen shows key views.
 * 2) createButton_isEnabled_andClickable() — button is enabled and clickable (navigates away; we only sanity-check click).
 * 3) recycler_itemCount_nonNegative() — adapter is attached and count >= 0 (works with or without data).
 * 4) recycler_safeScroll_noCrash() — attempts a safe scroll if items exist (no-op if list empty).
 */


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.ui.events.OrganizerEventsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventsFragmentUiSuite {

    /** Shows OrganizerEventsFragment and asserts core views are visible. */
    @Test
    public void rendersCoreUi() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            // Put OrganizerEventsFragment on screen
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new OrganizerEventsFragment())
                    .commitNow());

            // Core widgets
            onView(withId(R.id.btnCreateEvent)).check(matches(isDisplayed()));
            onView(withId(R.id.tvMyEvents)).check(matches(isDisplayed()));
            onView(withId(R.id.rvMyEvents)).check(matches(isDisplayed()));
        }
    }

    /**
     * Button is enabled and clickable.
     * We don't assert destination details (to avoid touching other fragments/SKDs).
     */
    @Test
    public void createButton_isEnabled_andClickable() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new OrganizerEventsFragment())
                    .commitNow());

            onView(withId(R.id.btnCreateEvent)).check(matches(isDisplayed()));
            onView(withId(R.id.btnCreateEvent)).check(matches(isEnabled()));

            // Click (this navigates to create screen; that flow already has a passing test in your other file)
            onView(withId(R.id.btnCreateEvent)).perform(click());

            // Give the transaction a tick so the click completes cleanly
            SystemClock.sleep(200);
        }
    }

    /** Adapter exists and item count is non-negative (works with 0 or many events). */
    @Test
    public void recycler_itemCount_nonNegative() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new OrganizerEventsFragment())
                    .commitNow());

            final int[] count = { -1 };
            sc.onActivity(a -> {
                RecyclerView rv = a.findViewById(R.id.rvMyEvents);
                count[0] = (rv != null && rv.getAdapter() != null) ? rv.getAdapter().getItemCount() : -1;
            });

            org.junit.Assert.assertTrue("Adapter should be attached with count >= 0", count[0] >= 0);
        }
    }

    /** Try to scroll the RecyclerView safely if data exists (no crash when empty). */
    @Test
    public void recycler_safeScroll_noCrash() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new OrganizerEventsFragment())
                    .commitNow());

            final int[] count = { 0 };
            sc.onActivity(a -> {
                RecyclerView rv = a.findViewById(R.id.rvMyEvents);
                count[0] = (rv != null && rv.getAdapter() != null) ? rv.getAdapter().getItemCount() : 0;
            });

            // Only attempt scroll if there is at least one item
            if (count[0] > 0) {
                onView(withId(R.id.rvMyEvents)).perform(
                        androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition(0));
                // Sanity: still displayed after scroll
                onView(withId(R.id.rvMyEvents)).check(matches(isDisplayed()));
            } else {
                // If empty, just assert the list is on screen (no-op)
                onView(withId(R.id.rvMyEvents)).check(matches(isDisplayed()));
            }
        }
    }
}
