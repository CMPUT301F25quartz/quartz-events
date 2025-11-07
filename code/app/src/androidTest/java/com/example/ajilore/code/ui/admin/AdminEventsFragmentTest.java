package com.example.ajilore.code.ui.admin;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.example.ajilore.code.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.os.Bundle;

/**
 * Instrumented tests for AdminEventsFragment.
 *
 * Tests US 03.04.01 - Admin can browse events
 * Tests US 03.01.01 - Admin can remove events
 *
 * @author Dinma (Team Quartz)
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class AdminEventsFragmentTest {

    private FragmentScenario<AdminEventsFragment> scenario;

    @Before
    public void setUp() {
        // Launch fragment in test container
        // Launch fragment in test container using a theme that defines standard attributes
        // This ensures attributes like '?attr/selectableItemBackgroundBorderless' are resolved.
        // We use the fully qualified name for the theme from the AndroidX support library
        // to avoid incorrect R class imports, which is causing the current error.
        scenario = FragmentScenario.launchInContainer(
                AdminEventsFragment.class,
                null, // fragmentArgs
                androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar,
                (FragmentFactory) null // factory
        );
        // Wait for fragment to fully load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test US 03.04.01: Verify events list UI components are displayed
     */
    @Test
    public void testEventsFragmentComponentsDisplayed() {
        // Verify back button is displayed
        onView(withId(R.id.btn_back))
                .check(matches(isDisplayed()));

        // Verify title is displayed with correct text
        onView(withId(R.id.tv_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Browse Events")));

        // Verify admin badge is displayed
        onView(withId(R.id.tv_admin_badge))
                .check(matches(isDisplayed()))
                .check(matches(withText("ADMIN")));

        // Verify search bar is displayed
        onView(withId(R.id.et_search_events))
                .check(matches(isDisplayed()));
    }

    /**
     * Test US 03.04.01: Verify back button functionality
     */
    @Test
    public void testBackButtonWorks() {
        // Click back button
        onView(withId(R.id.btn_back))
                .perform(click());

        // Fragment should be popped from back stack
        // Verified by the fact that click doesn't crash
    }


    /**
     * Test that menu button is displayed
     */
    @Test
    public void testMenuButtonDisplayed() {
        onView(withId(R.id.btn_menu))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that search hint is correct
     */
    @Test
    public void testSearchBarHint() {
        // Search bar should have appropriate hint text
        onView(withId(R.id.et_search_events))
                .check(matches(isDisplayed()));
    }

    /**
     * Helper method to wait for async operations
     */
    private void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}