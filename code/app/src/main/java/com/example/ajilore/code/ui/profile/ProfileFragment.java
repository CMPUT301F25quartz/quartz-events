package com.example.ajilore.code.ui.profile;

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

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.admin.AdminAboutFragment;
import com.example.ajilore.code.ui.events.OrganizerEventsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.junit.After;

public class ProfileFragment extends Fragment {

    // UI refs from fragment_profile.xml
    private MaterialToolbar toolbar;
    private ImageView imgProfile;
    private TextView tvName, tvEmail, tvPhone;
    private MaterialButton btnEditProfile, btnDeleteProfile;

    public ProfileFragment() { /* empty */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Bind views
        toolbar        = v.findViewById(R.id.profileToolbar);
        imgProfile     = v.findViewById(R.id.imgProfile);
        tvName         = v.findViewById(R.id.tvName);
        tvEmail        = v.findViewById(R.id.tvEmail);
        tvPhone        = v.findViewById(R.id.tvPhone);
        btnEditProfile = v.findViewById(R.id.btnEditProfile);
        btnDeleteProfile = v.findViewById(R.id.btnDeleteProfile);

        // (Optional) Prefill with whatever you have available; replace with real data later
        tvName.setText("Name: John Doe");
        tvEmail.setText("Email: john@example.com");
        tvPhone.setText("Phone: —");

        // Overflow (three dots) actions
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        // Edit Profile: navigate to your edit flow or show a sheet/dialog
        btnEditProfile.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Edit Profile tapped", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to your edit screen (e.g., EditProfileFragment or existing profile editor)
            // Example:
            // requireActivity().getSupportFragmentManager().beginTransaction()
            //         .replace(R.id.nav_host_fragment, new EditProfileFragment())
            //         .addToBackStack(null)
            //         .commit();
        });

        // Delete Profile: confirm first
        btnDeleteProfile.setOnClickListener(view -> confirmDelete());
    }

    // Handle 3-dot menu clicks
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
            Toast.makeText(getContext(), "Switch to Admin", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment,
                            new AdminAboutFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_sign_out) {
            Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT).show();
            // TODO: Call FirebaseAuth.getInstance().signOut() when you wire auth,
            //navigate back to your LoginFragment
             requireActivity().getSupportFragmentManager().beginTransaction()
                   .replace(R.id.nav_host_fragment, new LoginFragment())
                    .commit();
            return true;
        }
        return false;
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("This will remove your account data. Are you sure?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> performDelete())
                .show();
    }

    // Stub: replace with real deletion (Firestore + Auth) when ready
    private void performDelete() {
        // TODO: Delete from Firestore and/or Auth here
        Toast.makeText(getContext(), "Profile deleted (stub)", Toast.LENGTH_SHORT).show();
        //After delete, send user to Login screen
        requireActivity().getSupportFragmentManager().beginTransaction()
              .replace(R.id.nav_host_fragment, new LoginFragment()).commit();
    }



}
