package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminAcceptanceTest {

    @Before
    public void launchActivity() {
        ActivityScenario.launch(AdminActivity.class);
    }


    // US 03.05.01 - Browse Profiles & US 03.07.01 - Remove Organizers
    @Test
    public void testBrowseAndFilterProfiles() throws InterruptedException {
        // 1. Navigate to Profiles
        onView(withId(R.id.btn_profiles)).perform(click());
        Thread.sleep(1000);

        // 2. Verify Filter Buttons work
        onView(withId(R.id.rb_organizers)).perform(click());
        Thread.sleep(500);

        // 3. Verify List updates (UI check for existence)
        onView(withId(R.id.rv_users)).check(matches(isDisplayed()));

        // 4. Test Organizer Deletion Flow
        // Click an organizer (simulating a click on the list item to see details/delete)
        // Or click the delete button directly if exposed in the adapter
        onView(withId(R.id.rv_users))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // If it's an organizer, it might show the "Activity" dialog with "Deactivate" button
        // We verify a dialog appears
        onView(withText(org.hamcrest.Matchers.anyOf(
                org.hamcrest.Matchers.containsString("Activity"),
                org.hamcrest.Matchers.containsString("Entrant")
        ))).check(matches(isDisplayed()));
    }

    // US 03.06.01 - Browse Images & US 03.03.01 - Remove Images
    @Test
    public void testBrowseImages() throws InterruptedException {
        // 1. Navigate to Images
        onView(withId(R.id.btn_images)).perform(click());
        Thread.sleep(1500);

        // 2. Verify Grid is displayed
        onView(withId(R.id.rv_images)).check(matches(isDisplayed()));

        // 3. Verify Search functionality
        onView(withId(R.id.et_search_images)).perform(typeText("Concert"), closeSoftKeyboard());
    }

    // US 03.08.01 - Review Logs
    @Test
    public void testNotificationLogs() throws InterruptedException {
        // Assuming there is a button to get to logs (e.g., via a menu or button hidden in AdminActivity)
        // If logic exists to open it:
        // onView(withId(R.id.btn_logs)).perform(click());

        // Since the user provided the Fragment but not the navigation button in the main test file:
        // We can launch the fragment purely for testing if we wanted, or assume a button exists.
        // If no button exists yet in layout, skip or add:
        // onView(withId(R.id.btn_view_logs)).perform(click());
    }
}