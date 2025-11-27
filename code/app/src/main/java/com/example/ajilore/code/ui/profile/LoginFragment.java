package com.example.ajilore.code.ui.profile;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.GeneralEventsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
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
    private TextInputLayout tilNameLogin, tilEmailSignup, tilPhoneSignup;
    private TextInputEditText etNameLogin, etEmailSignup, etPhoneSignup;
    private MaterialButton btnContinueLogin, btnSignup;
    private LinearLayout groupSignup;

    // Firestore
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;
    private boolean authReady = false;
    private BottomNavigationView bottomNavigationView;

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
        tilNameLogin   = v.findViewById(R.id.tilNameLogin);
        etNameLogin    = v.findViewById(R.id.etNameLogin);
        btnContinueLogin = v.findViewById(R.id.btnContinueLogin);

        groupSignup    = v.findViewById(R.id.groupSignup);
        tilEmailSignup = v.findViewById(R.id.tilEmailSignup);
        etEmailSignup  = v.findViewById(R.id.etEmailSignup);
        tilPhoneSignup = v.findViewById(R.id.tilPhoneSignup);
        etPhoneSignup  = v.findViewById(R.id.etPhoneSignup);
        btnSignup      = v.findViewById(R.id.btnSignup);

        // Hide signup section initially
        groupSignup.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        ((MainActivity) requireActivity()).hideBottomNav();


        // Disable interactions until auth is ready
        setUiEnabled(false);

        // Ensure we have a UID
        if (auth.getCurrentUser() == null) {
            Log.d("AuthTest", "Starting anonymous sign-in…");
            auth.signInAnonymously().addOnSuccessListener(res -> {
                uid = res.getUser().getUid();
                authReady = true;
                Log.d("AuthTest", "Anonymous sign-in success. UID=" + uid);
                setUiEnabled(true);
            }).addOnFailureListener(e -> {
                authReady = false;
                Log.e("AuthTest", "Anonymous sign-in FAILED: " + e.getMessage(), e);
                Toast.makeText(getContext(), "Auth failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            uid = auth.getCurrentUser().getUid();
            authReady = true;
            Log.d("AuthTest", "Already signed in. UID=" + uid);
            setUiEnabled(true);
        }

        // Live validation for login name
        etNameLogin.addTextChangedListener(simpleWatcher(s -> {
            boolean ok = isNonBlank(getText(etNameLogin));
            tilNameLogin.setError(ok ? null : "Name required");
            btnContinueLogin.setEnabled(authReady && ok);
        }));

        // Live validation for signup (email required, phone optional)
        TextWatcher signupWatcher = simpleWatcher(s -> {
            boolean okEmail = isEmailOk(getText(etEmailSignup));
            tilEmailSignup.setError(okEmail ? null : "Must contain @");
            btnSignup.setEnabled(okEmail && isNonBlank(getText(etNameLogin)));
        });
        etEmailSignup.addTextChangedListener(signupWatcher);
        etPhoneSignup.addTextChangedListener(signupWatcher);

        // LOGIN: check if user exists by nameLower; if yes -> navigate; if no -> show signup
        btnContinueLogin.setOnClickListener(view -> {
            if (!authReady) {
                Toast.makeText(getContext(), "Setting up… try again", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = getText(etNameLogin);
            if (!isNonBlank(name)) {
                tilNameLogin.setError("Name required");
                return;
            }
            String nameLower = name.toLowerCase(Locale.ROOT).trim();

            db.collection("usersByName").document(nameLower)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Toast.makeText(getContext(), "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
                            //dsiplaying the nav bar
                            //((MainActivity) requireActivity()).findViewById(R.id.menu_bottom_nav).setVisibility(View.VISIBLE);
                            //bottomNavigationView.setVisibility(View.VISIBLE);
                            // bottomNavigationView.setSelectedItemId(R.id.generalEventsFragment);
                            ((MainActivity) requireActivity()).showBottomNav();
                            navigateToEvents();
                        } else {
                            Toast.makeText(getContext(), "No account found. Please sign up.", Toast.LENGTH_SHORT).show();
                            groupSignup.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(err ->
                            Toast.makeText(getContext(), "Login check failed: " + err.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        // SIGNUP: create user doc then navigate
        btnSignup.setOnClickListener(view -> {
            if (!authReady) {
                Toast.makeText(getContext(), "Setting up… try again", Toast.LENGTH_SHORT).show();
                return;
            }
            String name  = getText(etNameLogin);
            String email = getText(etEmailSignup);
            String phone = getText(etPhoneSignup);

            if (!isNonBlank(name)) {
                tilNameLogin.setError("Name required");
                return;
            }
            if (!isEmailOk(email)) {
                tilEmailSignup.setError("Must contain @");
                return;
            }

            String nameLower = name.toLowerCase(Locale.ROOT).trim();
            String deviceId  = getDeviceId();

            Map<String, Object> user = new HashMap<>();
            user.put("name", name.trim());
            user.put("nameLower", nameLower);
            user.put("email", email.trim());
            user.put("phone", phone.trim());
            user.put("role", "entrant");
            user.put("createdAt", FieldValue.serverTimestamp());


            db.runTransaction(trx -> {
                // Ensure name not taken (second layer of safety)
                var nameDocRef = db.collection("usersByName").document(nameLower);
                var nameDoc = trx.get(nameDocRef);
                if (nameDoc.exists()) {
                    throw new RuntimeException("That name is already taken.");
                }
                // Write profile
                trx.set(db.collection("users").document(uid), user);
                // Reserve name
                Map<String,Object> idx = new HashMap<>();
                idx.put("uid", uid);
                idx.put("createdAt", FieldValue.serverTimestamp());
                trx.set(nameDocRef, idx);
                return null;
            }).addOnSuccessListener(x -> {
                Toast.makeText(getContext(), "Account created!", Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).showBottomNav();
                navigateToEvents();
            }).addOnFailureListener(err ->
                    Toast.makeText(getContext(), "Signup failed: " + err.getMessage(), Toast.LENGTH_LONG).show()
            );
        });
    }
    /**
     * Enables or disables all input fields and buttons based on authentication readiness.
     *
     * @param enabled true to enable UI interaction, false to disable it.
     */
    private void setUiEnabled(boolean enabled) {
        if (btnContinueLogin != null) btnContinueLogin.setEnabled(enabled && isNonBlank(getText(etNameLogin)));
        if (btnSignup != null)       btnSignup.setEnabled(enabled && isEmailOk(getText(etEmailSignup)) && isNonBlank(getText(etNameLogin)));
        if (etNameLogin != null)     etNameLogin.setEnabled(true); // allow typing anytime
        if (etEmailSignup != null)   etEmailSignup.setEnabled(enabled);
        if (etPhoneSignup != null)   etPhoneSignup.setEnabled(enabled);
    }


    // ---------- navigation ----------

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

    // ---------- helpers ----------

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
    /** Creates a simplified TextWatcher for live validation. */
    private TextWatcher simpleWatcher(OnChange cb) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { cb.run(String.valueOf(s)); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }
    /** Returns a unique device ID used as a backup identifier for anonymous users. */
    private String getDeviceId() {
        return Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }
    /** Functional interface used by {@link #simpleWatcher(OnChange)} for text change callbacks. */
    private interface OnChange { void run(String s); }
}