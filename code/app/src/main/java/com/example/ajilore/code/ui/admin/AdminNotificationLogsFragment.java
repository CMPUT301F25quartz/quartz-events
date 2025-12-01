package com.example.ajilore.code.ui.admin;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.adapters.AdminLogsAdapter;
import com.example.ajilore.code.controllers.AdminController;
import com.example.ajilore.code.models.NotificationLog;

import java.util.List;

/**
 * Fragment for displaying notification logs sent by organizers.
 *
 * Implements US 03.08.01 - Admin can review logs of all notifications
 *
 * Features:
 * - View all notification logs (timestamp, sender, audience, message)
 * - Search by event ID or message content
 * - Filter by audience type (waiting, selected, chosen, cancelled, all)
 * - Logs are read-only (cannot be modified or deleted)
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-27
 */
public class AdminNotificationLogsFragment extends Fragment {

    private static final String TAG = "AdminNotifLogsFragment";

    private RecyclerView rvLogs;
    private AdminLogsAdapter adapter;
    private AdminController adminController;
    private EditText etSearch;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    // Filter buttons
    private Button btnFilterAll, btnFilterWaiting, btnFilterSelected, btnFilterChosen, btnFilterCancelled;
    private String currentFilter = "all"; // Track active filter

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_logs, container, false);

        initializeViews(view);
        adminController = new AdminController();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupBackButton();
        loadLogs();

        return view;
    }

    private void initializeViews(View view) {
        rvLogs = view.findViewById(R.id.rv_logs);
        etSearch = view.findViewById(R.id.et_search);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
        btnBack = view.findViewById(R.id.btn_back);

        // Filter buttons
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterWaiting = view.findViewById(R.id.btn_filter_waiting);
        btnFilterSelected = view.findViewById(R.id.btn_filter_selected);
        btnFilterChosen = view.findViewById(R.id.btn_filter_chosen);
        btnFilterCancelled = view.findViewById(R.id.btn_filter_cancelled);
    }

    private void setupRecyclerView() {
        adapter = new AdminLogsAdapter(requireContext());
        rvLogs.setAdapter(adapter);
        rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s.toString(), currentFilter);
                    updateEmptyState();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupFilters() {
        View.OnClickListener filterListener = v -> {
            // Reset all button states
            resetFilterButtons();

            // Set active filter
            if (v.getId() == R.id.btn_filter_all) {
                currentFilter = "all";
                btnFilterAll.setSelected(true);
            } else if (v.getId() == R.id.btn_filter_waiting) {
                currentFilter = "waiting";
                btnFilterWaiting.setSelected(true);
            } else if (v.getId() == R.id.btn_filter_selected) {
                currentFilter = "selected";
                btnFilterSelected.setSelected(true);
            } else if (v.getId() == R.id.btn_filter_chosen) {
                currentFilter = "chosen";
                btnFilterChosen.setSelected(true);
            } else if (v.getId() == R.id.btn_filter_cancelled) {
                currentFilter = "cancelled";
                btnFilterCancelled.setSelected(true);
            }

            // Apply filter
            adapter.filter(etSearch.getText().toString(), currentFilter);
            updateEmptyState();
        };

        btnFilterAll.setOnClickListener(filterListener);
        btnFilterWaiting.setOnClickListener(filterListener);
        btnFilterSelected.setOnClickListener(filterListener);
        btnFilterChosen.setOnClickListener(filterListener);
        btnFilterCancelled.setOnClickListener(filterListener);

        // Set default active filter
        btnFilterAll.setSelected(true);
    }

    private void resetFilterButtons() {
        btnFilterAll.setSelected(false);
        btnFilterWaiting.setSelected(false);
        btnFilterSelected.setSelected(false);
        btnFilterChosen.setSelected(false);
        btnFilterCancelled.setSelected(false);
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    private void loadLogs() {
        showLoading(true);

        adminController.fetchNotificationLogs(new AdminController.DataCallback<List<NotificationLog>>() {
            @Override
            public void onSuccess(List<NotificationLog> logs) {
                if (isAdded()) {
                    showLoading(false);
                    adapter.setLogs(logs);
                    updateEmptyState();

                    String message = logs.isEmpty() ?
                            "No notification logs found" :
                            "Loaded " + logs.size() + " logs";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            "Error loading logs: " + e.getMessage(),
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
        rvLogs.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvLogs.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}