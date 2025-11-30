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

public class OrganizerEntrantsActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private Toolbar toolbar;

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to previous screen
        return true;
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
    }

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

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}