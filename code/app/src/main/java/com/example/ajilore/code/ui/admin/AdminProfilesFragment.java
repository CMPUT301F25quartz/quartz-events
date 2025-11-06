package com.example.ajilore.code.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.List;

/**
 * Fragment for displaying and managing the list of users in the admin interface.
 *
 * <p>This fragment displays all user profiles and provides administrative controls
 * for user management, including viewing detailed user information and removing users
 * who violate application policies. Despite the class name "ProfilesFragment", this
 * component manages the user browsing functionality.</p>
 *
 * <p>Design Pattern: Fragment pattern from Android framework. Works in conjunction with
 * AdminUsersAdapter following the Model-View-Adapter pattern.</p>
 *
 * <p>User Stories:</p>
 * <ul>
 *   <li>US 03.05.01 - As an administrator, I want to be able to browse profiles</li>
 *   <li>US 03.02.01 - As an administrator, I want to be able to remove profiles</li>
 * </ul>
 *
 * <p>Note: This fragment is accessed via the "Profiles" button in the admin dashboard
 * and displays the comprehensive list of all user accounts in the system.</p>
 *
 * <p>Outstanding Issues:</p>
 * <ul>
 *   <li>Consider renaming class to AdminUsersFragment for clarity</li>
 *   <li>Implement undo functionality for accidental deletions</li>
 *   <li>Add loading states while fetching user data</li>
 * </ul>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminProfilesFragment extends Fragment implements AdminUsersAdapter.OnUserActionListener {

    private RecyclerView rvUsers;
    private AdminUsersAdapter adapter;
    private AdminController adminController;
    private EditText etSearch;
    private LinearLayout layoutEmptyState;
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
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void setupRecyclerView() {
        adapter = new AdminUsersAdapter(requireContext(), this);
        rvUsers.setAdapter(adapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSearch() {
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

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
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
                    Toast.makeText(requireContext(),
                            "Loaded " + users.size() + " users",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    updateEmptyState();
                }
            }
        });
    }

    private void showLoading(boolean show) {
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
                "User: " + user.getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(User user) {
        showDeleteDialog(user);
    }

    private void showDeleteDialog(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User?")
                .setMessage("Are you sure you want to remove \"" + user.getName() +
                        "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        adminController.removeUser(user.getUserId(), new AdminController.OperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "User deleted",
                            Toast.LENGTH_SHORT).show();
                    loadUsers(); // Reload list
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Error deleting: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}