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
 * {@code LoginFragment} handles both the login and signup process for users.
 * <p>
 * It allows users to:
 * <ul>
 *     <li>Log in using a name (verified in Firestore)</li>
 *     <li>Sign up with a name, email, and optional phone number</li>
 *     <li>Automatically authenticate anonymously using FirebaseAuth</li>
 *     <li>Navigate to the main events screen once authenticated</li>
 * </ul>
 *
 * <p>The fragment connects to Firestore to check or create user records and
 * ensures that names are unique across the system using the
 * {@code usersByName} collection.</p>
 *
 * <p><b>Outstanding issues:</b>
 * <ul>
 *     <li>The sign up button does not do anything</li>
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
    private FusedLocationProviderClient fusedLocationClient;
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
     * Called after the view has been created. Initializes Firebase authentication,
     * sets up validation listeners, and handles both login and signup button logic.
     *
     * @param v The fragment’s root view.
     * @param savedInstanceState Saved state if available.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

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


        // Hide signup section initially
        groupSignup.setVisibility(View.GONE);

        //Firebase initialization
        db = FirebaseFirestore.getInstance();

        // Location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Get stable device ID
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.d("DEVICE_ID", "Using device ID: " + deviceId);

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

                Map<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("email", email);
                user.put("phone", phone);
                user.put("role", "entrant");
                user.put("createdAt", FieldValue.serverTimestamp());
                user.put("preferences", "yes");
                user.put("profilepicture", null);
                user.put("locationEnabled", switchLocation.isChecked());

                if(!switchLocation.isChecked()){
                    saveUserToFirestore(user);
                    return;
                }

                requestCurrentLocation(user);
            });
        }

    private void requestCurrentLocation(Map<String, Object> user) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            return;
        }

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {
            if (location != null) {
                user.put("latitude", location.getLatitude());
                user.put("longitude", location.getLongitude());
            }

            saveUserToFirestore(user);

        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Couldn't get location", Toast.LENGTH_SHORT).show();
            saveUserToFirestore(user);
        });
    }

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
     * Navigates to the general events fragment after successful log in or sign up
     */
    private void navigateToEvents() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new GeneralEventsFragment())
                .commit();
    }


    /** Returns trimmed text from an EditText, or an empty string if null. */
    private String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    /** Checks if a string is not null or empty. */
    private boolean isNonBlank(String s) {
        return s != null && s.trim().length() > 0;
    }
    /** Basic validation to ensure email contains '@'. */
    private boolean isEmailOk(String s) {
        return s != null && s.contains("@");
    }
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
        }
    }
}
