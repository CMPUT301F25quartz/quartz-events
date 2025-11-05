package com.example.ajilore.code;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.ui.admin.AdminEventsFragment;
import com.example.ajilore.code.ui.admin.AdminProfilesFragment;
import com.example.ajilore.code.ui.events.EventsFragment;
import com.example.ajilore.code.ui.history.HistoryFragment;
import com.example.ajilore.code.ui.inbox.InboxFragment;
import com.example.ajilore.code.ui.profile.ProfileFragment;
import com.example.ajilore.code.utils.AdminAuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * MainActivity - The main entry point of the application.
 * Handles bottom navigation for regular users and admin menu access for admin users.
 *
 * NEW FEATURES ADDED:
 * - Admin role detection based on device ID
 * - Admin menu in ActionBar (3-dot menu)
 * - Navigation to admin browse screens
 */
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private boolean isAdmin = false;  // NEW: Track if current user is admin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // NEW: Check if current device has admin privileges
        checkAdminStatus();

        // Setup bottom navigation (unchanged)
        setupBottomNavigation();

        // Load default fragment on startup (unchanged)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new EventsFragment())
                    .commit();
        }

        // Test Firebase connection (unchanged)
        testFirebaseConnection();
    }

    /**
     * Check if the current device is an admin device
     * This method:
     * 1. Gets the unique device ID
     * 2. Temporarily grants admin access (for testing only)
     * 3. Checks if device is in the admin list
     * 4. Shows a toast notification if admin mode is enabled
     * 5. Triggers menu recreation to show/hide admin options
     */
    private void checkAdminStatus() {
        // Get this device's unique ID
        String deviceId = AdminAuthManager.getDeviceId(this);
        Log.d("ADMIN_CHECK", "📱 Device ID: " + deviceId);

        // TEMPORARY: Auto-grant admin access for testing
        // TODO: Remove this line in production! Admins should be pre-configured.
        AdminAuthManager.addCurrentDeviceAsAdmin(this);

        // Check if this device is in the admin list
        isAdmin = AdminAuthManager.isAdmin(this);
        Log.d("ADMIN_CHECK", "Admin Status: " + isAdmin);

        // Show notification if admin mode is active
        if (isAdmin) {
            Toast.makeText(this, "👑 Admin Mode Enabled", Toast.LENGTH_LONG).show();
            // Recreate options menu to show admin items
            invalidateOptionsMenu();
        }
    }

    /**
     * Inflate the options menu (3-dot menu in ActionBar)
     * This method is called by Android to create the menu.
     * If the user is an admin, we inflate the admin menu with admin options.
     * If not admin, no menu is shown.
     *
     * @param menu The menu to inflate into
     * @return true if menu was created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show menu if user is admin
        if (isAdmin) {
            getMenuInflater().inflate(R.menu.menu_admin, menu);
            Log.d("ADMIN_MENU", "✅ Admin menu inflated");
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle menu item selections
     * This is called when user taps an item in the options menu (3-dot menu).
     *
     * Handles:
     * - "Admin: Browse Events" → Opens AdminEventsFragment
     * - "Admin: Browse Users" → Opens AdminProfilesFragment
     *
     * @param item The menu item that was selected
     * @return true if the item selection was handled
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_admin_events) {
            // Navigate to admin events browsing screen
            navigateToAdminFragment(new AdminEventsFragment(), "Admin Events");
            return true;

        } else if (id == R.id.action_admin_users) {
            // Navigate to admin users browsing screen
            navigateToAdminFragment(new AdminProfilesFragment(), "Admin Users");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Navigate to an admin fragment
     * This replaces the current fragment with an admin fragment
     * and adds it to the back stack so user can press back to return.
     *
     * @param fragment The admin fragment to navigate to
     * @param tag A tag to identify this fragment (for debugging)
     */
    private void navigateToAdminFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(tag)  // Add to back stack so back button works
                .commit();

        Log.d("NAVIGATION", "Navigated to: " + tag);
    }

    /**
     * Setup bottom navigation for regular user features
     * Handles navigation between: Events, History, Inbox, Profile
     */
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.menu_bottom_nav);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.historyFragment) {
                    selectedFragment = new HistoryFragment();
                } else if (id == R.id.eventsFragment) {
                    selectedFragment = new EventsFragment();
                } else if (id == R.id.inboxFragment) {
                    selectedFragment = new InboxFragment();
                } else if (id == R.id.profileFragment) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, selectedFragment)
                            .commit();
                }
                return true;
            }
        });
    }

    /**
     * Test Firebase connection on app startup
     * Fetches events collection to verify Firestore is working
     */
    private void testFirebaseConnection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();

                    Log.d("Firebase", "✅ SUCCESS! Connected to Firestore");
                    Log.d("Firebase", "Found " + count + " events");

                    // Log each event
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        Log.d("Firebase", "Event: " + title);
                    }

                    Toast.makeText(this,
                            "✅ Firebase connected! Found " + count + " events",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "❌ FAILED: " + e.getMessage());

                    Toast.makeText(this,
                            "❌ Firebase error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}