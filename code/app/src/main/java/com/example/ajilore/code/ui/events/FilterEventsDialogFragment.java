package com.example.ajilore.code.ui.events;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.ajilore.code.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * DialogFragment that provides filtering controls for the Events list.
 *
 * <p>This component supports US 01.01.04 — allowing entrants to filter events
 * based on date range, event type (category), and availability (open/closed).
 * It is displayed as a fullscreen modal dialog, giving users an intuitive UI
 * for selecting multiple criteria.</p>
 *
 * <h3>Supported Filter Types</h3>
 * <ul>
 *     <li><b>Date Range:</b> Select a continuous range via calendar chips.</li>
 *     <li><b>Categories:</b> Party, Workshop, Other (multi-select).</li>
 *     <li><b>Availability:</b> Open or Closed (single-select).</li>
 * </ul>
 *
 * <h3>Persistent Filter State</h3>
 * <p>The dialog stores the most recently applied filters in a static
 * {@link EventFilters} object, and restores them when reopened.</p>
 *
 * <h3>Usage</h3>
 * <pre>
 * FilterEventsDialogFragment dialog = FilterEventsDialogFragment.newInstance();
 * dialog.setFiltersListener(filters -> { ... });
 * dialog.show(getSupportFragmentManager(), "filters");
 * </pre>
 *
 * <p>When the user applies or clears filters, the dialog passes results to the host
 * through {@link OnFiltersAppliedListener}.</p>
 */
public class FilterEventsDialogFragment extends DialogFragment {

    // Views
    private TextView tvSelectedMonth;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private ChipGroup chipGroupDates;
    private CheckBox cbParty;
    private CheckBox cbWorkshop;
    private CheckBox cbOther;
    private CheckBox cbStatusClosed;
    private CheckBox cbStatusOpen;
    private Button btnApplyFilter;
    private Button btnClearFilter;
    private ImageButton btnBack;

    // Calendar for date selection
    private Calendar currentMonth;
    private Date startDate;
    private Date endDate;

    // Filter data
    private Set<String> selectedCategories;
    private String availabilityFilter;

    // Store current filters to persist across dialog opens
    private static EventFilters currentFilters = new EventFilters();

    // Callback interface
    private OnFiltersAppliedListener filtersListener;

    public interface OnFiltersAppliedListener {
        void onFiltersApplied(EventFilters filters);
        void onFiltersCancelled();
    }

    /**
     * Creates a new instance of the filter dialog.
     *
     * @return A fresh {@link FilterEventsDialogFragment}.
     */
    public static FilterEventsDialogFragment newInstance() {
        return new FilterEventsDialogFragment();
    }

    /**
     * Returns the most recently applied filter selections.
     *
     * <p>This allows the Events screen to show active filter badges or reapply
     * the filter to the Firestore query.</p>
     *
     * @return The saved {@link EventFilters} instance.
     */
    public static EventFilters getCurrentFilters() {
        return currentFilters;
    }

    /**
     * Removes the default dialog title bar to create a fullscreen modal appearance.
     *
     * @param savedInstanceState Previous state, if any.
     * @return Customized {@link Dialog} instance.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Remove dialog title bar for fullscreen appearance
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    /**
     * Expands the dialog to full width and height, giving it a full-screen layout.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Make dialog fullscreen
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    /**
     * Inflates the filter dialog UI layout.
     *
     * @param inflater LayoutInflater used to inflate XML.
     * @param container Optional parent container.
     * @param savedInstanceState Prior saved state.
     * @return Inflated root view for the dialog.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_event, container, false);
    }

    /**
     * Initializes UI components, restores previously selected filters,
     * attaches listeners, and populates the month/day chip views.
     *
     * @param view Inflated root view.
     * @param savedInstanceState Saved state bundle, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize data structures
        currentMonth = Calendar.getInstance();
        selectedCategories = new HashSet<>();

        // Restore previous filters
        restorePreviousFilters();

        // Initialize views
        initializeViews(view);

        // Set up listeners
        setupListeners();

        // Display current month
        updateMonthDisplay();

        // Restore UI state after views are initialized
        restoreUIState();
    }

    /**
     * Restores the filter state from the static {@link #currentFilters} object.
     *
     * <p>This includes:</p>
     * <ul>
     *     <li>Date range (start/end)</li>
     *     <li>Selected event categories</li>
     *     <li>Availability setting</li>
     *     <li>The month to display in the calendar</li>
     * </ul>
     */
    private void restorePreviousFilters() {
        if (currentFilters != null) {
            startDate = currentFilters.startDate;
            endDate = currentFilters.endDate;
            selectedCategories = new HashSet<>(currentFilters.categories);
            availabilityFilter = currentFilters.availabilityFilter;

            // Set currentMonth to the month of startDate if exists
            if (startDate != null) {
                currentMonth.setTime(startDate);
            }
        }
    }

