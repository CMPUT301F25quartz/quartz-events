package com.example.ajilore.code;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.ui.events.EventsFragment;
import com.example.ajilore.code.ui.events.ManageEventsFragment;
import com.example.ajilore.code.ui.events.OrganizerEventsFragment;
import com.example.ajilore.code.ui.history.HistoryFragment;
import com.example.ajilore.code.ui.inbox.InboxFragment;
import com.example.ajilore.code.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // divine rocks!
        //yes she does
        //Hook it up to the bottom nav view
        //NavigationUI.setupWithNavController(navView, navController);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.menu_bottom_nav);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment selected_fragment = null;

                int id = item.getItemId();

                if(id == R.id.historyFragment){
                    selected_fragment = new HistoryFragment();
                    //Toast.makeText(MainActivity.this, "History", Toast.LENGTH_SHORT).show();
                } //else if(id == R.id.eventsFragment) {
                    //selected_fragment = new EventsFragment();
                    //Toast.makeText(MainActivity.this, "Events", Toast.LENGTH_SHORT).show();
                 else if(id == R.id.inboxFragment) {
                    selected_fragment = new InboxFragment();
                    //Toast.makeText(MainActivity.this, "Inbox", Toast.LENGTH_SHORT).show();
                }
                else if(id == R.id.profileFragment) {
                    selected_fragment = new ProfileFragment();
                    //Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.eventsFragment) {
                    // temporary: always show organizer version
                    selected_fragment = new OrganizerEventsFragment();
                }
                if(selected_fragment != null){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, selected_fragment).commit();
                }
                return true;
            }
        });





    }
}