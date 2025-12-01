package com.example.ajilore.code;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.activities.OrganizerEntrantsActivity;
import com.example.ajilore.code.ui.events.list.InvitedEntrantsFragment;
import com.google.android.material.tabs.TabLayout;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for OrganizerEntrantsActivity:
 *  - Verifies TabLayout is present and has 3 tabs.
 *  - Verifies the initial fragment is InvitedEntrantsFragment.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEntrantsActivityTests {

    @Test
    public void organizerEntrants_hasThreeTabs_andLoadsInvitedFragmentFirst() {
        try (ActivityScenario<OrganizerEntrantsActivity> scenario =
                     ActivityScenario.launch(OrganizerEntrantsActivity.class)) {

            scenario.onActivity(activity -> {
                // TabLayout should exist and have 3 tabs: Invited, Cancelled, Enrolled
                TabLayout tabLayout = activity.findViewById(R.id.tab_layout);
                assertNotNull("TabLayout should not be null", tabLayout);
                assertEquals("TabLayout should have 3 tabs", 3, tabLayout.getTabCount());

                // Initial fragment loaded into fragment_container should be InvitedEntrantsFragment
                Fragment current = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

                assertNotNull("Fragment in fragment_container should not be null", current);
                assertTrue("Initial fragment should be InvitedEntrantsFragment",
                        current instanceof InvitedEntrantsFragment);
            });
        }
    }
}
