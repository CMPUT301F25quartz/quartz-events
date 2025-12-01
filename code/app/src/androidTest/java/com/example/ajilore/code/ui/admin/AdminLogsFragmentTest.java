package com.example.ajilore.code.ui.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot; // IMPORT THIS
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.example.ajilore.code.R;
import com.example.ajilore.code.MyViewActions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class AdminLogsFragmentTest {

    @Before
    public void setUp() {
        // Launch Fragment with the correct theme and cast null to FragmentFactory
        FragmentScenario.launchInContainer(
                AdminNotificationLogsFragment.class,
                null,
                androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar,
                (FragmentFactory) null
        );

        // FIX: Perform waitFor on 'isRoot()', not on a specific view ID
        onView(isRoot()).perform(MyViewActions.waitFor(1000));
    }

    @Test
    public void testLogFilters() {
        // Use forceClick to handle buttons inside HorizontalScrollView safely
        onView(withId(R.id.btn_filter_waiting)).perform(MyViewActions.forceClick());
        onView(isRoot()).perform(MyViewActions.waitFor(500)); // Short wait for filter to apply

        onView(withId(R.id.btn_filter_selected)).perform(MyViewActions.forceClick());
        onView(isRoot()).perform(MyViewActions.waitFor(500));

        // This button is often off-screen, forceClick handles it without needing to scroll
        onView(withId(R.id.btn_filter_cancelled)).perform(MyViewActions.forceClick());
        onView(isRoot()).perform(MyViewActions.waitFor(500));

        // Verify the list exists (it might be empty or visible depending on data)
        // We check 'rv_logs' specifically to ensure the layout hasn't crashed
        onView(withId(R.id.rv_logs)).check(matches(isDisplayed()));
    }
}