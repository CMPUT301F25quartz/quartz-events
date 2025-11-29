package com.example.ajilore.code;

import android.Manifest; // Kulnoor ADDED: Import for notification permission
import android.content.Intent;
import android.content.pm.PackageManager; // Kulnoor ADDED: Import for permission handling
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // Kulnoor ADDED: Import for requesting permissions
import androidx.core.content.ContextCompat; // Kulnoor ADDED: Import for checking permissions
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.ui.admin.AdminEventsFragment;
import com.example.ajilore.code.ui.admin.AdminProfilesFragment;
import com.example.ajilore.code.ui.events.EntrantEventsFragment;
import com.example.ajilore.code.ui.events.EventsFragment;
import com.example.ajilore.code.ui.events.GeneralEventsFragment;
import com.example.ajilore.code.ui.events.OrganizerEventsFragment;
import com.example.ajilore.code.ui.history.HistoryFragment;
import com.example.ajilore.code.ui.inbox.InboxFragment;
import com.example.ajilore.code.ui.profile.LoginFragment;
import com.example.ajilore.code.ui.profile.ProfileFragment;
import com.example.ajilore.code.utils.AdminAuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

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

        //Setting up the Cloudinary connection
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dswduwd5v");
        config.put("api_key","494611986897794");
        config.put("api_secret","dIx5IJLF94eA5Cqcoo8g90IvaA8");
        // Prevent reinitialization crash
        try {
            MediaManager.init(this, config);
            Log.d("MainActivity", "MediaManager initialized successfully");
        } catch (IllegalStateException e) {
            Log.d("MainActivity", "MediaManager already initialized");
            // Already initialized, no action needed
        }

        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.menu_bottom_nav);
        bottomNavigationView.setVisibility(View.GONE);

        //hide the nav bar
        //findViewById(R.id.menu_bottom_nav).setVisibility(View.GONE);

        // Apply window insets (should be right after setContentView)
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

        // Check if we should navigate to a specific fragment
        handleNavigationIntent();

        // Load default fragment on startup (unchanged)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new LoginFragment())
                    .commit();
        }
        // Load default fragment on startup if (savedInstanceState == null) { getSupportFragmentManager().beginTransaction() .replace(R.id.nav_host_fragment, new OrganizerEventsFragment()) .commit(); //highlight the correct tab in the bottom nav
        // bottomNavigationView.setSelectedItemId(R.id.generalEventsFragment);  // Test Firebase connection testFirebaseConnection();
        // Test Firebase connection (unchanged)
        testFirebaseConnection();

        // Kulnoor ADDED: Check and request notification permission
        checkNotificationPermission();
    }

    // Kulnoor ADDED: Method to check and request notification permission for Android 13+
    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    /**
     * Handle navigation intents from other activities
     */
    private void handleNavigationIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Handle explicit bottom nav show request
            if (intent.getBooleanExtra("show_bottom_nav", false)) {
                if (bottomNavigationView != null) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    Log.d("MainActivity", "Bottom nav shown via intent flag");
                }
            }

            // Handle navigation destination
            if (intent.hasExtra("navigate_to")) {
                String destination = intent.getStringExtra("navigate_to");

                if ("profile".equals(destination)) {
                    // Ensure bottom nav is visible
                    if (bottomNavigationView != null) {
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    }

                    // Navigate to profile fragment
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new ProfileFragment())
                            .commit();

                    // Highlight profile tab in bottom nav
                    if (bottomNavigationView != null) {
                        bottomNavigationView.setSelectedItemId(R.id.profileFragment);
                    }

                    Log.d("MainActivity", "Navigated to ProfileFragment with bottom nav visible");
                }
            }
        }
    }

    /**
     * Handle new intents when activity is reused (FLAG_ACTIVITY_SINGLE_TOP)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);  // Update the intent
        handleNavigationIntent();  // Process the new intent
        Log.d("MainActivity", "onNewIntent called - processing navigation");
    }

    /**
     * Show the bottom navigation bar
     */
    public void showBottomNav() {
        if (bottomNavigationView != null){
            bottomNavigationView.setVisibility(View.VISIBLE);
            //set default tab to events
            bottomNavigationView.setSelectedItemId(R.id.generalEventsFragment);
        }
    }

    public void hideBottomNav() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
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
        Log.d("ADMIN_CHECK", "Checking Admin Status for Device ID: " + deviceId);

        // Verify against the hardcoded allowlist
        isAdmin = AdminAuthManager.isAdmin(this);
        Log.d("ADMIN_CHECK", "Admin Status: " + isAdmin);

        // Show notification if admin mode is active
        if (isAdmin) {
            Toast.makeText(this, "Administrator Access Granted", Toast.LENGTH_LONG).show();
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
            Log.d("ADMIN_MENU", " Admin menu inflated");
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

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.menu_bottom_nav);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.historyFragment) {
                    selectedFragment = new HistoryFragment();

                    //to be modified, keep as organizer for now
                } else if (id == R.id.generalEventsFragment) {
                    selectedFragment = new GeneralEventsFragment();
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
//    after setting up Firebase Auth set up (divine)
//    private void setupBottomNavigation(String userRole) { // Add userRole param
//        bottomNavigationView = findViewById(R.id.menu_bottom_nav);
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            Fragment selectedFragment = null;
//            int id = item.getItemId();
//            if (id == R.id.historyFragment) {
//                selectedFragment = new HistoryFragment();
//            } else if (id == R.id.eventsFragment) {
//                // Conditionally load based on role
//                if ("organizer".equals(userRole)) {
//                    selectedFragment = new OrganizerEventsFragment();
//                } else if ("entrant".equals(userRole)) {
//                    selectedFragment = new EntrantEventsFragment();
//                } else {
//                    selectedFragment = new ManageEventsFragment(); // Admin
//                }
//            } else if (id == R.id.inboxFragment) {
//                selectedFragment = new InboxFragment();
//            } else if (id == R.id.profileFragment) {
//                selectedFragment = new ProfileFragment();
//            }
//            if (selectedFragment != null) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.nav_host_fragment, selectedFragment)
//                        .commit();
//            }
//            return true;
//        });
//    }

    private void testFirebaseConnection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Changed it so that it will query the right document

        db.collection("org_events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();

                    Log.d("Firebase", " SUCCESS! Connected to Firestore");
                    Log.d("Firebase", "Found " + count + " events");

                    // Log each event
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        Log.d("Firebase", "Event: " + title);
                        Log.d("EventsCheck", "Event ID: " +doc.getId());
                    }

                    Toast.makeText(this,
                            "Firebase connected! Found " + count + " events",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", " FAILED: " + e.getMessage());

                    Toast.makeText(this,
                            "Firebase error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}