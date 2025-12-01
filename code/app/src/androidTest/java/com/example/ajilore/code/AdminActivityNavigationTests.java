package com.example.ajilore.code;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.ui.admin.AdminAboutFragment;
import com.example.ajilore.code.ui.admin.AdminEventsFragment;
import com.example.ajilore.code.ui.admin.AdminImagesFragment;
import com.example.ajilore.code.ui.admin.AdminProfilesFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Navigation behaviour tests for AdminActivity.
 *
 * Verifies that:
 *  - Initial fragment is AdminAboutFragment.
 *  - switchToEventsPage loads AdminEventsFragment.
 *  - switchToProfilesPage loads AdminProfilesFragment.
 *  - switchToImagesPage loads AdminImagesFragment.
 */
@RunWith(AndroidJUnit4.class)
public class AdminActivityNavigationTests {

    @Test
    public void adminActivity_loadsAboutFragmentInitially() {
        try (ActivityScenario<AdminActivity> scenario =
                     ActivityScenario.launch(AdminActivity.class)) {

            scenario.onActivity(activity -> {
                Fragment current = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.admin_container);
                assertNotNull("Initial admin fragment should not be null", current);
                assertTrue("Initial admin fragment should be AdminAboutFragment",
                        current instanceof AdminAboutFragment);
            });
        }
    }

    @Test
    public void switchToEventsProfilesImages_loadsCorrectFragments() {
        try (ActivityScenario<AdminActivity> scenario =
                     ActivityScenario.launch(AdminActivity.class)) {

            scenario.onActivity(activity -> {
                // Events
                activity.switchToEventsPage();
                activity.getSupportFragmentManager().executePendingTransactions();
                Fragment current = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.admin_container);
                assertNotNull(current);
                assertTrue("After switchToEventsPage, fragment should be AdminEventsFragment",
                        current instanceof AdminEventsFragment);

                // Profiles
                activity.switchToProfilesPage();
                activity.getSupportFragmentManager().executePendingTransactions();
                current = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.admin_container);
                assertNotNull(current);
                assertTrue("After switchToProfilesPage, fragment should be AdminProfilesFragment",
                        current instanceof AdminProfilesFragment);

                // Images
                activity.switchToImagesPage();
                activity.getSupportFragmentManager().executePendingTransactions();
                current = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.admin_container);
                assertNotNull(current);
                assertTrue("After switchToImagesPage, fragment should be AdminImagesFragment",
                        current instanceof AdminImagesFragment);
            });
        }
    }
}
