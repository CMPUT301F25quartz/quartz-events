package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.ajilore.code.ui.events.ManageEventQRFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ManageEventQRFragment.
 *
 * Covers:
 * 1) Core UI renders with provided title/subtitle.
 * 2) Back button pops the fragment off the back stack.
 */
@RunWith(AndroidJUnit4.class)
public class ManageEventQRFragmentTests {

    /** Core UI smoke test: title/subtitle and QR container are visible. */
    @Test
    public void coreUi_renders_withArgs() {
        final String title = "My Sample Event";

        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, ManageEventQRFragment.newInstance("evt_123", title))
                    .commit());
            // Let the transaction settle
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.tvEventTitle)).check(matches(withText(title)));
            onView(withId(R.id.tvEventSubtitle))
                    .check(matches(withText("Scan to view event • opens in app or web")));

            // Basic presence checks (don’t assert bitmap content to avoid flakiness)
            onView(withId(R.id.ivQR)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.btnShare)).check(matches(isDisplayed()));
            onView(withId(R.id.btnDownload)).check(matches(isDisplayed()));
        }
    }

    /** In-UI back button should pop this fragment from the back stack. */
    @Test
    public void backButton_popsFragment() {
        final String title = "Back Test Event";

        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            // Put a base fragment first (not on back stack)
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new Fragment())
                    .commitNow());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            // Push ManageEventQRFragment ONTO the back stack
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, ManageEventQRFragment.newInstance("evt_456", title))
                    .addToBackStack("qr")
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            final int[] before = {0};
            sc.onActivity(a -> before[0] = a.getSupportFragmentManager().getBackStackEntryCount());

            onView(withId(R.id.btnBack)).check(matches(isDisplayed())).perform(click());

            // Give the pop a short beat
            android.os.SystemClock.sleep(250);

            final int[] after = {0};
            sc.onActivity(a -> after[0] = a.getSupportFragmentManager().getBackStackEntryCount());

            org.junit.Assert.assertTrue("Back should pop QR fragment", after[0] < before[0]);
        }
    }
}
