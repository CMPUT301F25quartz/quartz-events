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
 * AdminNotificationLogsFragment
 *
 * <p>Displays a searchable and filterable list of all notification logs sent by
 * organizers. This screen is accessible only to administrators and enables them
 * to audit broadcast activity across the system.</p>
 *
 * <p>Implements <b>US 03.08.01 – Admin can review logs of all notifications</b>.</p>
 *
 * <h3>Features</h3>
 * <ul>
 *     <li>Real-time list of notification logs (timestamp, event, audience, sender, message)</li>
 *     <li>Text search by event ID, message content, or sender</li>
 *     <li>Filter by audience: waiting, selected, chosen, cancelled, or all</li>
 *     <li>Empty-state display when no results match search/filter criteria</li>
 *     <li>Back-navigation to the Admin dashboard</li>
 * </ul>
 *
 * <p>The list itself is read-only; administrators cannot modify or delete logs.</p>
 *
 * <p><b>Design Pattern:</b> MVVM-lite with a controller-backed fragment.
 * The fragment manages UI + filtering while {@link AdminController}
 * handles Firestore retrieval.</p>
 *
 * @author
 *     Dinma (Team Quartz)
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

    /**
     * Inflates the log viewer layout, initializes UI components,
     * configures listeners (search, filters, back button),
     * and triggers the initial log load.
     *
     * @param inflater Layout inflater used to inflate the fragment UI.
     * @param container Parent container the fragment attaches to.
     * @param savedInstanceState Previously saved state.
     * @return The root view for the log viewer UI.
     */

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

    /**
     * Finds and initializes all views including RecyclerView, search bar,
     * empty-state layout, progress indicator, filter buttons, and back button.
     *
     * @param view The inflated fragment layout.
     */

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

    /**
     * Sets up the RecyclerView used to display notification logs.
     * Initializes {@link AdminLogsAdapter} and applies a vertical layout manager.
     */

    private void setupRecyclerView() {
        adapter = new AdminLogsAdapter(requireContext());
        rvLogs.setAdapter(adapter);
        rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * Adds a text listener to the search input field.
     * Updates the adapter’s filtered results on every keystroke and refreshes the empty-state UI.
     *
     * <p>Search terms can match:</p>
     * <ul>
     *     <li>eventId</li>
     *     <li>message text</li>
     *     <li>audience category</li>
     *     <li>sender UID</li>
     * </ul>
     */

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

    /**
     * Initializes click listeners for all audience filter buttons.
     * Selecting a filter applies it immediately to the adapter.
     *
     * <p>The active filter is tracked via {@code currentFilter} and combined
     * with the search term for final filtering.</p>
     *
     * <p>Filters:</p>
     * <ul>
     *     <li>all → show every log</li>
     *     <li>waiting → show logs targeting waiting list</li>
     *     <li>selected → show logs sent to accepted entrants</li>
     *     <li>chosen → show logs sent to lottery-selected entrants</li>
     *     <li>cancelled → show logs sent to removed entrants</li>
     * </ul>
     */

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

    /**
     * Clears the 'selected' state on all filter buttons before setting the new active filter.
     * Ensures consistent UI styling.
     */

    private void resetFilterButtons() {
        btnFilterAll.setSelected(false);
        btnFilterWaiting.setSelected(false);
        btnFilterSelected.setSelected(false);
        btnFilterChosen.setSelected(false);
        btnFilterCancelled.setSelected(false);
    }

    /**
     * Configures the top back button to navigate to the previous screen using
     * {@link #requireActivity()}.onBackPressed().
     */

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    /**
     * Fetches all notification logs from Firestore through {@link AdminController}.
     *
     * <p>On success:</p>
     * <ul>
     *     <li>Populates adapter with logs</li>
     *     <li>Applies active search + filter</li>
     *     <li>Updates empty-state visibility</li>
     * </ul>
     *
     * <p>On failure:</p>
     * <ul>
     *     <li>Shows error message</li>
     *     <li>Displays empty state</li>
     * </ul>
     *
     * <p>A loading spinner replaces the RecyclerView during fetch.</p>
     */
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

    /**
     * Shows or hides the progress bar and toggles the RecyclerView visibility.
     *
     * @param show If true, display the progress indicator; otherwise hide it.
     */

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvLogs.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Toggles between the empty-state message and the RecyclerView depending on
     * whether the adapter currently contains any results after filtering.
     */
    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvLogs.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}