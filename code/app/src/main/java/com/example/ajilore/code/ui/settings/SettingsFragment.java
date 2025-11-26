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

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchNotifications;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views and Firebase
        switchNotifications = view.findViewById(R.id.switchNotifications);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Use demoUser if not signed in
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "demoUser";

        // Load current preference
        loadNotificationPreference(userId);

        // Listen for toggle changes
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationPreference(userId, isChecked);
        });

        return view;
    }

    private void loadNotificationPreference(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    Boolean enabled = null;
                    if (userDoc.exists()) {
                        enabled = userDoc.getBoolean("notificationsEnabled");
                    }

                    if (enabled != null) {
                        switchNotifications.setChecked(enabled);
                    } else {
                        // Fallback to preferences subcollection
                        db.collection("users")
                                .document(userId)
                                .collection("preferences")
                                .document("notifications")
                                .get()
                                .addOnSuccessListener(prefDoc -> {
                                    boolean prefEnabled = true;
                                    if (prefDoc.exists()) {
                                        Boolean pref = prefDoc.getBoolean("enabled");
                                        prefEnabled = pref != null ? pref : true;
                                    }
                                    switchNotifications.setChecked(prefEnabled);
                                })
                                .addOnFailureListener(e -> {
                                    switchNotifications.setChecked(true);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    switchNotifications.setChecked(true);
                });
    }

    private void updateNotificationPreference(String userId, boolean isChecked) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("notificationsEnabled", isChecked);

        db.collection("users")
                .document(userId)
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Also update preferences subcollection for backup
                    Map<String, Object> prefData = new HashMap<>();
                    prefData.put("enabled", isChecked);
                    db.collection("users")
                            .document(userId)
                            .collection("preferences")
                            .document("notifications")
                            .set(prefData);
                })
                .addOnFailureListener(e -> {
                    // If main update fails, at least update preferences
                    Map<String, Object> prefData = new HashMap<>();
                    prefData.put("enabled", isChecked);
                    db.collection("users")
                            .document(userId)
                            .collection("preferences")
                            .document("notifications")
                            .set(prefData);
                });
    }
}