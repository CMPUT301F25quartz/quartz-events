package com.example.ajilore.code.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.AdminActivity;
import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.admin.AdminAboutFragment;
import com.example.ajilore.code.ui.events.OrganizerEventsFragment;
import com.example.ajilore.code.utils.AdminAuthManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;

/**
 * {@code ProfileFragment} displays and manages a user's profile information
 * <p>
 *     It allows users to:
 *     <ul>
 *         <li>View their profile information including name, email and phone number</li>
 *         <li>Edit their profile information</li>
 *         <li>Delete their account</li>
 *         <li>switch to organizer or admin mode if allowed</li>
 *         <li>sign out of their account</li>
 *     </ul>
 * </p>
 * <p>
 *     The fragment also interacts with Firebase Authentication and Firestore to handle user identity and data persistence.
 * </p><b>Outstanding issues:</b>
 * <ul>
 *     <li> There are no profile pictures for the users</li>
 *     <li> When you go to the profile page as an existing user, your profile information is not displayed properly</li>
 * </ul>
 *
 * @author
 * Temi Akindele
 */

public class ProfileFragment extends Fragment {

    // UI
    private MaterialToolbar toolbar;
    private ImageView imgProfile;
    private TextView tvName, tvEmail, tvPhone, tvProfileHeader;
    private MaterialButton btnEditProfile, btnDeleteProfile;
    private BottomNavigationView bottomNavigationView;

    //firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;
    /** Default constructor required for Fragment instantiation. */
    public ProfileFragment() { }

    /**
     * Inflates the layout for the ProfileFragment.
     *
     * @param inflater  LayoutInflater used to inflate XML layout
     * @param container Parent view group
     * @param savedInstanceState Previously saved instance state (if any)
     * @return The root view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Initializes Firebase, binds UI elements, and loads user profile information.
     *
     * @param v Root view after layout inflation
     * @param savedInstanceState Previously saved state (if any)
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        AdminAuthManager.addCurrentDeviceAsAdmin(requireContext());

        // Bind views
        toolbar        = v.findViewById(R.id.profileToolbar);
        imgProfile     = v.findViewById(R.id.imgProfile);
        tvName         = v.findViewById(R.id.tvName);
        tvEmail        = v.findViewById(R.id.tvEmail);
        tvPhone        = v.findViewById(R.id.tvPhone);
        btnEditProfile = v.findViewById(R.id.btnEditProfile);
        btnDeleteProfile = v.findViewById(R.id.btnDeleteProfile);
        tvProfileHeader = v.findViewById(R.id.tvProfileHeader);

        bottomNavigationView = requireActivity().findViewById(R.id.menu_bottom_nav);



        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser current = auth.getCurrentUser();
        uid = (current != null) ? current.getUid() : null;
        //three dots
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        btnEditProfile.setOnClickListener(view -> openEditProfile());

        // Delete Profile: confirm first
        btnDeleteProfile.setOnClickListener(view -> confirmDelete());

        loadProfile();
    }

    /** Refreshes profile information whenever the fragment becomes visible again. */
    @Override
    public void onResume() {
        super.onResume();
        // Refresh when coming back from Edit
        loadProfile();
    }

    /**
     * Handles toolbar (3-dot) menu item clicks.
     *
     * @param item Menu item selected by the user
     * @return true if handled successfully, false otherwise
     */
    private boolean onMenuItemClick(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_switch_organizer) {
            Toast.makeText(getContext(), "Switch to Organizer", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment,
                            new OrganizerEventsFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_switch_admin) {
            if (AdminAuthManager.isAdmin(requireContext())) {
                Intent intent = new Intent(requireActivity(), AdminActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Access Denied: Admin privileges required", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_sign_out) {
            Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new LoginFragment())
                    .commit();
            return true;
        }
        return false;
    }
    /**
     * Loads user profile information from Firestore and updates the UI.
     * <p>
     * Displays default placeholders if user data is unavailable.
     */
    private void loadProfile() {
        var user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvProfileHeader.setText("Hi User");
            tvName.setText("Name: —");
            tvEmail.setText("Email: —");
            tvPhone.setText("Phone: —");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String name  = doc.getString("name");
                    String email = doc.getString("email");
                    String phone = doc.getString("phone");

                    tvProfileHeader.setText((name != null && !name.isEmpty()) ? "Hi " + name : "Hi User");
                    tvName.setText("Name: " + (name == null || name.isEmpty() ? "—" : name));
                    tvEmail.setText("Email: " + (email == null || email.isEmpty() ? "—" : email));
                    tvPhone.setText("Phone: " + (phone == null || phone.isEmpty() ? "—" : phone));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /**
     * Opens the Edit Profile screen by replacing the current fragment.
     */
    private void openEditProfile(){
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new EditProfileFragment())
                .addToBackStack(null)
                .commit();
    }
    /**
     * Displays a confirmation dialog before permanently deleting the user's profile.
     */
    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("This will remove your account data. Are you sure?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> performDelete())
                .show();
    }
    /**
     * Deletes the user’s profile data from Firestore and Firebase Authentication.
     * <p>
     * If deletion fails due to authentication constraints, the user is signed out instead.
     */
    private void performDelete() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            Toast.makeText(getContext(), "Not signed in.", Toast.LENGTH_LONG).show();
            return;
        }
        uid = current.getUid();

        // 1) Read user doc to get nameLower for secondary index
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String nameLower = doc.getString("nameLower");

                    // 2) Batch delete Firestore docs
                    var batch = db.batch();
                    batch.delete(db.collection("users").document(uid));
                    if (nameLower != null && !nameLower.trim().isEmpty()) {
                        batch.delete(db.collection("usersByName").document(nameLower));
                    }

                    batch.commit()
                            .addOnSuccessListener(x -> {
                                // 3) Delete Auth user (falls back to signOut if reauth is required)
                                current.delete()
                                        .addOnSuccessListener(v -> {
                                            Toast.makeText(getContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
                                            navigateToLogin();
                                        })
                                        .addOnFailureListener(err -> {
                                            // If delete requires recent login or fails, just sign out
                                            FirebaseAuth.getInstance().signOut();
                                            Toast.makeText(getContext(), "Profile data deleted. Signed out.", Toast.LENGTH_SHORT).show();
                                            navigateToLogin();
                                        });
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    /**
     * Navigates the user back to the Login screen after sign-out or deletion.
     */
    private void navigateToLogin() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new LoginFragment())
                .commit();
    }



}