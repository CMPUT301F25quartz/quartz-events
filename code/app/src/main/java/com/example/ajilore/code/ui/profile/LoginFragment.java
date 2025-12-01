package com.example.ajilore.code.ui.profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.GeneralEventsFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/**
 * {@code LoginFragment} manages the login and signup flow for users in the app.
 *
 * <p>This fragment uses the device's {@code ANDROID_ID} as the unique identifier,
 * meaning each physical device corresponds to one account in Firestore
 * (no username/password system is used).</p>
 *
 * <h3>Features</h3>
 * <ul>
 *     <li><b>Login</b> — If the device already has a Firestore user record, the user is logged in immediately.</li>
 *     <li><b>Signup</b> — If the device is not recognized, the user may create a new account with name, email, and optional phone number.</li>
 *     <li><b>Location Preference Toggle</b> — Users can enable/disable location collection.
 *         This preference is saved to Firestore and used when registering for events.</li>
 *     <li><b>Permission Handling</b> — If the user enables location, the fragment checks runtime
 *         permissions and requests them when required.</li>
 *     <li><b>Navigation</b> — Upon login or signup, users are sent to {@link GeneralEventsFragment}.</li>
 * </ul>
 *
 * @author
 *     Temi Akindele
 */

public class LoginFragment extends Fragment {

    // Views
    private TextInputLayout tilNameSignup, tilEmailSignup, tilPhoneSignup;
    private TextInputEditText etNameSignup, etEmailSignup, etPhoneSignup;
    private MaterialButton btnContinueLogin, btnSignup;
    private LinearLayout groupSignup;

    private FirebaseFirestore db;
    // Location
    private String deviceId;
    private static final int LOCATION_REQUEST_CODE = 101;
    private MaterialSwitch switchLocation;


    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater  LayoutInflater used to inflate the XML layout.
     * @param container Parent container that holds this fragment's view.
     * @param savedInstanceState Previously saved state (if any).
     * @return The root view for the login screen.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    /**
     * Initializes UI components, loads saved user data (including location preference),
     * sets up listeners for login and signup, hides the bottom navigation,
     * and configures Firestore and device identity.
     *
     * @param v Root view returned from {@link #onCreateView}
     * @param savedInstanceState Previously saved state (unused)
     */

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        //Firebase initialization
        db = FirebaseFirestore.getInstance();


        // Get stable device ID
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.d("DEVICE_ID", "Using device ID: " + deviceId);

        // Bind views
        tilNameSignup   = v.findViewById(R.id.tilNameSignup);
        etNameSignup    = v.findViewById(R.id.etNameSignup);
        btnContinueLogin = v.findViewById(R.id.btnContinueLogin);


        groupSignup    = v.findViewById(R.id.groupSignup);
        tilEmailSignup = v.findViewById(R.id.tilEmailSignup);
        etEmailSignup  = v.findViewById(R.id.etEmailSignup);
        tilPhoneSignup = v.findViewById(R.id.tilPhoneSignup);
        etPhoneSignup  = v.findViewById(R.id.etPhoneSignup);
        btnSignup      = v.findViewById(R.id.btnSignup);

