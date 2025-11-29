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
 * Fragment responsible for browsing and managing user profiles in the Admin Dashboard.
 *
 * <p>This fragment serves as the central hub for User Management, implementing the
 * following project requirements:</p>
 * <ul>
 * <li><b>US 03.05.01 (Browse Profiles):</b> Lists all users with search and role filtering.</li>
 * <li><b>US 03.02.01 (Remove Profiles):</b> Allows deletion of standard entrant profiles.</li>
 * <li><b>US 03.07.01 (Remove Organizers):</b> specialized flow to deactivate organizers and flag their events.</li>
 * </ul>
 *
 * @author Dinma (Team Quartz)
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
     * Initializes view references from the layout.
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
     * Configures the RecyclerView with the AdminUsersAdapter.
     */
    private void setupRecyclerView() {
        adapter = new AdminUsersAdapter(requireContext(), this);
        rvUsers.setAdapter(adapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * Sets up the Back Button to navigate up the stack (back to Admin Dashboard).
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
     * Configures listeners for the Search Bar (TextWatcher) and Filter Tabs (RadioGroup).
     * Both listeners trigger the adapter's filter method to update the list live.
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
     * Fetches the complete list of users from Firestore.
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
     * Handles clicks on user rows.
     * If the user is an Organizer, we show their event activity (Requirement).
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
     * Handles delete button clicks.
     * Routes to different logic based on role:
     * - Organizers get the Deactivation flow (US 03.07.01)
     * - Entrants get the standard Delete flow (US 03.02.01)
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
     * Displays a dialog listing the events created by an organizer.
     * Allows Admin to inspect activity before deciding to deactivate.
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
            deactivateOrganizer(user);
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ================= CRUD Operations =================

    /**
     * Deactivates an organizer account via Controller.
     */
    private void deactivateOrganizer(User user) {
        adminController.deactivateOrganizer(user.getUserId(), new AdminController.OperationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Organizer deactivated", Toast.LENGTH_LONG).show();
                loadUsers(); // Refresh list
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

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvUsers.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}