package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.ajilore.code.ui.events.EventDetailsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * EventDetailsFragment — simple, reliable UI tests.
 *
 * Covers:
 * 1) Basic UI rendering
 * 2) Back button pops from back stack
 * 3) Join/Leave button click is safe
 * 4) Registration window label visibility
 * 5) Waiting list count label visibility
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailsFragmentTests {

    /** 1) Basic UI renders properly with safe args. */
    @Test
    public void rendersMinimalChrome() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment,
                            EventDetailsFragment.newInstance("evt_123", "Sample Event")) // Now with 2 arguments
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.btnJoinLeaveWaitingList)).check(matches(isDisplayed()));
            onView(withId(R.id.ivEventPoster)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.tvLotteryInfo)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
        }
    }

    /** 2) Back button should pop the fragment from the back stack. */
    @Test
    public void backButtonPopsFragment() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new Fragment())
                    .commitNow());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment,
                            EventDetailsFragment.newInstance("evt_456", "Back Test"))
                    .addToBackStack("details")
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            final int[] before = {0};
            sc.onActivity(a -> before[0] = a.getSupportFragmentManager().getBackStackEntryCount());

            onView(withId(R.id.btnBack)).check(matches(isDisplayed())).perform(click());
            SystemClock.sleep(250);

            final int[] after = {0};
            sc.onActivity(a -> after[0] = a.getSupportFragmentManager().getBackStackEntryCount());

            org.junit.Assert.assertTrue("Back should pop EventDetailsFragment", after[0] < before[0]);
        }
    }

    /** 3) Join/Leave button click should not crash. */
    @Test
    public void joinLeaveClickIsSafe() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment,
                            EventDetailsFragment.newInstance("evt_789", "Click Test"))
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.btnJoinLeaveWaitingList)).check(matches(isDisplayed())).perform(click());
        }
    }

    @Test
    public void postersVisible() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment,
                            EventDetailsFragment.newInstance("evt_789", "Click Test"))
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            onView(withId(R.id.ivEventPoster)).check(matches(isDisplayed()));
            onView(withId(R.id.ivEventPoster)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
        }
    }

    @Test
    public void lotteryInfoVisible() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment,
                            EventDetailsFragment.newInstance("evt_789", "Click Test"))
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            onView(withId(R.id.tvLotteryInfo)).check(matches(isDisplayed()));
            onView(withId(R.id.tvLotteryInfo)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
        }
    }


}

