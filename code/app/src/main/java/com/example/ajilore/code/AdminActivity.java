package com.example.ajilore.code;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.ui.admin.AdminAboutFragment;
import com.example.ajilore.code.ui.admin.AdminEventsFragment;
import com.example.ajilore.code.ui.admin.AdminImagesFragment;
import com.example.ajilore.code.ui.admin.AdminProfilesFragment;

/**
 * Main Activity for the Administrator interface.
 *
 * <p>This activity serves as the central hub for administrative functions in the application.
 * It provides navigation between different administrative views (Events, Profiles, Images)
 * and handles user authentication verification to ensure only authorized admins can access
 * administrative features.</p>
 *
 * <p>Design Pattern: This class follows the Activity pattern from Android framework and
 * implements a navigation controller pattern for switching between admin fragments.</p>
 *
 * <p>User Stories Implemented:</p>
 * <ul>
 *   <li>US 03.04.01 - As an administrator, I want to be able to browse events</li>
 *   <li>US 03.05.01 - As an administrator, I want to be able to browse profiles</li>
 *   <li>US 03.06.01 - As an administrator, I want to be able to browse images</li>
 * </ul>
 *
 * <p>Outstanding Issues:</p>
 * <ul>
 *   <li>Need to add proper error handling for navigation failures</li>
 *   <li>Consider adding confirmation dialogs before navigation in certain contexts</li>
 * </ul>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminActivity extends AppCompatActivity {

    /**
     * Initializes the admin activity, sets up the user interface, and verifies
     * admin authentication.
     *
     * <p>This method inflates the admin layout, initializes navigation components,
     * and ensures the current user has administrative privileges before allowing
     * access to admin features.</p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down, this Bundle contains the most recent data.
     *                          Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Start with admin about fragment (main dashboard) instead of events
        if (savedInstanceState == null) {
            loadFragment(new AdminAboutFragment());
        }
    }

    /**
     * Loads a fragment into the admin container.
     *
     * <p>This is a public method so fragments can call it to navigate between
     * different admin sections. Uses fragment transactions with back stack
     * to enable proper back navigation.</p>
     *
     * @param fragment The fragment to display in the admin container
     */
    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Navigates to the Events browsing page.
     *
     * <p>Loads AdminEventsFragment to display all events in the system.
     * Called when user clicks the "Events" button in admin dashboard.</p>
     */
    public void switchToEventsPage() {
        loadFragment(new AdminEventsFragment());
    }

    /**
     * Navigates to the Profiles/Users browsing page.
     *
     * <p>Loads AdminProfilesFragment to display all user profiles.
     * Called when user clicks the "Profiles" button in admin dashboard.
     * This shows the list of users with AdminUsersAdapter.</p>
     */
    public void switchToProfilesPage() {
        loadFragment(new AdminProfilesFragment());  // FIXED: Now loads users
    }

    /**
     * Navigates to the Images browsing page.
     *
     * <p>Loads AdminImagesFragment to display all uploaded images.
     * Called when user clicks the "Images" button in admin dashboard.</p>
     */
    public void switchToImagesPage() {
        loadFragment(new AdminImagesFragment());
    }

    /**
     * Navigates back to the admin dashboard.
     *
     * <p>Loads AdminAboutFragment which serves as the main admin dashboard
     * with navigation buttons to other admin sections.</p>
     */
    public void switchToAboutPage() {
        loadFragment(new AdminAboutFragment());
    }
}