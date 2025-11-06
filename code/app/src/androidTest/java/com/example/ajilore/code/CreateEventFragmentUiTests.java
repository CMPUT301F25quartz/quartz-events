package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;

import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;


import android.os.SystemClock;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * CreateEventFragment UI tests (no network/emulator).
 *
 * What we cover:
 * 1) Screen renders and core inputs are visible.
 * 2) Back/Cancel buttons pop the fragment off the back stack.
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventFragmentUiTests {

    /** 1)  */
    @Test
    public void rendersBasicChrome() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new com.example.ajilore.code.ui.events.CreateEventFragment())
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            // Scroll before assertions since the layout may be inside a NestedScrollView.
            onView(withId(R.id.btnBack)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.btnSave)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.btnCancel)).perform(scrollTo()).check(matches(isDisplayed()));

            onView(withId(R.id.etTitle)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.etLocation)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.etDate)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.etRegOpen)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.etRegClose)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.etEventType)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.actCapacity)).perform(scrollTo()).check(matches(isDisplayed()));


            onView(withId(R.id.ivPlusBig)).perform(scrollTo())
                    .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
            onView(withId(R.id.ivPosterPreview))
                    .check(matches(withEffectiveVisibility(Visibility.GONE))); // hidden until image chosen
        }
    }






    @Test
    public void backButtonHidesCreateEventFragment() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            // Put a base fragment first
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new Fragment())
                    .commitNow());

            // Push CreateEventFragment onto the stack
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new com.example.ajilore.code.ui.events.CreateEventFragment())
                    .addToBackStack("create")
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();


            onView(withId(R.id.btnBack)).perform(scrollTo()).check(matches(isDisplayed()));

            // Press back in-UI
            onView(withId(R.id.btnBack)).perform(scrollTo(), click());
            android.os.SystemClock.sleep(250);

            onView(withId(R.id.btnBack)).check(doesNotExist());
        }
    }

}
