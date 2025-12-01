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
 * US 01.01.04: Filter events based on interests and availability
 * DialogFragment for filtering events by date range, location, category, and availability
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

    // ✅ NEW: Store current filters to persist across dialog opens
    private static EventFilters currentFilters = new EventFilters();

    // Callback interface
    private OnFiltersAppliedListener filtersListener;

    public interface OnFiltersAppliedListener {
        void onFiltersApplied(EventFilters filters);
        void onFiltersCancelled();
    }

    public static FilterEventsDialogFragment newInstance() {
        return new FilterEventsDialogFragment();
    }

    // ✅ NEW: Method to get current filters from outside
    public static EventFilters getCurrentFilters() {
        return currentFilters;
    }

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize data structures
        currentMonth = Calendar.getInstance();
        selectedCategories = new HashSet<>();

        // ✅ NEW: Restore previous filters
        restorePreviousFilters();

        // Initialize views
        initializeViews(view);

        // Set up listeners
        setupListeners();

        // Display current month
        updateMonthDisplay();

        // ✅ NEW: Restore UI state after views are initialized
        restoreUIState();
    }

    // ✅ NEW: Restore filters from static storage
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

    // Restore UI state based on saved filters
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

    private void clearDateSelection() {
        startDate = null;
        endDate = null;

        for (int i = 0; i < chipGroupDates.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDates.getChildAt(i);
            chip.setChecked(false);
        }
    }

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

    public void setFiltersListener(OnFiltersAppliedListener listener) {
        this.filtersListener = listener;
    }

    /**
     * Data class to hold filter criteria
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