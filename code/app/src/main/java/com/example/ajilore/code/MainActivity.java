package com.example.ajilore.code;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.ajilore.code.ui.events.EventsFragment;
import com.example.ajilore.code.ui.history.HistoryFragment;
import com.example.ajilore.code.ui.inbox.InboxFragment;
import com.example.ajilore.code.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {


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

        BottomNavigationView bottomNavigationView = findViewById(R.id.menu_bottom_nav);


        //NavController  from the host fragment
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        //Hook it up to the bottom nav view
        NavigationUI.setupWithNavController(bottomNavigationView, navController);



    }
}