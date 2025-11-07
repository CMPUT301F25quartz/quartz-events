
/**
 * Test that checks that when the organizer taps "Create New Event",
 * the app successfully navigates to the create-event screen.
 *
 *
 * - Opens MainActivity
 * - Shows OrganizerEventsFragment
 * - Clicks the "Create New Event" button
 * - Verifies that navigation happens by checking back stack increase
 */

package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.ui.events.ManageEventsFragment;
import com.example.ajilore.code.ui.events.OrganizerEventsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventsFragmentIntentTest {

    @Test
    public void clickingCreateEvent_navigatesToCreateScreen() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {

            // 1) Attach OrganizerEventsFragment on the main thread
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new OrganizerEventsFragment())
                    .commitNow());

            // 2) Sanity: the button is visible (run from test thread)
            onView(withId(R.id.btnCreateEvent)).check(matches(isDisplayed()));

            // 3) Capture back stack BEFORE (must read via onActivity)
            AtomicInteger before = new AtomicInteger();
            sc.onActivity(a -> {
                FragmentManager fm = a.getSupportFragmentManager();
                before.set(fm.getBackStackEntryCount());
            });

            // 4) Click the button (test thread)
            onView(withId(R.id.btnCreateEvent)).perform(click());

            // 5) Give the transaction a tiny moment (or use IdlingResource if you have one)
            SystemClock.sleep(250);

            // 6) Capture back stack AFTER and assert (main-thread read, test-thread assert)
            AtomicInteger after = new AtomicInteger();
            sc.onActivity(a -> {
                FragmentManager fm = a.getSupportFragmentManager();
                after.set(fm.getBackStackEntryCount());
            });

            org.junit.Assert.assertTrue("Expected back stack to increase", after.get() > before.get());
        }
    }
}
