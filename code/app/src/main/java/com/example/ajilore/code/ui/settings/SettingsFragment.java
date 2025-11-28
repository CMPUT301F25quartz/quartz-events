package com.example.ajilore.code.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Import for Toast
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ajilore.code.utils.AdminAuthManager; // Import AdminAuthManager
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.ajilore.code.R;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchNotifications;
    private FirebaseAuth auth; // Initialize for potential use
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchNotifications = view.findViewById(R.id.switchNotifications);
        auth = FirebaseAuth.getInstance(); // Initialize for potential use
        db = FirebaseFirestore.getInstance();

        // Use device ID instead of "demoUser"
        String userId = AdminAuthManager.getDeviceId(requireContext()); // Use device ID
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Cannot load settings: Device ID unavailable", Toast.LENGTH_SHORT).show();
            return view; // Return early if device ID is not available
        }

        // Load current preference from Firestore using device ID
        db.collection("users")
                .document(userId)
                .collection("preferences")
                .document("notifications")
                .get()
                .addOnSuccessListener(doc -> {
                    boolean enabled = true; // default
                    if (doc.exists()) {
                        Boolean pref = doc.getBoolean("enabled");
                        enabled = pref != null ? pref : true;
                    }
                    switchNotifications.setChecked(enabled);
                });

        // Listen for toggle changes - update using device ID
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Map<String, Object> data = new HashMap<>();
            data.put("enabled", isChecked);
            db.collection("users")
                    .document(userId) // Use actual device ID
                    .collection("preferences")
                    .document("notifications")
                    .set(data);
        });

        return view;
    }
}