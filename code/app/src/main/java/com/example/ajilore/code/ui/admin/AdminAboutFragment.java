package com.example.ajilore.code.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.example.ajilore.code.AdminActivity;
import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.example.ajilore.code.utils.AdminAuthManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * AdminAboutFragment
 *
 * <p>Displays the administrator dashboard landing page. This fragment shows the
 * administrator’s profile information (name + profile image) and provides
 * navigation entry points to all admin-controlled areas of the app such as:</p>
 *
 * <ul>
 *     <li>Admin Events Management</li>
 *     <li>Admin User Profiles</li>
 *     <li>Admin Image Storage</li>
 *     <li>Notification Logs (via options menu)</li>
 * </ul>
 *
 * <p>The admin profile is loaded from Firestore using the device ID as the
 * document identifier in the {@code users} collection. This fragment also
 * supports Glide loading of profile images and provides fallback UI when no
 * image is available.</p>
 *
 * <p><b>Design Pattern:</b> Navigation hub fragment for admin-privileged screens.</p>
 *
 * @author
 *     Dinma (Team Quartz)
 * @version 1.1
 * @since 2025-11-25
 */
public class AdminAboutFragment extends Fragment {

    private static final String TAG = "AdminAboutFragment";

    private TextView tvAdminName;
    private ImageView ivAdminProfile;  // NEW: Profile picture
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

        // Initialize Views
        tvAdminName = view.findViewById(R.id.tv_admin_name);
        ivAdminProfile = view.findViewById(R.id.iv_profile_image);
        ImageButton btnOptions = view.findViewById(R.id.btn_options_menu);
        btnOptions.setOnClickListener(this::showOptionsMenu);

        // Fetch and display admin name + profile picture
        fetchAdminProfile();

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
     * Shows the top-right popup menu containing admin utility actions.
     * <p>Currently includes:</p>
     * <ul>
     *     <li><b>Notification Logs</b> → navigates to {@link AdminNotificationLogsFragment}</li>
     * </ul>
     *
     * @param v The view that anchors the popup menu.
     */
    private void showOptionsMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenu().add(0, 1, 0, "Notification Logs");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                // Navigate to logs
                ((AdminActivity)requireActivity()).loadFragment(new AdminNotificationLogsFragment());
                return true;
            }
            return false;
        });
        popup.show();
    }

    /**
     * Fetches the admin's profile (name + picture) from Firestore based on device ID.
     * The device ID is used as the document ID in the 'users' collection.
     *
     * UPDATED: Now also loads profile picture using Glide.
     */
    private void fetchAdminProfile() {
        // Get the device ID
        String deviceId = AdminAuthManager.getDeviceId(requireContext());

        if (deviceId == null || deviceId.isEmpty()) {
            tvAdminName.setText("Admin User");
            setDefaultProfileImage();
            Log.e(TAG, "Device ID is null or empty");
            return;
        }

        Log.d(TAG, "Fetching admin profile for device ID: " + deviceId);

        // Set loading state
        tvAdminName.setText("Loading...");

        // Query the 'users' collection using device ID as document ID
        db.collection("users")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the name field
                        String name = documentSnapshot.getString("name");

                        // Get the profile picture (use "profilepicture" to match Firebase!)
                        String profilePicUrl = documentSnapshot.getString("profilepicture");

                        // Set name
                        if (name != null && !name.isEmpty()) {
                            tvAdminName.setText(name);
                            Log.d(TAG, "✅ Successfully loaded admin name: " + name);
                        } else {
                            tvAdminName.setText("Admin User");
                            Log.w(TAG, "Document exists but name is null");
                        }

                        // Load profile picture with Glide
                        loadProfileImage(profilePicUrl);

                    } else {
                        // Document doesn't exist for this device ID
                        tvAdminName.setText("Admin User");
                        setDefaultProfileImage();
                        Log.w(TAG, "No user document found for device ID: " + deviceId);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    tvAdminName.setText("Admin User");
                    setDefaultProfileImage();
                    Log.e(TAG, "Error fetching admin profile from Firestore", e);
                    Toast.makeText(getContext(),
                            "Failed to load admin profile",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads the profile image using Glide.
     * If URL is null or empty, shows default icon.
     *
     * @param profilePicUrl The URL of the profile picture from Firebase
     */
    private void loadProfileImage(String profilePicUrl) {
        if (profilePicUrl != null && !profilePicUrl.isEmpty() && !profilePicUrl.equals("\"\"")) {
            // Load actual profile picture
            Glide.with(this)
                    .load(profilePicUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)  // Loading placeholder
                    .error(android.R.drawable.ic_menu_myplaces)        // Error fallback
                    .circleCrop()                                       // Make it circular
                    .into(ivAdminProfile);

            Log.d(TAG, "Loaded profile picture: " + profilePicUrl);
        } else {
            // No profile picture available
            setDefaultProfileImage();
            Log.d(TAG, "No profile picture URL available");
        }
    }

    /**
     * Sets the default profile icon when no profile picture is available.
     */
    private void setDefaultProfileImage() {
        ivAdminProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
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