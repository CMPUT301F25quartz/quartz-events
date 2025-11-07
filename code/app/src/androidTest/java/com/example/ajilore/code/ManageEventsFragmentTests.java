package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.ajilore.code.ui.events.ManageEventsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ManageEventsFragments
 *
 * Tests:
 * 1) coreUi_renders_initialState: key views visible; notify card hidden.
 * 2) notifyFlow_showsCard_setsHint_andEmptyMessageErrors: tap Notify -> pick audience -> form shows;
 *    pressing Send with empty message shows field error.
 * 3) qrButton_navigates_to_QR: tapping QR button pushes ManageEventQRFragment (back stack increases).
 */
@RunWith(AndroidJUnit4.class)
public class ManageEventsFragmentTests {

    /** 1) Core UI renders and notify card starts hidden. */
    @Test
    public void coreUi_renders_initialState() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, ManageEventsFragment.newInstance("evt_123", "Sample Event"))
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            // Top bits
            onView(withId(R.id.tvEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));

            // Pills + actions
            onView(withId(R.id.tgWaiting)).check(matches(isDisplayed()));
            onView(withId(R.id.tgChosen)).check(matches(isDisplayed()));
            onView(withId(R.id.tgSelected)).check(matches(isDisplayed()));
            onView(withId(R.id.tgCancelled)).check(matches(isDisplayed()));
            onView(withId(R.id.btnNotifyEntrants)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSelectEntrants)).check(matches(isDisplayed()));
            onView(withId(R.id.btnQR)).check(matches(isDisplayed()));
            onView(withId(R.id.btnEditEvent)).check(matches(isDisplayed()));

            // Notify card should be hidden initially
            onView(withId(R.id.cardNotify)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));
        }
    }

    /** 2) Notify flow: click Notify, pick CHOSEN -> form shows with hint; Send with empty msg errors. */
    @Test
    public void notifyFlow_showsCard_setsHint_andEmptyMessageErrors() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, ManageEventsFragment.newInstance("evt_456", "Notify Event"))
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            // Enter notify mode
            onView(withId(R.id.btnNotifyEntrants)).check(matches(isDisplayed())).perform(click());

            // Select an audience pill (CHOSEN)
            onView(withId(R.id.tgChosen)).check(matches(isDisplayed())).perform(click());

            // Form should now be visible with the CHOSEN hint
            onView(withId(R.id.cardNotify)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.etMessage))
                    .check(matches(withHint("You've been chosen! Please accept by Friday @ 5pm.")));

            // Press Send with empty message -> field error "Message required"
            onView(withId(R.id.btnSend)).perform(click());
            onView(withId(R.id.etMessage)).check(matches(hasErrorText("Message required")));

            // (Optional) Type some text just to ensure input works
            onView(withId(R.id.etMessage)).perform(replaceText("Test message"), closeSoftKeyboard());
            onView(withId(R.id.etMessage)).check(matches(isDisplayed()));
        }
    }

    /** 3) QR button navigates to the QR screen (back stack increases). */
    @Test
    public void qrButton_navigates_to_QR() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            // Put a base fragment first (not on back stack)
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new Fragment())
                    .commitNow());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            // Show ManageEventsFragment ON back stack
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, ManageEventsFragment.newInstance("evt_qr", "QR Event"))
                    .addToBackStack("manage")
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            final int[] before = {0};
            sc.onActivity(a -> before[0] = a.getSupportFragmentManager().getBackStackEntryCount());

            // Tap QR
            onView(withId(R.id.btnQR)).check(matches(isDisplayed())).perform(click());

            // Let navigation settle
            android.os.SystemClock.sleep(250);

            final int[] after = {0};
            sc.onActivity(a -> after[0] = a.getSupportFragmentManager().getBackStackEntryCount());

            org.junit.Assert.assertTrue("QR tap should navigate (increase back stack)", after[0] > before[0]);
        }
    }
}
