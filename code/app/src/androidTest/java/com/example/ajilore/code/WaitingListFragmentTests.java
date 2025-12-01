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

import com.example.ajilore.code.ui.events.WaitingListFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WaitingListFragmentTests {

    @Test
    public void rendersCoreUi_noFirestore() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, WaitingListFragment.newInstance("evt_test"))
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.rvEntrants)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.etSearch)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.tvTotal)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.tvAccepted)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.tvDeclined)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.tvPending)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
        }
    }

    @Test
    public void backButton_popsFragment() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            // base fragment, not on back stack
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new Fragment())
                    .commitNow());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            // push normal fragment
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, WaitingListFragment.newInstance("evt_test"))
                    .addToBackStack("waiting")
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            final int[] before = {0};
            sc.onActivity(a -> before[0] = a.getSupportFragmentManager().getBackStackEntryCount());

            onView(withId(R.id.btnBack)).check(matches(isDisplayed())).perform(click());
            SystemClock.sleep(250);

            final int[] after = {0};
            sc.onActivity(a -> after[0] = a.getSupportFragmentManager().getBackStackEntryCount());

            org.junit.Assert.assertTrue("Back should pop fragment", after[0] < before[0]);
        }
    }
}

