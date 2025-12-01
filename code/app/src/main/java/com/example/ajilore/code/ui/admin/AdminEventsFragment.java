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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.adapters.AdminEventsAdapter;
import com.example.ajilore.code.controllers.AdminController;
import com.example.ajilore.code.ui.events.model.Event;
import com.example.ajilore.code.utils.DeleteDialogHelper;

import java.util.List;

/**
 * Fragment for displaying and managing events in the admin interface.
 *
 * <p>This fragment shows all events in the system and provides administrative
 * controls for event moderation, including viewing event details and removing
 * events that violate application policies.</p>
 *
 * <p>Design Pattern: Fragment pattern with RecyclerView adapter pattern.</p>
 *
 * <p>User Stories:</p>
 * <ul>
 *   <li>US 03.04.01 - As an administrator, I want to be able to browse events</li>
 *   <li>US 03.01.01 - As an administrator, I want to be able to remove events</li>
 * </ul>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminEventsFragment extends Fragment implements AdminEventsAdapter.OnEventActionListener {

    private RecyclerView rvEvents;
    private AdminEventsAdapter adapter;
    private AdminController adminController;
    private EditText etSearch;
    private LinearLayout layoutEmptyState;
    private ImageButton btnBack;

    /**
     * Creates and returns the view hierarchy for the fragment.
     *
     * @param inflater LayoutInflater to inflate views
     * @param container Parent container
     * @param savedInstanceState Saved state
     * @return Root view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_events, container, false);

        initializeViews(view);
        adminController = new AdminController();
        setupRecyclerView();
        setupSearch();
        setupBackButton();
        loadEvents();

        return view;
    }

    /**
     * Wires up member variables and UI fields from the root view.
     * @param view Root fragment view
     */
    private void initializeViews(View view) {
        rvEvents = view.findViewById(R.id.rv_events);
        etSearch = view.findViewById(R.id.et_search_events);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnBack = view.findViewById(R.id.btn_back);
    }

    /**
     * Sets up the RecyclerView and its adapter for the event list.
     */
    private void setupRecyclerView() {
        adapter = new AdminEventsAdapter(requireContext(), this);
        rvEvents.setAdapter(adapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * Configures the search EditText to filter events live.
     */
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

    /**
     * Sets up the back button in the toolbar to navigate to previous screen.
     */
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    /**
     * Loads all events from Firestore database.
     *
     * <p>Fetches event data asynchronously and populates the RecyclerView.</p>
     */
    private void loadEvents() {
        showLoading(true);

        adminController.fetchAllEvents(new AdminController.DataCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                if (isAdded()) {
                    showLoading(false);
                    adapter.setEvents(events);
                    updateEmptyState();
                    Toast.makeText(requireContext(),
                            "Loaded " + events.size() + " events",
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

    /**
     * Sets list visibility based on loading state.
     * @param show True to show "loading", false to show the list.
     */
    private void showLoading(boolean show) {
        // You can add a ProgressBar to your layout and toggle it here
        rvEvents.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Shows or hides empty state view if there are no events loaded.
     */
    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Called when an event is clicked by the admin for detail/toast display.
     * @param event The Event model
     */
    @Override
    public void onEventClick(Event event) {
        Toast.makeText(requireContext(),
                event.title + "\nCapacity: " + event.capacity,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles event deletion by administrator.
     *
     * @param event The event to be removed from the system
     */
    @Override
    public void onDeleteClick(Event event) {
        // Usage: Context, Type, Specific Name, The Action
        DeleteDialogHelper.showDeleteDialog(
                requireContext(),
                "Event",
                event.title,
                () -> deleteEvent(event) // This is the Runnable/Action
        );
    }


    /**
     * Shows confirmation dialog before event deletion, with title and cancel actions.
     * @param event The Event to potentially remove.
     */
    private void showDeleteDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate your custom dialog layout
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_delete_confirmation, null);

        TextView messageText = dialogView.findViewById(R.id.tv_dialog_message);
        String confirmationMessage = requireContext().getString(
                R.string.delete_confirmation_message, // The string resource ID
                event.title // The argument to replace %1$s - use public field
        );
        messageText.setText(confirmationMessage);

        Button deleteButton = dialogView.findViewById(R.id.btn_delete);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.setView(dialogView).create();

        deleteButton.setOnClickListener(v -> {
            deleteEvent(event); // Keep your existing delete functionality
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Removes an event from the backend and reloads the event list on success.
     * @param event Event to delete.
     */
    private void deleteEvent(Event event) {
        adminController.removeEvent(event.id, new AdminController.OperationCallback() {  // Use public 'id' field
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Event deleted",
                            Toast.LENGTH_SHORT).show();
                    loadEvents(); // Reload list
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