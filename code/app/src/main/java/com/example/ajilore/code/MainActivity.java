package com.example.ajilore.code;

import android.Manifest; //  ADDED: Import for notification permission
import android.content.Intent;
import android.content.pm.PackageManager; //  ADDED: Import for permission handling
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
import androidx.core.app.ActivityCompat; // ADDED: Import for requesting permissions
import androidx.core.content.ContextCompat; //  ADDED: Import for checking permissions
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.ui.admin.AdminEventsFragment;
import com.example.ajilore.code.ui.admin.AdminProfilesFragment;
import com.example.ajilore.code.ui.events.EventDetailsFragment;
import com.example.ajilore.code.ui.events.EventsFragment;
import com.example.ajilore.code.ui.events.GeneralEventsFragment;
import com.example.ajilore.code.ui.events.OrganizerEventsFragment;
import com.example.ajilore.code.ui.history.HistoryFragment;
import com.example.ajilore.code.ui.inbox.InboxFragment;
import com.example.ajilore.code.ui.profile.LoginFragment;
import com.example.ajilore.code.ui.profile.ProfileFragment;
import com.example.ajilore.code.utils.AdminAuthManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.cloudinary.android.MediaManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private FirebaseFirestore db;
    private String userId;

    // to clean up listeners
    private ListenerRegistration registrationsListener;
    private final List<ListenerRegistration> inboxListeners = new ArrayList<>();
    // keep per-event unread counts
    private final Map<String, Integer> unreadPerEvent = new HashMap<>();



    private BottomNavigationView bottomNavigationView;
    private boolean isAdmin = false;  // NEW: Track if current user is admin

    /**
     * Called when the activity is first created.
     * Initializes Cloudinary, Firestore, navigation, ban checks, and inbox listeners.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     *                           if this is a fresh launch
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupRealtimeBanListener();

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

        db = FirebaseFirestore.getInstance();

        userId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (userId != null && !userId.isEmpty()) {
            startInboxBadgeListener();
        }

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if current device has admin privileges
        checkAdminStatus();

        // Setup bottom navigation
        setupBottomNavigation();

        // Check if we should navigate to a specific fragment
        handleNavigationIntent();

        if (savedInstanceState == null) {
            // Intercept startup to check for bans first
            checkBanStatusAndLogin();
        }
        // Load default fragment on startup if (savedInstanceState == null) { getSupportFragmentManager().beginTransaction() .replace(R.id.nav_host_fragment, new OrganizerEventsFragment()) .commit(); //highlight the correct tab in the bottom nav
        // bottomNavigationView.setSelectedItemId(R.id.generalEventsFragment);  // Test Firebase connection testFirebaseConnection();
        // Test Firebase connection (unchanged)
        testFirebaseConnection();

        // Kulnoor ADDED: Check and request notification permission
        checkNotificationPermission();
    }

    //  ADDED: Method to check and request notification permission for Android 13+
    /**
     * Checks and requests the POST_NOTIFICATIONS permission on Android 13+.
     * If the permission is not granted, a runtime permission request is issued.
     */
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

    /**
     * Hides the bottom navigation bar.
     * Used in cases where navigation should be disabled, such as when a user is banned.
     */
    public void hideBottomNav() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }


    /**
     * Opens the event details screen when a notification in the inbox is tapped.
     * Fetches the event document to retrieve the title and passes it to {@link EventDetailsFragment}.
     *
     * @param eventId the Firestore document ID of the event in the {@code org_events} collection
     */

    public void openEventDetailsFromInbox(@NonNull String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the event so we can pass the title into EventDetailsFragment
        db.collection("org_events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    String title = "";
                    if (doc != null && doc.exists()) {
                        String t = doc.getString("title");
                        if (t != null) title = t;
                    }

                    Fragment frag = EventDetailsFragment.newInstance(eventId, title);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, frag) //
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Could not open event: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }


    /**
     * Updates the inbox badge on the bottom navigation bar with the total number of unread messages.
     * If the count is zero or negative, the badge is hidden.
     *
     * @param unreadCount total number of unread, non-archived inbox messages across all events
     */

    public void updateInboxBadge(int unreadCount) {
        BottomNavigationView nav = findViewById(R.id.menu_bottom_nav); // View id
        if (nav == null) return;

        BadgeDrawable badge = nav.getOrCreateBadge(R.id.inboxFragment);

        if (unreadCount <= 0) {
            badge.clearNumber();
            badge.setVisible(false);
        } else {
            badge.setVisible(true);
            badge.setNumber(unreadCount);
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

    /**
     * Initializes and wires up the bottom navigation bar.
     * Sets the fragment that should be displayed for each bottom nav item.
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

    /**
     * Simple Firestore smoke test to confirm connectivity and that
     * the {@code org_events} collection is reachable.
     * Logs and toasts the number of events found.
     */
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

    /**
     * Starts and maintains snapshot listeners used to update the inbox badge.
     * Listens to the current user's registrations and, for each event, listens
     * to unread, non-archived inbox messages to compute the total unread count.
     */
    private void startInboxBadgeListener() {
        // Clean up previous if any
        if (registrationsListener != null) {
            registrationsListener.remove();
            registrationsListener = null;
        }
        for (ListenerRegistration l : inboxListeners) {
            l.remove();
        }
        inboxListeners.clear();
        unreadPerEvent.clear();

        registrationsListener = db.collection("users")
                .document(userId)
                .collection("registrations")
                .addSnapshotListener((regSnap, e) -> {
                    if (e != null || regSnap == null) {
                        return;
                    }

                    // Which events still exist
                    Set<String> currentEventIds = new HashSet<>();
                    for (DocumentSnapshot d : regSnap.getDocuments()) {
                        currentEventIds.add(d.getId());
                    }

                    // Remove counts for deleted registrations
                    unreadPerEvent.keySet().removeIf(id -> !currentEventIds.contains(id));

                    // Clear previous inbox listeners
                    for (ListenerRegistration l : inboxListeners) {
                        l.remove();
                    }
                    inboxListeners.clear();

                    // Attach a listener to each event's inbox
                    for (DocumentSnapshot regDoc : regSnap.getDocuments()) {
                        final String eventId = regDoc.getId();

                        ListenerRegistration inboxListener = regDoc.getReference()
                                .collection("inbox")
                                .whereEqualTo("archived", false)
                                .whereEqualTo("read", false)
                                .addSnapshotListener((inboxSnap, err) -> {
                                    if (err != null) return;

                                    int count = (inboxSnap == null) ? 0 : inboxSnap.size();
                                    unreadPerEvent.put(eventId, count);

                                    int totalUnread = 0;
                                    for (int c : unreadPerEvent.values()) {
                                        totalUnread += c;
                                    }

                                    updateInboxBadge(totalUnread);
                                });

                        inboxListeners.add(inboxListener);
                    }
                });
    }



    /**
     * SECURITY: Checks if the device is banned before allowing login.
     * This prevents removed organizers from simply creating new accounts.
     */
    private void checkBanStatusAndLogin() {
        // 1. Get the device ID
        String deviceId = com.example.ajilore.code.utils.AdminAuthManager.getDeviceId(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 2. Check "banned_users" collection
        db.collection("banned_users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // CASE 1: USER IS BANNED
                        Log.w("Auth", "Banned device attempted login: " + deviceId);
                        handleBannedUser();
                    } else {
                        // CASE 2: USER IS CLEAN -> Proceed to normal login
                        Log.d("Auth", "Device is clean. Proceeding to login.");
                        loadLoginFragment();
                    }
                })
                .addOnFailureListener(e -> {
                    // Fail safe: If check fails (network error), block or retry.
                    // For now, we allow proceed or show error.
                    Log.e("Auth", "Failed to check ban status", e);
                    Toast.makeText(this, "Connection Error: Verifying account status...", Toast.LENGTH_SHORT).show();
                    // Optional: Retry logic here
                });
    }

    /**
     * Loads the standard LoginFragment (moved from onCreate).
     */
    private void loadLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new com.example.ajilore.code.ui.profile.LoginFragment())
                .commit();
    }

    /**
     * blocks access for banned users.
     */
    private void handleBannedUser() {
        // Hide navigation to prevent bypass
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);

        // Show a strict dialog or toast
        new android.app.AlertDialog.Builder(this)
                .setTitle("Account Suspended")
                .setMessage("Your device has been banned due to policy violations. You cannot access this application.")
                .setCancelable(false) // Prevent clicking away
                .setPositiveButton("Close App", (dialog, which) -> finishAffinity())
                .show();
    }

    /**
     * WATCHDOG: Listens for changes to the current user's profile in real-time.
     * If the profile is deleted by an Admin, this detects it instantly.
     */
    private void setupRealtimeBanListener() {
        String deviceId = com.example.ajilore.code.utils.AdminAuthManager.getDeviceId(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add a listener to the USER document
        db.collection("users").document(deviceId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("Auth", "Listen failed.", e);
                return;
            }

            // If snapshot is not null but exists() is false, the document was DELETED.
            if (snapshot != null && !snapshot.exists()) {
                Log.w("Auth", "User profile disappeared. Checking for ban...");

                // Double-check the "banned_users" collection to confirm it was a ban
                db.collection("banned_users").document(deviceId).get()
                        .addOnSuccessListener(banDoc -> {
                            if (banDoc.exists()) {
                                // CONFIRMED: User was banned by Admin
                                // Run on UI thread to ensure dialog shows up
                                runOnUiThread(() -> showBanDialog());
                            }
                        });
            }
        });
    }

    /**
     * Display a strict alert dialog that blocks all interaction and closes the app.
     */
    private void showBanDialog() {
        // 1. Immediately hide the navigation bar so they can't switch tabs
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }

        // 2. Check if the activity is valid before showing dialog (prevents crashes)
        if (!isFinishing() && !isDestroyed()) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Account Removed")
                    .setMessage("Your account has been permanently removed by an administrator due to a policy violation.\n\nYou have been logged out.")
                    .setCancelable(false) // CRITICAL: Users cannot click outside to dismiss
                    .setPositiveButton("Exit App", (dialog, which) -> {
                        // 3. Close the app completely
                        finishAffinity();
                    })
                    .show();
        }
    }
}