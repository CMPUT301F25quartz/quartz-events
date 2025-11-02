package com.example.ajilore.code.ui.events;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ajilore.code.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A {@link Fragment} that provides a user interface for creating a new event.
 * <p>
 * This fragment contains a form with fields for event details such as title, type,
 * location, capacity, and various dates. It performs validation on user input and,
 * upon successful validation, saves the new event data to the Firebase Firestore
 * "org_events" collection.
 */
public class CreateEventFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private TextInputEditText etTitle, etLocation, etDate, etRegOpens, etRegCloses;
    private MaterialAutoCompleteTextView actCapacity, actEventType;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private final Calendar eventWhen = Calendar.getInstance();
    private final Calendar regOpenCal = Calendar.getInstance();
    private final Calendar regCloseCal = Calendar.getInstance();

    /**
     * Required empty public constructor for fragment instantiation.
     */
    public CreateEventFragment() {
        // Required empty public constructor
    }


    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment, the views will exist after this
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }


    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * This method is used to initialize views, set up adapters, and attach listeners.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //This helps to bind and attach listeners
        super.onViewCreated(view, savedInstanceState);

        //Buttons
        btnBack = view.findViewById(R.id.btnBack);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);


        etTitle = view.findViewById(R.id.etTitle);
        etLocation = view.findViewById(R.id.etLocation);
        etDate = view.findViewById(R.id.etDate);
        etRegOpens = view.findViewById(R.id.etRegOpen);
        etRegCloses = view.findViewById(R.id.etRegClose);


        actEventType = view.findViewById(R.id.etEventType);
        actCapacity = view.findViewById(R.id.actCapacity);

        TextInputLayout tilDate = view.findViewById(R.id.tilDate);
        TextInputLayout tilRegOpen = view.findViewById(R.id.tilRegOpen);
        TextInputLayout tilRegClose = view.findViewById(R.id.tilRegClose);

        // Dropdown options
        // Fetch string arrays from resources
        String[] types = getResources().getStringArray(R.array.event_types);
        String[] capacity = getResources().getStringArray(R.array.event_capacities);

        // Use a standard, public layout for dropdown items
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        ArrayAdapter<String> capacityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, capacity);

        actEventType.setAdapter(eventTypeAdapter);
        actCapacity.setAdapter(capacityAdapter);

        //Navigation for back/cancel
        View.OnClickListener goBack = v -> requireActivity().getSupportFragmentManager().popBackStack();
        if (btnCancel != null) btnCancel.setOnClickListener(goBack);
        btnBack.setOnClickListener(goBack);

        //Date/time pickers
        etDate.setOnClickListener(this::showEventDatePicker);
        tilDate.setEndIconOnClickListener(this::showEventDatePicker);

        etRegOpens.setOnClickListener(this::showRegOpenDatePicker);
        tilRegOpen.setEndIconOnClickListener(this::showRegOpenDatePicker);

        etRegCloses.setOnClickListener(this::showRegCloseDatePicker);
        tilRegClose.setEndIconOnClickListener(this::showRegCloseDatePicker);


        //Save
        btnSave.setOnClickListener(x -> validateAndSave());
        db = FirebaseFirestore.getInstance();
    }


    /**
     * Displays a {@link DatePickerDialog} for selecting the main event date.
     * @param v The view that triggered this method call.
     */
    private void showEventDatePicker(View v) {
        showDateThenTime(etDate, eventWhen);
    }

    /**
     * Displays a {@link DatePickerDialog} for selecting the registration opening date.
     * @param v The view that triggered this method call.
     */
    private void showRegOpenDatePicker(View v) {
        showDateThenTime(etRegOpens, regOpenCal);
    }

    /**
     * Displays a {@link DatePickerDialog} for selecting the registration closing date.
     * @param v The view that triggered this method call.
     */
    private void showRegCloseDatePicker(View v) {
        showDateThenTime(etRegCloses, regCloseCal);
    }

    /**
     * A generic helper method to show a styled {@link DatePickerDialog} and update the
     * corresponding {@link TextInputEditText} and {@link Calendar} instance upon date selection.
     *
     * @param et The EditText to update with the selected date string.
     * @param cal The Calendar instance to update with the selected date.
     */
    private void showDateThenTime(TextInputEditText et, Calendar cal) {
        // This constructor ensures the dialog uses your app's Material theme
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                R.style.ThemeOverlay_App_DatePicker, // <-- ADD THIS THEME
                (view, year, month, day) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, day);

                    // Clear time part to avoid timezone issues
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    // Use a consistent format
                    String format = DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime());
                    et.setText(format);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }


    /**
     * Validates all user input fields and, if valid, saves the event to Firestore.
     * <p>
     * This method performs the following steps:
     * 1. Retrieves all text inputs from the form fields.
     * 2. Performs null/empty checks on all required fields, setting errors if invalid.
     * 3. Parses the capacity string into a numeric type.
     * 4. If all data is valid, it constructs a data map and saves it as a new document
     *    in the "org_events" Firestore collection.
     * 5. Navigates back upon successful creation or displays an error on failure.
     */
    private void validateAndSave() {
        // --- Step 1: Get all input as strings ---
        String title = etTitle.getText().toString().trim();
        String eventType = actEventType.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String capacityStr = actCapacity.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String regOpensStr = etRegOpens.getText().toString().trim();
        String regClosesStr = etRegCloses.getText().toString().trim();

        // --- Step 2: Perform validation on all fields ---
        boolean valid = true;
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            valid = false;
        }
        if (eventType.isEmpty()) {
            // Use the TextInputLayout to show the error for dropdowns
            actEventType.setError("Type is required");
            valid = false;
        }
        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            valid = false;
        }
        if (capacityStr.isEmpty()) {
            actCapacity.setError("Capacity is required");
            valid = false;
        }
        if (dateStr.isEmpty()) {
            etDate.setError("Event date is required");
            valid = false;
        }
        if (regOpensStr.isEmpty()) {
            etRegOpens.setError("Registration open date is required");
            valid = false;
        }
        if (regClosesStr.isEmpty()) {
            etRegCloses.setError("Registration close date is required");
            valid = false;
        }

        // If any field is invalid, stop here.
        if (!valid) return;

        // --- Step 3: Parse the capacity string to a number ---
        long capacityVal;
        try {
            // Convert the string into a number (long)
            capacityVal = Long.parseLong(capacityStr);
        } catch (NumberFormatException e) {
            // This will catch cases where the string is not a valid number, though unlikely with your dropdown.
            actCapacity.setError("Invalid capacity format");
            Log.e("CreateEventFragment", "Failed to parse capacity string: " + capacityStr, e);
            return;
        }

        // --- Step 4: All data is valid, proceed to save ---
        btnSave.setEnabled(false);

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("type", eventType);
        event.put("location", location);
        event.put("capacity", capacityVal); // <-- SAVE THE NUMBER, NOT THE STRING

        // Use consistent field names for timestamps
        event.put("startsAt", new Timestamp(eventWhen.getTime()));
        event.put("regOpens", new Timestamp(regOpenCal.getTime())); // FIX: Use 'registrationOpens'
        event.put("regCloses", new Timestamp(regCloseCal.getTime())); // FIX: Use 'registrationCloses'

        //TODO: Use a real poster later
        event.put("posterKey", "jazz");
        event.put("status","published");

        //TODO: Use the real user id
        event.put("createdByUid","precious");
        event.put("createdAt", FieldValue.serverTimestamp());

        db.collection("org_events").add(event)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(requireContext(), "Event created ✅", Toast.LENGTH_SHORT).show();
                    if (isAdded()) { // Good practice to check if fragment is still attached
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(err -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to create: " + err.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


}