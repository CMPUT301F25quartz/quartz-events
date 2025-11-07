package com.example.ajilore.code;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * Instrumented tests for AdminActivity.
 *
 * Tests US 03.04.01 and US 03.05.01 - Admin browsing functionality
 *
 * @author Dinma (Team Quartz)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminActivityTest {

    private ActivityScenario<AdminActivity> scenario;

    @Before
    public void setUp() {
        Intents.init();
        // Launch activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminActivity.class);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        Intents.release();
        if (scenario != null) {
            scenario.close();
        }
    }

    /**
     * Test US 03.04.01: Verify admin can navigate to events browsing section
     */
    @Test
    public void testNavigateToEventsSection() {
        // Wait for the about fragment to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click on Events button
        onView(withId(R.id.btn_events))
                .perform(click());

        // Wait for fragment transition
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify Events fragment is displayed by checking for the title
        onView(withText("Browse Events"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test US 03.05.01: Verify admin can navigate to profiles browsing section
     */
    @Test
    public void testNavigateToProfilesSection() {
        // Wait for the about fragment to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click on Profiles button
        onView(withId(R.id.btn_profiles))
                .perform(click());

        // Wait for fragment transition
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify Profiles fragment is displayed
        onView(withText("Browse Profiles"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that admin activity launches successfully
     */
    @Test
    public void testAdminActivityLaunches() {
        // Just verify the activity launched
        onView(withId(R.id.admin_container))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that all navigation buttons are present
     */
    @Test
    public void testNavigationItemsPresent() {
        // Wait for fragment to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.btn_events))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btn_profiles))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btn_images))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that admin name is displayed
     */
    @Test
    public void testAdminNameDisplayed() {
        // Wait for fragment to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.tv_admin_name))
                .check(matches(isDisplayed()));
    }
}