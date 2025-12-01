package com.example.ajilore.code.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.adapters.AdminUsersAdapter;
import com.example.ajilore.code.controllers.AdminController;
import com.example.ajilore.code.models.User;
import com.example.ajilore.code.utils.DeleteDialogHelper;

import java.util.List;

/**
 * AdminProfilesFragment
 *
 * <p>Fragment responsible for browsing, searching, filtering, and managing user
 * profiles within the Admin Dashboard. This serves as the central hub for User
 * Management, integrating multiple admin flows and enforcing role-based
 * constraints.</p>
 *
 * <h3>Implements the following requirements:</h3>
 * <ul>
 *     <li><b>US 03.05.01 – Browse Profiles:</b> Displays all users with search and
 *         filter controls.</li>
 *     <li><b>US 03.02.01 – Remove Profiles:</b> Admin can delete entrant accounts.</li>
 *     <li><b>US 03.07.01 – Remove Organizers:</b> Admin can deactivate organizer
 *         accounts and automatically flag their events.</li>
 * </ul>
 *
 * <h3>Functions of this screen:</h3>
 * <ul>
 *     <li>View all users with real-time filtering</li>
 *     <li>Search by name, email, or device ID</li>
 *     <li>Filter by role (All / Entrants / Organizers)</li>
 *     <li>Tap an organizer to view created event history</li>
 *     <li>Delete entrant accounts</li>
 *     <li>Deactivate organizer accounts (special dialog + event flagging)</li>
 * </ul>
 *
 * <p><b>Design Pattern:</b> Fragment as a controller with a thin UI-only role.
 * Business operations and Firestore interaction are delegated to
 * {@link AdminController}, while {@link AdminUsersAdapter} handles display +
 * filtering.</p>
 *
 * @author
 *     Dinma (Team Quartz)
 * @version 2.0
 */

public class AdminProfilesFragment extends Fragment implements AdminUsersAdapter.OnUserActionListener {

    // UI Components
    private RecyclerView rvUsers;
    private AdminUsersAdapter adapter;
    private EditText etSearch;
    private RadioGroup rgRoleFilter;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    // Controllers & State
    private AdminController adminController;
    private String currentFilter = "All"; // Default filter state

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        initializeViews(view);
        adminController = new AdminController();

        setupRecyclerView();
        setupSearchListeners();
        setupBackButton(); // ENSURES NAVIGATION WORKS

        loadUsers();

