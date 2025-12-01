package com.example.ajilore.code;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.activities.OrganizerEntrantsActivity;
import com.example.ajilore.code.ui.events.list.InvitedEntrantsFragment;
import com.google.android.material.tabs.TabLayout;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Basic UI smoke tests for OrganizerEntrantsActivity.
 *
 * Verifies:
 *  - Toolbar and title are present.
 *  - TabLayout has 3 tabs with correct labels.
 *  - Initial fragment is InvitedEntrantsFragment.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEntrantsActivityBasicUiTests {

    @Test
    public void organizerEntrants_showsToolbarTabsAndInitialFragment() {
        try (ActivityScenario<OrganizerEntrantsActivity> scenario =
                     ActivityScenario.launch(OrganizerEntrantsActivity.class)) {

            scenario.onActivity(activity -> {
                // Toolbar
                Toolbar toolbar = activity.findViewById(R.id.toolbar);
                assertNotNull("Toolbar should not be null", toolbar);
                assertEquals("Entrant Management", toolbar.getTitle());

                // Tabs
                TabLayout tabLayout = activity.findViewById(R.id.tab_layout);
                assertNotNull("TabLayout should not be null", tabLayout);
                assertEquals("TabLayout should have 3 tabs", 3, tabLayout.getTabCount());
                assertEquals("Invited", tabLayout.getTabAt(0).getText());
                assertEquals("Cancelled", tabLayout.getTabAt(1).getText());
                assertEquals("Enrolled", tabLayout.getTabAt(2).getText());

                // Initial fragment should be InvitedEntrantsFragment
                Fragment current = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                assertNotNull("Initial fragment should not be null", current);
                assertTrue("Initial fragment should be InvitedEntrantsFragment",
                        current instanceof InvitedEntrantsFragment);
            });
        }
    }
}
