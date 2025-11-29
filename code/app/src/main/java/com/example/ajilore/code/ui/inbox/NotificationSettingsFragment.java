package com.example.ajilore.code.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationSettingsFragment extends Fragment {

    private Switch switchNotifications;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification_settings, container, false);

        btnBack = view.findViewById(R.id.btnBack);

        switchNotifications = view.findViewById(R.id.switchNotifications);
        db = FirebaseFirestore.getInstance();

        // NEW: Use Device ID to store preference (added by Kulnoor)
        userId = android.provider.Settings.Secure.getString(
                requireContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Cannot load notification settings", Toast.LENGTH_SHORT).show();
            return view;
        }

        // NEW: Load current toggle state from Firestore (added by Kulnoor)
        // DEFAULT TO TRUE if document doesn't exist
        db.collection("users")
                .document(userId)
                .collection("preferences")
                .document("notifications")
                .get()
                .addOnSuccessListener(doc -> {
                    boolean enabled = true; // default ON (this covers the case where the doc exists but field is missing/null)
                    if (doc.exists()) {
                        Boolean pref = doc.getBoolean("enabled");
                        enabled = pref != null ? pref : true; // Use stored value, default to true if field is null
                    } else {
                        // NEW: Document doesn't exist, set default to true and save it
                        enabled = true;
                        // Save the default value to Firestore
                        db.collection("users")
                                .document(userId)
                                .collection("preferences")
                                .document("notifications")
                                .set(java.util.Collections.singletonMap("enabled", enabled));
                    }
                    switchNotifications.setChecked(enabled); // Set the UI toggle
                });

        // NEW: Save toggle state when changed (added by Kulnoor)
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("users")
                    .document(userId)
                    .collection("preferences")
                    .document("notifications")
                    .set(
                            java.util.Collections.singletonMap("enabled", isChecked)
                    );

            Toast.makeText(
                    getContext(),
                    isChecked ? "Pop-up notifications enabled" : "Pop-up notifications disabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Set click listener for the back button
        btnBack.setOnClickListener(v -> {
            // Navigate back to the previous fragment (InboxFragment)
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack(); // This pops the current fragment from the back stack
            }
        });

        return view;
    }
}