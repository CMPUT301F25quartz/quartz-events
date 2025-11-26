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
 * Fragment for displaying and managing the list of users in the admin interface.
 *
 * Implements:
 * - US 03.05.01: Browse profiles
 * - US 03.02.01: Remove profiles
 * - US 03.07.01: Remove organizers that violate policy
 *
 * @author Dinma (Team Quartz)
 * @version 1.3
 * @since 2025-11-25
 */
public class AdminProfilesFragment extends Fragment implements AdminUsersAdapter.OnUserActionListener {

    private RecyclerView rvUsers;
    private AdminUsersAdapter adapter;
    private AdminController adminController;
    private EditText etSearch;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        initializeViews(view);
        adminController = new AdminController();
        setupRecyclerView();
        setupSearch();
        setupBackButton();
        loadUsers();

        return view;
    }

    private void initializeViews(View view) {
        rvUsers = view.findViewById(R.id.rv_users);
        etSearch = view.findViewById(R.id.et_search_users);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void setupRecyclerView() {
        adapter = new AdminUsersAdapter(requireContext(), this);
        rvUsers.setAdapter(adapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s.toString());
                    updateEmptyState();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    private void loadUsers() {
        showLoading(true);

        adminController.fetchAllUsers(new AdminController.DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (isAdded()) {
                    showLoading(false);
                    adapter.setUsers(users);
                    updateEmptyState();

                    String message = users.isEmpty() ?
                            "No users found" :
                            "Loaded " + users.size() + " users";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            "Error loading users: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    updateEmptyState();
                }
            }
        });
    }

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

    @Override
    public void onUserClick(User user) {
        Toast.makeText(requireContext(),
                "User: " + user.getName() + " (" + user.getRole() + ")",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows role-specific delete dialog.
     * Organizers get enhanced confirmation, entrants get simple confirmation.
     *
     * US 03.07.01 - Remove organizers
     * US 03.02.01 - Remove profiles
     */
    @Override
    public void onDeleteClick(User user) {
        // Check for "organiser" (British spelling in your Firebase)
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

    /**
     * Shows enhanced delete confirmation for organizers with consequences listed.
     * US 03.07.01 - Remove organizers that violate policy
     */
    private void showOrganizerDeleteDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_delete_organizer, null);

        TextView tvMessage = dialogView.findViewById(R.id.tv_message);
        tvMessage.setText("Are you sure you want to remove organizer \"" + user.getName() + "\"?");

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

    /**
     * Deactivates organizer account and flags all their events.
     * US 03.07.01 - Remove organizers that violate policy
     */
    private void deactivateOrganizer(User user) {
        Toast.makeText(requireContext(),
                "Deactivating organizer...",
                Toast.LENGTH_SHORT).show();

        adminController.deactivateOrganizer(user.getUserId(), new AdminController.OperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Organizer deactivated\nAll events flagged",
                            Toast.LENGTH_LONG).show();
                    loadUsers();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Error deactivating organizer:\n" + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Permanently deletes a user profile.
     * US 03.02.01 - Remove profiles
     */
    private void deleteUser(User user) {
        adminController.removeUser(user.getUserId(), new AdminController.OperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "User deleted",
                            Toast.LENGTH_SHORT).show();
                    loadUsers();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Error deleting user:\n" + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}