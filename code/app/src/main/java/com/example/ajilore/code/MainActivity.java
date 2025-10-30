package com.example.ajilore.code;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.ui.events.EntrantEventsFragment;
import com.example.ajilore.code.ui.events.EventsFragment;
import com.example.ajilore.code.ui.events.ManageEventsFragment;
import com.example.ajilore.code.ui.events.OrganizerEventsFragment;
import com.example.ajilore.code.ui.history.HistoryFragment;
import com.example.ajilore.code.ui.inbox.InboxFragment;
import com.example.ajilore.code.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        // after setting up Firebase Auth set up (divine)
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        String userRole = "entrant"; // Default; replace with actual role fetch
//        if (currentUser != null) {
//            // Fetch role from Firestore (e.g., users/{userId}/role)
//            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
//                    .get()
//                    .addOnSuccessListener(doc -> {
//                        userRole = doc.getString("role"); // e.g., "entrant", "organizer", "admin"
//                        loadDefaultFragment(userRole);
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e("MainActivity", "Failed to fetch user role: " + e.getMessage());
//                        loadDefaultFragment("entrant"); // Fallback
//                    });
//        } else {
//            // No user logged in; redirect to login or default to entrant
//            loadDefaultFragment("entrant");
//        }
//        private void loadDefaultFragment(String role) {
//            Fragment defaultFragment;
//            switch (role) {
//                case "organizer":
//                    defaultFragment = new OrganizerEventsFragment();
//                    break;
//                case "admin":
//                    defaultFragment = new ManageEventsFragment(); // Assuming you have this
//                    break;
//                default: // "entrant"
//                    defaultFragment = new EntrantEventsFragment();
//                    break;
//            }
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.nav_host_fragment, defaultFragment)
//                    .commit();
//        }
        setContentView(R.layout.activity_main);

        // Apply window insets (should be right after setContentView)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup bottom navigation
        setupBottomNavigation();

        // Load default fragment on startup
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new OrganizerEventsFragment())
                    .commit();
        }

        // Test Firebase connection
        testFirebaseConnection();
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
                } else if (id == R.id.eventsFragment) {
                    selectedFragment = new OrganizerEventsFragment();
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