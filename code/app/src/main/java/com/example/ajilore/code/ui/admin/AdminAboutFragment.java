package com.example.ajilore.code.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.AdminActivity;
import com.example.ajilore.code.R;

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

        setupNavigationButtons(view);

        return view;
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