        switchLocation = v.findViewById(R.id.switchLocation);
        // Load saved preference so toggle shows correct state
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean pref = doc.getBoolean("locationPreference");
                        if (pref != null) switchLocation.setChecked(pref);
                    }
                });



        // Hide signup section initially
        groupSignup.setVisibility(View.GONE);



        //Hide bottom nav during login
        ((MainActivity) requireActivity()).hideBottomNav();


        //enable sign up only when fields are valid
        TextWatcher signupWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = getText(etNameSignup);
                String email = getText(etEmailSignup);
                btnSignup.setEnabled(isNonBlank(name) && isEmailOk(email));
            }

            @Override public void afterTextChanged(Editable s) {}
        };

        etNameSignup.addTextChangedListener(signupWatcher);
        etEmailSignup.addTextChangedListener(signupWatcher);

        // LOGIN
        btnContinueLogin.setOnClickListener(view -> {
                    //checking if the device id already exists in firestore
                    db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    boolean enabled = switchLocation.isChecked();

                                    // update preference on every login
                                    db.collection("users")
                                            .document(deviceId)
                                            .update("locationPreference", enabled);
                                    //device already exists
                                    groupSignup.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
                                    ((MainActivity) requireActivity()).showBottomNav();
                                    navigateToEvents();
                                } else {
                                    //device not recognized
                                    Toast.makeText(getContext(), "New device. Please sign up.", Toast.LENGTH_SHORT).show();
                                    groupSignup.setVisibility(View.VISIBLE);
                                }
                            })
                            .addOnFailureListener(err ->
                                    Toast.makeText(getContext(), "Error" + err.getMessage(), Toast.LENGTH_LONG).show()
                            );
                });

        switchLocation.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                if (ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_REQUEST_CODE
                    );
                } else {
                    saveLocationPreference(true);
                }
            } else {
                saveLocationPreference(false);
                Toast.makeText(getContext(), "Location disabled", Toast.LENGTH_SHORT).show();
            }
        });


            // SIGNUP: create user doc then navigate
            btnSignup.setOnClickListener(view -> {
                String name = getText(etNameSignup);
                String email = getText(etEmailSignup);
                String phone = getText(etPhoneSignup);

                if (!isNonBlank(name)) {
                    tilNameSignup.setError("Name required");
                    return;
                }
                if (!isEmailOk(email)) {
                    tilEmailSignup.setError("Invalid email address");
                    return;
                }

                // Check for ban before creation
                // Use the initialized 'deviceId' variable
                db.collection("banned_users").document(deviceId).get()
                        .addOnSuccessListener(banDoc -> {
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("createdAt", FieldValue.serverTimestamp());
                            user.put("preferences", "yes");
                            user.put("profilepicture", null);
                            user.put("locationPreference", switchLocation.isChecked());

                            // --- CRITICAL SECURITY CHECK ---
                            if (banDoc.exists()) {
                                //  USER IS BANNED
                                // Force them to be an entrant and REVOKE creating privileges
                                user.put("role", "entrant");
                                user.put("canCreateEvent", false);

                                // Notify them nicely but firmly
                                Toast.makeText(getContext(),
                                        "Note: Your account is restricted due to a previous ban.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                //  USER IS CLEAN
                                user.put("role", "entrant"); // Default role
                                user.put("canCreateEvent", true); // Grant standard privileges
                            }

                                saveUserToFirestore(user);
                        });
            });
    }

    /**
     * Saves the user's location-sharing preference to Firestore.
     *
     * <p>This is triggered when the user toggles the location switch,
     * or after granting location permission.</p>
     *
     * @param enabled {@code true} if the user allows location collection, else {@code false}
     */

    private void saveLocationPreference(boolean enabled) {
        db.collection("users")
                .document(deviceId)
                .update("locationPreference", enabled)
                .addOnSuccessListener(x -> Log.d("Login", "Location preference updated"))
                .addOnFailureListener(e -> Log.e("Login", "Failed to update location preference", e));
    }

    /**
     * Creates or overwrites a user document in Firestore using the device ID.
     *
     * <p>Called only during signup. After saving, the user is navigated to
     * {@link GeneralEventsFragment} and the bottom navigation is shown.</p>
     *
     * @param user A map of all profile fields to store under {@code users/{deviceId}}
     */

    private void saveUserToFirestore(Map<String, Object> user) {
        db.collection("users")
                .document(deviceId)
                .set(user)
                .addOnSuccessListener(x -> {
                    Toast.makeText(getContext(), "Account created!", Toast.LENGTH_SHORT).show();
                    ((MainActivity) requireActivity()).showBottomNav();
                    navigateToEvents();
                })
                .addOnFailureListener(err ->
                        Toast.makeText(getContext(), "Signup failed: " + err.getMessage(), Toast.LENGTH_LONG).show()
                );
    }


    /**
     * Navigates to the {@link GeneralEventsFragment} after a successful login or signup.
     */

    private void navigateToEvents() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new GeneralEventsFragment())
                .commit();
    }


    /**
     * Returns the trimmed string contents of a {@link TextInputEditText},
     * or an empty string if the field is null.
     *
     * @param et The EditText field
     * @return Trimmed text or empty string
     */

    private String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /**
     * Checks whether a string is non-null and contains at least one non-space character.
     *
     * @param s The string to check
     * @return {@code true} if non-blank, otherwise {@code false}
     */
    private boolean isNonBlank(String s) {
        return s != null && s.trim().length() > 0;
    }

    /**
     * Performs basic email validation by checking if the string contains '@'.
     *
     * <p>This is not a full regex validation—just a lightweight check for UI enabling.</p>
     *
     * @param s Email string to validate
     * @return {@code true} if it contains '@', else {@code false}
     */
    private boolean isEmailOk(String s) {
        return s != null && s.contains("@");
    }

    /**
     * Handles the result of the runtime location permission request.
     *
     * <p>If the permission is granted, the user's Firestore preference is updated
     * to reflect that location collection is enabled.</p>
     *
     * @param requestCode Unique code associated with the permission request
     * @param permissions The permission(s) being requested
     * @param grantResults Results of the user's response
     */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getContext(), "Location permission granted", Toast.LENGTH_SHORT).show();

            saveLocationPreference(true);
        }
    }
}
