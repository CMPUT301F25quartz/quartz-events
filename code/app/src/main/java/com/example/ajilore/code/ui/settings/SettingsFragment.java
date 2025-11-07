package com.example.ajilore.code.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.ajilore.code.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment for managing user settings related to notifications.
 * Allows users to toggle notification preferences which are saved in Firestore.
 */
public class SettingsFragment extends Fragment {

    private SwitchMaterial switchNotifications;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    /**
     * Called to have the fragment instantiate its user interface view.
     * Initializes the notification switch and loads/saves user preferences from Firestore.
     *
     * @param inflater LayoutInflater to inflate views
     * @param container Optional parent view
     * @param savedInstanceState Saved state bundle
     * @return The root view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views and Firebase
        switchNotifications = view.findViewById(R.id.switchNotifications);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "demoUser";

        // Load the current notification preference from Firestore
        db.collection("users")
                .document(userId)
                .collection("preferences")
                .document("notifications")
                .get()
                .addOnSuccessListener(doc -> {
                    boolean enabled = true; // default to enabled
                    if (doc.exists()) {
                        Boolean pref = doc.getBoolean("enabled");
                        enabled = pref != null ? pref : true;
                    }
                    switchNotifications.setChecked(enabled);
                });

        // Listen for toggle changes and save preference to Firestore
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Map<String, Object> data = new HashMap<>();
            data.put("enabled", isChecked);

            db.collection("users")
                    .document(userId)
                    .collection("preferences")
                    .document("notifications")
                    .set(data);
        });

        return view;
    }
}
