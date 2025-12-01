package com.example.ajilore.code.ui.events;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.ajilore.code.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.ajilore.code.utils.AdminAuthManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import android.net.Uri;

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
    private TextView tvHeader;
    private MaterialAutoCompleteTextView actCapacity, actEventType;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private final Calendar eventWhen = Calendar.getInstance();
    private final Calendar regOpenCal = Calendar.getInstance();
    private final Calendar regCloseCal = Calendar.getInstance();

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ImageView ivPosterPreview;
    private ImageButton ivPlusBig;

    private Uri pickedPosterUri = null; // To keep track if the user picked an image
     // To store Firebase Storage download URL

    private String eventId = null;
    private String existingPosterUrl = null;
    private SwitchMaterial geolocationSwitch;





    /**
     * Required empty public constructor for fragment instantiation.
     */
    public CreateEventFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to build a CreateEventFragment for updating a specific event.
     * @param eventId Firestore event document ID (nullable for new event)
     * @return Configured CreateEventFragment instance
     */
    public static CreateEventFragment newInstance(String eventId){
        CreateEventFragment fragment = new CreateEventFragment();
        Bundle args = new Bundle();
        if(eventId != null){
            args.putString("eventId",eventId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment, including poster picking launcher and argument parsing.
     * @param savedInstanceState Saved bundle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        pickedPosterUri = result.getData().getData();
                        requireContext().getContentResolver().takePersistableUriPermission(
                                pickedPosterUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Glide.with(this).load(pickedPosterUri).into(ivPosterPreview);
                        ivPosterPreview.setVisibility(View.VISIBLE);
                        ivPlusBig.setVisibility(View.GONE);
                    }
                }
        );

        Bundle args = getArguments();
        if(args != null && args.containsKey("eventId")){
            eventId = args.getString("eventId");
        }
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

        //Header
        tvHeader = view.findViewById(R.id.tvHeader);

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

        ivPosterPreview = view.findViewById(R.id.ivPosterPreview);
        ivPlusBig = view.findViewById(R.id.ivPlusBig);

        //geolocation toggle
        geolocationSwitch = view.findViewById(R.id.geolocation);


        //Adding poster logic

        ivPlusBig.setOnClickListener(v-> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        ivPosterPreview.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    pickImageLauncher.launch(intent);
                });

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

        if(eventId != null) {
            tvHeader.setText("Edit Event");
            db.collection("org_events").document(eventId).get()
                    .addOnSuccessListener(doc ->{
                        if(doc != null && doc.exists()){
                            etTitle.setText(doc.getString("title"));
                            etLocation.setText(doc.getString("location"));
                            actEventType.setText(doc.getString("type"));
                            //actCapacity.setText(doc.getString("capacity"));
                            Object capacityObj = doc.get("capacity");
                            if(capacityObj != null){
                                actCapacity.setText(capacityObj.toString());
                            } else {
                                actCapacity.setText("");
                            }


                            //Set poster image from cloudinary
                            existingPosterUrl = doc.getString("posterUrl");
                            if(existingPosterUrl != null && existingPosterUrl.startsWith("https")){
                                Glide.with(this).load(existingPosterUrl).into(ivPosterPreview);
                                ivPosterPreview.setVisibility(View.VISIBLE);
                                ivPlusBig.setVisibility(View.GONE);
                            }else{
                                ivPosterPreview.setVisibility(View.GONE);
                                ivPlusBig.setVisibility(View.VISIBLE);
                            }

                            Timestamp startsAt = doc.getTimestamp("startsAt");
                            if(startsAt != null){
                                eventWhen.setTime(startsAt.toDate());
                                etDate.setText(DateFormat.getDateInstance().format(eventWhen.getTime()));
                            }

                            //regOpenCal
                            Timestamp regOpens = doc.getTimestamp("regOpens");
                            if(regOpens != null){
                                regOpenCal.setTime(regOpens.toDate());
                                etRegOpens.setText(DateFormat.getDateInstance().format(regOpenCal.getTime()));
                            }

                            //regCloseCal
                            Timestamp regCloses = doc.getTimestamp("regCloses");
                            if(regCloses != null){
                                regCloseCal.setTime(regCloses.toDate());
                                etRegCloses.setText(DateFormat.getDateInstance().format(regCloseCal.getTime()));
                            }

                            //geolocation
                            Boolean geo = doc.getBoolean("geolocationRequired");
                            if (geo != null) {
                                geolocationSwitch.setChecked(geo);
                            }

                        }
                    });
        }
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
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                R.style.ThemeOverlay_App_DatePicker,
                (view, year, month, day) -> {
                    // Set the date part
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, day);

                    // show a time picker
                    new android.app.TimePickerDialog(
                            requireContext(),
                            (timePicker, hour, minute) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hour);
                                cal.set(Calendar.MINUTE, minute);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);

                                //Format full date+time into the field
                                String formatted = DateFormat.getDateTimeInstance(
                                        DateFormat.MEDIUM,
                                        DateFormat.SHORT
                                ).format(cal.getTime());
                                et.setText(formatted);
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true  // 24‑hour; use false for 12‑hour
                    ).show();
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

        Log.d("CreateEventFragment", "validateAndSave() called");

        //Retrieve all user input as strings
        String title = etTitle.getText().toString().trim();
        String eventType = actEventType.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String capacityStr = actCapacity.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String regOpensStr = etRegOpens.getText().toString().trim();
        String regClosesStr = etRegCloses.getText().toString().trim();

        // Validate input fields and set errors on invalid/empty
        boolean valid = true;
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            valid = false;
        }
        if (eventType.isEmpty()) {
            actEventType.setError("Type is required");
            valid = false;
        }
        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            valid = false;
        }
        /**
        if (capacityStr.isEmpty()) {
            actCapacity.setError("Capacity is required");
            valid = false;
        }**/
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
        if (!valid) {
            btnSave.setEnabled(true);
            return;
        }

        // Parse capacity to number
        Long capacityVal = null;
        if(!capacityStr.isEmpty()) {
            try {
                capacityVal = Long.parseLong(capacityStr);
            } catch (NumberFormatException e) {
                actCapacity.setError("Invalid capacity format");
                Log.e("CreateEventFragment", "Failed to parse capacity: " + capacityStr, e);
                btnSave.setEnabled(true);
                return;
            }
        }

        // Prepare database event creation logic
        btnSave.setEnabled(false);

        String deviceId = AdminAuthManager.getDeviceId(requireContext());
        // Check Permissions in Firestore before proceeding
        Long finalCapacityVal = capacityVal;
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                            // 1. Check Policy Violations
                            if (documentSnapshot.exists()) {
                                Boolean canCreate = documentSnapshot.getBoolean("canCreateEvents");
                                String status = documentSnapshot.getString("accountStatus");

                                // If user is deactivated, STOP everything.
                                if (Boolean.FALSE.equals(canCreate) || "deactivated".equals(status)) {
                                    Toast.makeText(getContext(), "Account deactivated due to policy violation.", Toast.LENGTH_LONG).show();
                                    btnSave.setEnabled(true);
                                    return;
                                }
                            }
         // User is clear, has permissions
        boolean geoRequired = geolocationSwitch != null &&geolocationSwitch.isChecked();


        Consumer<String> saveEventWithPosterUrl = (posterUrl) -> {
            Map<String, Object> event = new HashMap<>();
            event.put("title", title);
            event.put("type", eventType);
            event.put("location", location);
            if (finalCapacityVal != null){
            event.put("capacity", finalCapacityVal);
            }
            event.put("startsAt", new Timestamp(eventWhen.getTime()));
            event.put("regOpens", new Timestamp(regOpenCal.getTime()));
            event.put("regCloses", new Timestamp(regCloseCal.getTime()));
            event.put("posterUrl", posterUrl);
            event.put("status", "published");
            event.put("createdByUid", deviceId); // Replace with actual user ID in production
            event.put("createdAt", FieldValue.serverTimestamp());
            event.put("updatedAt", FieldValue.serverTimestamp());
            event.put("geolocationRequired", geoRequired);


            if(posterUrl != null){
                event.put("posterUrl", posterUrl);
            }else if (existingPosterUrl != null){
                event.put("posterUrl", existingPosterUrl);
            }

            if(eventId == null){
                //Create a new event
                event.put("createdAt", FieldValue.serverTimestamp());
                event.put("createdByUid", deviceId);
                db.collection("org_events").add(event)
                        .addOnSuccessListener(ref -> {
                            //role change to organiser
                            db.collection("users")
                                    .document(deviceId)
                                    .update("role", "organiser")
                                    .addOnSuccessListener(v -> Log.d("CreateEvent", "User is now an organiser"))
                                    .addOnFailureListener(err -> Log.e("CreateEvent", "Failed to update role: " + err.getMessage()));
                            btnSave.setEnabled(true);
                            Toast.makeText(requireContext(), "Event saved!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(err -> {
                            btnSave.setEnabled(true);
                            Toast.makeText(requireContext(), "Failed to create: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        });

            }else {
                //Update the header to say Edit Event not Create Event
                //tvHeader.setText("Edit Event");
                // update existing event
                db.collection("org_events").document(eventId)
                        .update(event)
                        .addOnSuccessListener(ref -> {
                            btnSave.setEnabled(true);
                            Toast.makeText(requireContext(), "Event updated!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(err -> {
                            btnSave.setEnabled(true);
                            Toast.makeText(requireContext(), "Failed to update: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }


        };

        // Step 5: Handle poster upload if provided, else save immediately
        if (pickedPosterUri != null) {
            uploadToCloudinary(pickedPosterUri, saveEventWithPosterUrl);
        } else {
            saveEventWithPosterUrl.accept(null);
        }
        Log.d("CreateEventFragment", "Validation passed, starting upload or save");
                })
                .addOnFailureListener(e -> {
                    // Handle network error during permission check
                    Log.e("CreateEventFragment", "Error checking permissions", e);
                    Toast.makeText(getContext(), "Error verifying permissions. Please try again.", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                });
    }
    /**
     * Uploads an image to Cloudinary and calls a consumer with the resulting URL.
     * @param imageUri The picked image's URI.
     * @param onUrlReady Consumer called with an HTTPS Cloudinary URL on success.
     */
   private void uploadToCloudinary(Uri imageUri, Consumer<String> onUrlReady){
       MediaManager.get().upload(imageUri)
               .callback(new UploadCallback() {
                   @Override
                   public void onStart(String requestId) {
                       Log.d("Cloudinary", "Started image upload: " + requestId);
                   }
                   @Override
                   public void onProgress(String requestId, long bytes, long totalBytes) {}
                   @Override
                   public void onSuccess(String requestId, Map resultData) {
                       String url = (String) resultData.get("secure_url");
                       Log.d("Cloudinary", "Uploaded image URL: " + url);
                       // Pass the URL to a callback for further use (e.g., save posterUrl to Firestore).
                       onUrlReady.accept(url);
                   }
                   @Override
                   public void onError(String requestId, ErrorInfo error) {
                       Log.e("Cloudinary", "Upload error: " + error.getDescription());
                       // Optionally show a toast or handle UI here.
                       Toast.makeText(getContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                   }
                   @Override
                   public void onReschedule(String requestId, ErrorInfo error) {}
               })
               .dispatch();
   }






}