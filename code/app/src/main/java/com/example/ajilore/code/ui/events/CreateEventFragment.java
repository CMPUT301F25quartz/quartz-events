package com.example.ajilore.code.ui.events;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
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
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateEventFragment} factory method to
 * create an instance of this fragment.
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment, the views will exist after this
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

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

    private void showEventDatePicker(View v) {
        showDateThenTime(etDate, eventWhen);
    }

    private void showRegOpenDatePicker(View v) {
        showDateThenTime(etRegOpens, regOpenCal);
    }

    private void showRegCloseDatePicker(View v) {
        showDateThenTime(etRegCloses, regCloseCal);
    }

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


    private void validateAndSave() {
        String title = etTitle.getText().toString().trim();
        String eventType = actEventType.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String capacity = actCapacity.getText().toString().trim();


        boolean valid = true;
        if (title.isEmpty()) {
            etTitle.setError("Title required");
            valid = false;
        }
        if (eventType.isEmpty()) {
            actEventType.setError("Type required");
            valid = false;
        }
        if (location.isEmpty()) {
            etLocation.setError("Location required");
            valid = false;
        }

        if(TextUtils.isEmpty(etDate.getText())){
            etDate.setError("Date required");
            valid = false;
        }

        if(TextUtils.isEmpty(etRegOpens.getText())){
            etRegOpens.setError("Registration opens required");
            valid = false;
        }

        if(TextUtils.isEmpty(etRegCloses.getText())){
            etRegCloses.setError("Registration closes required");
            valid = false;
        }

        if(capacity.isEmpty()){
            actCapacity.setError("Capacity required");
            valid = false;
        }

        if(!valid) return;

        btnSave.setEnabled(false);

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("type", eventType);
        event.put("location", location);
        event.put("capacity", capacity);

        event.put("startsAt", new Timestamp(eventWhen.getTime()));
        event.put("regOpens", new Timestamp(regOpenCal.getTime()));
        event.put("regCloses", new Timestamp(regCloseCal.getTime()));

        //TODO: Use a real poster later
        event.put("posterKey", "jazz");

        event.put("status","published");

        //TODO: Use the real user id
        event.put("createdByUid","precious");

        event.put("createdAt", FieldValue.serverTimestamp());

        db.collection("org_events").add(event)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(requireContext(), "Event created ✅", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(err -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(requireContext(), "Failed to create: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    });



    }


}