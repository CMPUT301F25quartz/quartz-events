package com.example.ajilore.code;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.activities.OrganizerEntrantsActivity;
import com.example.ajilore.code.ui.events.list.CancelledEntrantsFragment;
import com.example.ajilore.code.ui.events.list.EnrolledEntrantsFragment;
import com.google.android.material.tabs.TabLayout;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tab switching behaviour tests for OrganizerEntrantsActivity.
 *
 * Verifies that selecting each tab swaps in the correct fragment.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEntrantsActivityTabSwitchTests {

    @Test
    public void selectingCancelledTab_showsCancelledEntrantsFragment() {
        try (ActivityScenario<OrganizerEntrantsActivity> scenario =
                     ActivityScenario.launch(OrganizerEntrantsActivity.class)) {

            scenario.onActivity(activity -> {
                TabLayout tabLayout = activity.findViewById(R.id.tab_layout);
                assertNotNull("TabLayout should not be null", tabLayout);

                // Select "Cancelled" tab (index 1)
                TabLayout.Tab cancelledTab = tabLayout.getTabAt(1);
                assertNotNull("Cancelled tab should exist", cancelledTab);
                cancelledTab.select();

                // Let fragment transaction complete
                activity.getSupportFragmentManager().executePendingTransactions();

                Fragment current = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                assertNotNull("Fragment after selecting Cancelled tab should not be null", current);
                assertTrue("Fragment should be CancelledEntrantsFragment",
                        current instanceof CancelledEntrantsFragment);
            });
        }
    }
}