    /**
     * Restores UI checkboxes and calendar chip selections based on the
     * previously stored filter values.
     */
    private void restoreUIState() {
        // Restore category checkboxes
        cbParty.setChecked(selectedCategories.contains("party"));
        cbWorkshop.setChecked(selectedCategories.contains("workshop"));
        cbOther.setChecked(selectedCategories.contains("other"));

        // Restore availability checkboxes
        if ("open".equals(availabilityFilter)) {
            cbStatusOpen.setChecked(true);
        } else if ("closed".equals(availabilityFilter)) {
            cbStatusClosed.setChecked(true);
        }

        // Restore date selection
        if (startDate != null && endDate != null) {
            selectDateRange();
        }
    }

    /**
     * Finds and initializes all view references from the layout.
     *
     * @param view The inflated root view.
     */
    private void initializeViews(View view) {
        // Back button
        btnBack = view.findViewById(R.id.btnBack);

        // Date Range
        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        chipGroupDates = view.findViewById(R.id.chipGroupDates);

        // Category (Event Type)
        cbParty = view.findViewById(R.id.cbParty);
        cbWorkshop = view.findViewById(R.id.cbWorkshop);
        cbOther = view.findViewById(R.id.cbOther);

        // Availability
        cbStatusClosed = view.findViewById(R.id.cbStatusClosed);
        cbStatusOpen = view.findViewById(R.id.cbStatusOpen);

        // Action buttons
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);
    }

    /**
     * Binds all click listeners and checkbox listeners used for:
     * <ul>
     *     <li>Navigating between months</li>
     *     <li>Selecting event categories</li>
     *     <li>Toggling availability</li>
     *     <li>Applying or clearing filters</li>
     *     <li>Dismissing the dialog</li>
     * </ul>
     */
    private void setupListeners() {
        // Back button - dismiss dialog without changing filters
        btnBack.setOnClickListener(v -> {
            if (filtersListener != null) {
                filtersListener.onFiltersCancelled();
            }
            dismiss();
        });

        // Month navigation
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthDisplay();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthDisplay();
        });

        // Category checkboxes (multiple selection allowed)
        cbParty.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategories.add("party");
            } else {
                selectedCategories.remove("party");
            }
        });

        cbWorkshop.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategories.add("workshop");
            } else {
                selectedCategories.remove("workshop");
            }
        });

        cbOther.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategories.add("other");
            } else {
                selectedCategories.remove("other");
            }
        });

        // Availability checkboxes (single selection)
        cbStatusOpen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbStatusClosed.setChecked(false);
                availabilityFilter = "open";
            } else if (!cbStatusClosed.isChecked()) {
                availabilityFilter = null;
            }
        });

        cbStatusClosed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbStatusOpen.setChecked(false);
                availabilityFilter = "closed";
            } else if (!cbStatusOpen.isChecked()) {
                availabilityFilter = null;
            }
        });

        // Apply filter button
        btnApplyFilter.setOnClickListener(v -> applyFilters());

        // Clear filter button
        btnClearFilter.setOnClickListener(v -> clearAllFilters());
    }

    /**
     * Updates the displayed month title and regenerates the chip list
     * showing each day of the selected month.
     *
     * <p>Also re-applies date range highlighting if the user has already selected
     * a start and end date.</p>
     */
    private void updateMonthDisplay() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvSelectedMonth.setText(monthFormat.format(currentMonth.getTime()));

        chipGroupDates.removeAllViews();

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;

            Chip chip = new Chip(requireContext());
            chip.setText(String.valueOf(day));
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector, null));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Calendar selectedCal = (Calendar) currentMonth.clone();
                    selectedCal.set(Calendar.DAY_OF_MONTH, currentDay);
                    selectedCal.set(Calendar.HOUR_OF_DAY, 0);
                    selectedCal.set(Calendar.MINUTE, 0);
                    selectedCal.set(Calendar.SECOND, 0);
                    selectedCal.set(Calendar.MILLISECOND, 0);
                    Date selectedDate = selectedCal.getTime();

                    if (startDate == null) {
                        startDate = selectedDate;
                    } else if (endDate == null) {
                        if (selectedDate.after(startDate)) {
                            Calendar endCal = (Calendar) selectedCal.clone();
                            endCal.set(Calendar.HOUR_OF_DAY, 23);
                            endCal.set(Calendar.MINUTE, 59);
                            endCal.set(Calendar.SECOND, 59);
                            endCal.set(Calendar.MILLISECOND, 999);
                            endDate = endCal.getTime();
                            selectDateRange();
                        } else {
                            clearDateSelection();
                            startDate = selectedDate;
                        }
                    } else {
                        clearDateSelection();
                        startDate = selectedDate;
                    }
                }
            });

            chipGroupDates.addView(chip);
        }

        // Restore date selection after recreating chips
        if (startDate != null && endDate != null) {
            selectDateRange();
        }
    }

    /**
     * Highlights all day chips that fall within the selected date range.
     *
     * <p>This method programmatically checks the chips between
     * {@link #startDate} and {@link #endDate}, without triggering
     * their listeners.</p>
     */
    private void selectDateRange() {
        if (startDate == null || endDate == null) return;

        for (int i = 0; i < chipGroupDates.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDates.getChildAt(i);
            int day = Integer.parseInt(chip.getText().toString());

            Calendar current = (Calendar) currentMonth.clone();
            current.set(Calendar.DAY_OF_MONTH, day);
            current.set(Calendar.HOUR_OF_DAY, 0);
            current.set(Calendar.MINUTE, 0);
            current.set(Calendar.SECOND, 0);
            current.set(Calendar.MILLISECOND, 0);

            // Check if date is in range
            boolean inRange = !current.getTime().before(startDate) && !current.getTime().after(endDate);

            // Remove listener temporarily to avoid triggering it when we programmatically check
            chip.setOnCheckedChangeListener(null);
            chip.setChecked(inRange);

            // Re-attach the listener
            final int currentDay = day;
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Calendar selectedCal = (Calendar) currentMonth.clone();
                    selectedCal.set(Calendar.DAY_OF_MONTH, currentDay);
                    selectedCal.set(Calendar.HOUR_OF_DAY, 0);
                    selectedCal.set(Calendar.MINUTE, 0);
                    selectedCal.set(Calendar.SECOND, 0);
                    selectedCal.set(Calendar.MILLISECOND, 0);
                    Date selectedDate = selectedCal.getTime();

                    if (startDate == null) {
                        startDate = selectedDate;
                    } else if (endDate == null) {
                        if (selectedDate.after(startDate)) {
                            Calendar endCal = (Calendar) selectedCal.clone();
                            endCal.set(Calendar.HOUR_OF_DAY, 23);
                            endCal.set(Calendar.MINUTE, 59);
                            endCal.set(Calendar.SECOND, 59);
                            endCal.set(Calendar.MILLISECOND, 999);
                            endDate = endCal.getTime();
                            selectDateRange();
                        } else {
                            clearDateSelection();
                            startDate = selectedDate;
                        }
                    } else {
                        clearDateSelection();
                        startDate = selectedDate;
                    }
                }
            });
        }
    }

    /**
     * Clears the selected start and end dates and unchecks all day chips.
     */
    private void clearDateSelection() {
        startDate = null;
        endDate = null;

        for (int i = 0; i < chipGroupDates.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDates.getChildAt(i);
            chip.setChecked(false);
        }
    }

    /**
     * Collects all selected filter options, saves them to the static
     * {@link #currentFilters} object, and notifies the listener via
     * {@link OnFiltersAppliedListener}.
     *
     * <p>The dialog then closes.</p>
     */
    private void applyFilters() {
        EventFilters filters = new EventFilters();
        filters.startDate = startDate;
        filters.endDate = endDate;
        filters.categories = new HashSet<>(selectedCategories);
        filters.availabilityFilter = availabilityFilter;

        // Save filters to static variable
        currentFilters = filters;

        if (filtersListener != null) {
            filtersListener.onFiltersApplied(filters);
        }

        dismiss();
    }

    /**
     * Resets all filter controls (date, category, availability) to their
     * default unselected state.
     *
     * <p>This also clears the persistent {@link #currentFilters} storage
     * and notifies the listener with an empty {@link EventFilters} object.</p>
     */
    private void clearAllFilters() {
        // Clear all selections in the UI
        clearDateSelection();

        cbParty.setChecked(false);
        cbWorkshop.setChecked(false);
        cbOther.setChecked(false);
        selectedCategories.clear();

        cbStatusClosed.setChecked(false);
        cbStatusOpen.setChecked(false);
        availabilityFilter = null;

        // Clear static filters
        currentFilters = new EventFilters();

        // Apply empty filters and dismiss
        EventFilters emptyFilters = new EventFilters();
        if (filtersListener != null) {
            filtersListener.onFiltersApplied(emptyFilters);
        }

        dismiss();
    }

    /**
     * Registers a listener to receive callbacks when the user applies
     * or cancels filtering.
     *
     * @param listener Callback to notify the host fragment or activity.
     */
    public void setFiltersListener(OnFiltersAppliedListener listener) {
        this.filtersListener = listener;
    }

    /**
     * POJO-style data class representing a full set of event filter criteria.
     *
     * <p>Supports serialization so it can be stored or passed between components.</p>
     *
     * Fields include:
     * <ul>
     *     <li>{@link #startDate}</li>
     *     <li>{@link #endDate}</li>
     *     <li>{@link #categories}</li>
     *     <li>{@link #availabilityFilter}</li>
     * </ul>
     */
    public static class EventFilters implements Serializable {
        public Date startDate;
        public Date endDate;
        public Set<String> categories;
        public String availabilityFilter;

        public EventFilters() {
            categories = new HashSet<>();
        }

        public boolean hasFilters() {
            return startDate != null ||
                    endDate != null ||
                    !categories.isEmpty() ||
                    availabilityFilter != null;
        }
    }
}