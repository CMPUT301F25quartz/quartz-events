package com.example.ajilore.code.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;

import com.example.ajilore.code.AdminActivity;
import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.example.ajilore.code.utils.AdminAuthManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment displaying the admin dashboard with navigation options.
 *
 * <p>This fragment serves as the main landing page for administrators,
 * displaying admin profile information and providing navigation buttons
 * to access different administrative functions (Events, Profiles, Images).</p>
 *
 * <p>Design Pattern: Fragment pattern serving as a navigation hub.</p>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminAboutFragment extends Fragment {

    private static final String TAG = "AdminAboutFragment";

    private TextView tvAdminName;
    private FirebaseFirestore db;


    /**
     * Inflates the admin dashboard layout and attaches click listeners for admin navigation buttons.
     *
     * @param inflater          The LayoutInflater used to inflate fragment views.
     * @param container         The parent view the fragment UI will be attached to.
     * @param savedInstanceState Previous saved instance state, if any.
     * @return The root View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_about, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextView
        tvAdminName = view.findViewById(R.id.tv_admin_name);

        // Fetch and display admin name
        fetchAdminName();

        setupNavigationButtons(view);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked - returning to MainActivity");

            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("navigate_to", "profile");
            intent.putExtra("show_bottom_nav", true);
            startActivity(intent);

            requireActivity().finish();
        });

        return view;
    }

    /**
     * Fetches the admin's name from Firestore based on device ID.
     * The device ID is used as the document ID in the 'users' collection.
     */
    private void fetchAdminName() {
        // Get the device ID
        String deviceId = AdminAuthManager.getDeviceId(requireContext());

        if (deviceId == null || deviceId.isEmpty()) {
            tvAdminName.setText("Admin User");
            Log.e(TAG, "Device ID is null or empty");
            return;
        }

        Log.d(TAG, "Fetching admin name for device ID: " + deviceId);

        // Set loading state
        tvAdminName.setText("Loading...");

        // Query the 'users' collection using device ID as document ID
        db.collection("users")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the name field from the document
                        String name = documentSnapshot.getString("name");

                        if (name != null && !name.isEmpty()) {
                            tvAdminName.setText(name);
                            Log.d(TAG, "Successfully loaded admin name: " + name);
                        } else {
                            // Fallback: If name is null, show "Admin User"
                            tvAdminName.setText("Admin User");
                            Log.w(TAG, "Document exists but name is null");
                        }
                    } else {
                        // Document doesn't exist for this device ID
                        tvAdminName.setText("Admin User");
                        Log.w(TAG, "No user document found for device ID: " + deviceId);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    tvAdminName.setText("Admin User");
                    Log.e(TAG, "Error fetching admin name from Firestore", e);
                    Toast.makeText(getContext(),
                            "Failed to load admin name",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Configures click listeners on navigation hub buttons, routing to corresponding admin sections.
     * @param view Root view containing all admin dashboard buttons.
     */
    private void setupNavigationButtons(View view) {
        Button btnEvents = view.findViewById(R.id.btn_events);
        Button btnProfiles = view.findViewById(R.id.btn_profiles);
        Button btnImages = view.findViewById(R.id.btn_images);

        AdminActivity activity = (AdminActivity) requireActivity();

        // CORRECTLY MAPPED:
        btnEvents.setOnClickListener(v -> activity.switchToEventsPage());
        btnProfiles.setOnClickListener(v -> activity.switchToProfilesPage()); // Shows USERS
        btnImages.setOnClickListener(v -> activity.switchToImagesPage());     // Shows IMAGES
    }
}