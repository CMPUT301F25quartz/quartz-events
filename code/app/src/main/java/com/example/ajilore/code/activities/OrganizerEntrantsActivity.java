package com.example.ajilore.code.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.list.CancelledEntrantsFragment;
import com.example.ajilore.code.ui.events.list.EnrolledEntrantsFragment;
import com.example.ajilore.code.ui.events.list.InvitedEntrantsFragment;
import com.google.android.material.tabs.TabLayout;

/**
 * Activity for organizers to manage entrants of a specific event.
 *
 * <p>This screen provides a tab-based interface allowing the organizer
 * to browse three entrant categories:</p>
 *
 * <ul>
 *     <li><b>Invited</b> — Entrants who were invited but have not accepted yet.</li>
 *     <li><b>Cancelled</b> — Entrants who declined or were removed from the event.</li>
 *     <li><b>Enrolled</b> — Entrants who accepted and are confirmed for the event.</li>
 * </ul>
 *
 * <p>The activity uses a {@link TabLayout} combined with fragment swapping
 * to display each entrant list. Fragments include:</p>
 *
 * <ul>
 *     <li>{@link InvitedEntrantsFragment}</li>
 *     <li>{@link CancelledEntrantsFragment}</li>
 *     <li>{@link EnrolledEntrantsFragment}</li>
 * </ul>
 *
 * <p>Design Pattern: Activity as a container + TabLayout controller.</p>
 */
public class OrganizerEntrantsActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private Toolbar toolbar;

    /**
     * Called when the activity is first created.
     *
     * <p>Responsibilities:</p>
     * <ul>
     *     <li>Inflate the main layout.</li>
     *     <li>Set up the toolbar with a working back button.</li>
     *     <li>Initialize the tab layout.</li>
     *     <li>Attach the tab listener that swaps fragments.</li>
     *     <li>Load the default tab (Invited entrants).</li>
     * </ul>
     *
     * @param savedInstanceState Previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_entrants);

        // Initialize toolbar and set up back button
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set toolbar title
        toolbar.setTitle("Entrant Management");

        initViews();
        setupTabs();
        loadFragment(new InvitedEntrantsFragment());
    }

    /**
     * Handles the toolbar's "up" navigation (back arrow).
     *
     * <p>Simply delegates to {@link #onBackPressed()} to return to
     * the previous screen.</p>
     *
     * @return true to indicate the event was handled.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to previous screen
        return true;
    }

    /**
     * Initializes view references from the activity layout.
     *
     * <p>Currently binds the {@link TabLayout} used for switching
     * between entrant categories.</p>
     */
    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
    }

    /**
     * Sets up the three tabs used for managing entrants:
     * Invited, Cancelled, and Enrolled.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Adds each tab to the {@link TabLayout}.</li>
     *     <li>Registers a listener that swaps in the correct fragment
     *         on tab selection.</li>
     * </ul>
     *
     * <p>Fragment mapping:</p>
     * <ul>
     *     <li>Tab 0 → {@link InvitedEntrantsFragment}</li>
     *     <li>Tab 1 → {@link CancelledEntrantsFragment}</li>
     *     <li>Tab 2 → {@link EnrolledEntrantsFragment}</li>
     * </ul>
     */
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Invited"));
        tabLayout.addTab(tabLayout.newTab().setText("Cancelled"));
        tabLayout.addTab(tabLayout.newTab().setText("Enrolled"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new InvitedEntrantsFragment();
                        break;
                    case 1:
                        fragment = new CancelledEntrantsFragment();
                        break;
                    case 2:
                        fragment = new EnrolledEntrantsFragment();
                        break;
                    default:
                        fragment = new InvitedEntrantsFragment();
                        break;
                }
                loadFragment(fragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Replaces the fragment currently displayed inside the container.
     *
     * <p>Called whenever a tab is selected to load the correct
     * entrant list fragment.</p>
     *
     * @param fragment The fragment instance to display.
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}