        return view;
    }

    /**
     * Initializes all view references from the inflated layout, including the
     * RecyclerView, search bar, empty-state layout, loading indicator, back button,
     * and role filter group.
     *
     * @param view The root view returned by onCreateView().
     */

    private void initializeViews(View view) {
        rvUsers = view.findViewById(R.id.rv_users);
        etSearch = view.findViewById(R.id.et_search_users);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
        btnBack = view.findViewById(R.id.btn_back);
        rgRoleFilter = view.findViewById(R.id.rg_role_filter);
    }

    /**
     * Configures the RecyclerView used for displaying user profiles.
     *
     * <p>Initializes the {@link AdminUsersAdapter} with this fragment as its
     * callback listener, applies a vertical layout manager, and attaches the
     * adapter to the RecyclerView.</p>
     */
    private void setupRecyclerView() {
        adapter = new AdminUsersAdapter(requireContext(), this);
        rvUsers.setAdapter(adapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * Configures the top-back button to navigate out of the profile manager.
     *
     * <p>Uses the host activity's OnBackPressedDispatcher to ensure correct
     * navigation behavior regardless of the fragment stack.</p>
     */
    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Uses the Activity's onBackPressed logic to pop the fragment
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        }
    }

    /**
     * Attaches listeners for search and role filtering:
     *
     * <ul>
     *     <li><b>TextWatcher:</b> Filters the user list dynamically as the admin types.</li>
     *     <li><b>RadioGroup listener:</b> Filters the list to show All, Entrants, or Organizers.</li>
     * </ul>
     *
     * <p>Both values (text query + role filter) are combined and passed to the
     * adapter’s filtering function.</p>
     */
    private void setupSearchListeners() {
        // 1. Text Search Listener
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s.toString(), currentFilter);
                    updateEmptyState();
                }

                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // 2. Role Filter Listener (All vs Entrants vs Organizers)
        if (rgRoleFilter != null) {
            rgRoleFilter.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_all) {
                    currentFilter = "All";
                } else if (checkedId == R.id.rb_entrants) {
                    currentFilter = "Entrants";
                } else if (checkedId == R.id.rb_organizers) {
                    currentFilter = "Organizers";
                }
                // Apply both text query AND role filter
                adapter.filter(etSearch.getText().toString(), currentFilter);
                updateEmptyState();
            });
        }
    }

    /**
     * Loads the full list of users from Firestore using {@link AdminController}.
     *
     * <p>Once retrieved:</p>
     * <ul>
     *     <li>Populates the adapter</li>
     *     <li>Immediately applies the current text + role filters</li>
     *     <li>Shows or hides the empty-state view</li>
     * </ul>
     *
     * <p>Displays a loading spinner while data is being fetched.</p>
     */
    private void loadUsers() {
        showLoading(true);

        adminController.fetchAllUsers(new AdminController.DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                // Safety check: Ensure fragment is still valid before updating UI
                if (!isAdded()) return;

                showLoading(false);
                adapter.setUsers(users);

                // Re-apply current filters if any exist
                adapter.filter(etSearch.getText().toString(), currentFilter);
                updateEmptyState();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                showLoading(false);
                Toast.makeText(requireContext(),
                        "Error loading users: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Handles taps on individual user rows.
     *
     * <p>Behavior is role dependent:</p>
     * <ul>
     *     <li><b>Organizers:</b> Opens a dialog showing all events they created so the admin can
     *     review their activity before deactivation.</li>
     *     <li><b>Entrants:</b> Shows a small informational toast (view-only behavior).</li>
     * </ul>
     *
     * @param user The user the admin tapped.
     */
    @Override
    public void onUserClick(User user) {
        if ("organiser".equalsIgnoreCase(user.getRole())) {
            showOrganizerDetailsDialog(user);
        } else {
            Toast.makeText(requireContext(), "Entrant: " + user.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles delete-button clicks on user rows.
     *
     * <p>Behavior is role dependent:</p>
     * <ul>
     *     <li><b>Entrants:</b> Shows a standard delete confirmation dialog using
     *         {@link DeleteDialogHelper} and removes the account.</li>
     *     <li><b>Organizers:</b> Opens the specialized organizer-warning dialog,
     *         informing admin that events will also be flagged.</li>
     * </ul>
     *
     * @param user The user the admin wants to delete.
     */
    @Override
    public void onDeleteClick(User user) {
        if ("organiser".equalsIgnoreCase(user.getRole())) {
            showOrganizerDeleteDialog(user);
        } else {
            DeleteDialogHelper.showDeleteDialog(
                    requireContext(),
                    "Profile",
                    user.getName(),
                    () -> deleteUser(user)
            );
        }
    }

    // ================= Dialog Helpers =================

    /**
     * Displays a dialog listing all events created by an organizer.
     *
     * <p>This screen helps admins understand the organizer's activity before
     * deactivation. The dialog also includes a shortcut button for removing the
     * organizer.</p>
     *
     * @param user The organizer whose events should be displayed.
     */
    private void showOrganizerDetailsDialog(User user) {
        Toast.makeText(requireContext(), "Fetching organizer activity...", Toast.LENGTH_SHORT).show();

        adminController.getOrganizerEvents(user.getUserId(), new AdminController.DataCallback<List<com.example.ajilore.code.ui.events.model.Event>>() {
            @Override
            public void onSuccess(List<com.example.ajilore.code.ui.events.model.Event> events) {
                if (!isAdded()) return;

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Activity: " + user.getName());

                if (events.isEmpty()) {
                    builder.setMessage("No events created by this organizer.");
                } else {
                    String[] eventTitles = new String[events.size()];
                    for (int i = 0; i < events.size(); i++) {
                        eventTitles[i] = "• " + events.get(i).title;
                    }
                    builder.setItems(eventTitles, null);
                }

                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.setNegativeButton("Deactivate Account", (dialog, which) -> showOrganizerDeleteDialog(user));
                builder.show();
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to load activity", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Shows a specialized warning dialog for deleting organizers.
     * Warns that this action also flags their events.
     */
    private void showOrganizerDeleteDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Custom layout for better warning visuals
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_delete_organizer, null);

        TextView tvMessage = dialogView.findViewById(R.id.tv_message);
        tvMessage.setText("Are you sure you want to remove organizer \"" + user.getName() + "\"?\n\nThis will deactivate their account and flag all their created events.");

        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.setView(dialogView).create();

        btnDelete.setOnClickListener(v -> {
            removeOrganizer(user);
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ================= CRUD Operations =================

    /**
     * Deactivates an organizer account via Controller.
     */
    /**
     * Permanently removes an organizer account via Controller.
     * This calls the new atomic batch method that bans the ID and flags events.
     */
    private void removeOrganizer(User user) {
        adminController.removeOrganizer(user.getUserId(), new AdminController.OperationCallback() {
            @Override
            public void onSuccess() {
                // Check if fragment is still attached to avoid crash on rotation/exit
                if (!isAdded()) return;

                Toast.makeText(requireContext(), "Organizer removed & banned successfully", Toast.LENGTH_LONG).show();
                loadUsers(); // Refresh the RecyclerView to show they are gone
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Permanently deletes a standard entrant profile via Controller.
     */
    private void deleteUser(User user) {
        adminController.removeUser(user.getUserId(), new AdminController.OperationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "User deleted", Toast.LENGTH_SHORT).show();
                loadUsers(); // Refresh list
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ================= UI Helpers =================

    /**
     * Toggles the progress bar and RecyclerView visibility to indicate whether
     * data is currently being loaded.
     *
     * @param show If true, show the progress indicator; otherwise hide it.
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvUsers.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Updates the visibility of the empty-state layout depending on whether the
     * adapter currently has items to display after filtering.
     */
    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}