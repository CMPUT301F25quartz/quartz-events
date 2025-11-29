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
    private CheckBox cbWithin10km;
    private CheckBox cbWithin25km;
    private CheckBox cbWithin50km;
    private CheckBox cbMusicConcerts;
    private CheckBox cbSports;
    private CheckBox cbOpenForRegistration;
    private CheckBox cbWaitingListAvailable;
    private Button btnApplyFilter;
    private Button btnClearFilter;
    private ImageButton btnBack;

    // Calendar for date selection
    private Calendar currentMonth;
    private Date startDate;
    private Date endDate;

    // Filter data
    private Set<String> selectedCategories;
    private String selectedLocationRange;
    private String availabilityFilter;

    // Callback interface
    private OnFiltersAppliedListener filtersListener;

    public interface OnFiltersAppliedListener {
        void onFiltersApplied(EventFilters filters);
        void onFiltersCancelled();
    }

    public static FilterEventsDialogFragment newInstance() {
        return new FilterEventsDialogFragment();
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

        // Initialize views
        initializeViews(view);

        // Set up listeners
        setupListeners();

        // Display current month
        updateMonthDisplay();
    }

    private void initializeViews(View view) {
        // Back button
        btnBack = view.findViewById(R.id.btnBack);

        // Date Range
        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        chipGroupDates = view.findViewById(R.id.chipGroupDates);

        // Location
        cbWithin10km = view.findViewById(R.id.cbWithin10km);
        cbWithin25km = view.findViewById(R.id.cbWithin25km);
        cbWithin50km = view.findViewById(R.id.cbWithin50km);

        // Category
        cbMusicConcerts = view.findViewById(R.id.cbMusicConcerts);
        cbSports = view.findViewById(R.id.cbSports);

        // Availability
        cbOpenForRegistration = view.findViewById(R.id.cbOpenForRegistration);
        cbWaitingListAvailable = view.findViewById(R.id.cbWaitingListAvailable);

        // Action buttons
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);
    }

    private void setupListeners() {
        // Back button - dismiss dialog
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

        // Location checkboxes (single selection)
        cbWithin10km.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbWithin25km.setChecked(false);
                cbWithin50km.setChecked(false);
                selectedLocationRange = "10km";
            } else {
                selectedLocationRange = null;
            }
        });

        cbWithin25km.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbWithin10km.setChecked(false);
                cbWithin50km.setChecked(false);
                selectedLocationRange = "25km";
            } else {
                selectedLocationRange = null;
            }
        });

        cbWithin50km.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbWithin10km.setChecked(false);
                cbWithin25km.setChecked(false);
                selectedLocationRange = "50km";
            } else {
                selectedLocationRange = null;
            }
        });

        // Category checkboxes
        cbMusicConcerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategories.add("Music & Concerts");
            } else {
                selectedCategories.remove("Music & Concerts");
            }
        });

        cbSports.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategories.add("Sports");
            } else {
                selectedCategories.remove("Sports");
            }
        });

        // Availability checkboxes (single selection)
        cbOpenForRegistration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbWaitingListAvailable.setChecked(false);
                availabilityFilter = "open";
            } else {
                availabilityFilter = null;
            }
        });

        cbWaitingListAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbOpenForRegistration.setChecked(false);
                availabilityFilter = "waiting";
            } else {
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

        // Clear existing chips
        chipGroupDates.removeAllViews();

        // Get days in month
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Create chips for each day
        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            Date date = cal.getTime();

            Chip chip = new Chip(requireContext());
            chip.setText(String.valueOf(day));
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector, null));

            // Handle chip selection for date range
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (startDate == null) {
                        startDate = date;
                    } else if (endDate == null) {
                        if (date.after(startDate)) {
                            endDate = date;
                            selectDateRange();
                        } else {
                            clearDateSelection();
                            startDate = date;
                        }
                    } else {
                        clearDateSelection();
                        startDate = date;
                    }
                }
            });

            chipGroupDates.addView(chip);
        }
    }

    private void selectDateRange() {
        if (startDate == null || endDate == null) return;

        Calendar start = Calendar.getInstance();
        start.setTime(startDate);

        Calendar end = Calendar.getInstance();
        end.setTime(endDate);

        for (int i = 0; i < chipGroupDates.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDates.getChildAt(i);
            int day = Integer.parseInt(chip.getText().toString());

            Calendar current = (Calendar) currentMonth.clone();
            current.set(Calendar.DAY_OF_MONTH, day);

            if (!current.getTime().before(startDate) && !current.getTime().after(endDate)) {
                chip.setChecked(true);
            }
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
        filters.locationRange = selectedLocationRange;
        filters.categories = new HashSet<>(selectedCategories);
        filters.availabilityFilter = availabilityFilter;

        if (filtersListener != null) {
            filtersListener.onFiltersApplied(filters);
        }

        dismiss();
    }

    private void clearAllFilters() {
        clearDateSelection();

        cbWithin10km.setChecked(false);
        cbWithin25km.setChecked(false);
        cbWithin50km.setChecked(false);
        selectedLocationRange = null;

        cbMusicConcerts.setChecked(false);
        cbSports.setChecked(false);
        selectedCategories.clear();

        cbOpenForRegistration.setChecked(false);
        cbWaitingListAvailable.setChecked(false);
        availabilityFilter = null;

        EventFilters emptyFilters = new EventFilters();
        if (filtersListener != null) {
            filtersListener.onFiltersApplied(emptyFilters);
        }
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
        public String locationRange;
        public Set<String> categories;
        public String availabilityFilter;

        public EventFilters() {
            categories = new HashSet<>();
        }

        public boolean hasFilters() {
            return startDate != null ||
                    endDate != null ||
                    locationRange != null ||
                    !categories.isEmpty() ||
                    availabilityFilter != null;
        }
    }
}