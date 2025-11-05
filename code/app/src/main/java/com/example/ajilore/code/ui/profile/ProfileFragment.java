package com.example.ajilore.code.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
// Uncomment these when you’re ready to wire Firebase again
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    // UI refs
    private MaterialToolbar profileToolbar;
    private TextView tvDisplayName;
    private Chip chipRole;
    private TextInputLayout tilName, tilEmail, tilPhone;
    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnEditProfile, btnSave, btnCancel;
    private LinearLayout buttonRow;
    private MaterialSwitch switchNotifications;

    // Auth buttons
    private MaterialButtonToggleGroup toggleAuthMode;
    private MaterialButton btnModeLogin, btnModeSignup;
    private MaterialButton btnPrimary;

    // Firebase (disabled for now to guarantee nav works)
//    private FirebaseAuth auth;
//    private FirebaseFirestore db;
//    private String userId;
//    private boolean authReady = false;

    // Modes
    private enum Mode { LOGIN, SIGNUP, EDIT }
    private Mode mode = Mode.LOGIN;

    public ProfileFragment() { }

    public static ProfileFragment newInstance(String p1, String p2) {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        b.putString("param1", p1);
        b.putString("param2", p2);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Bind views
        profileToolbar = v.findViewById(R.id.profileToolbar);
        tvDisplayName  = v.findViewById(R.id.tvDisplayName);
        chipRole       = v.findViewById(R.id.chipRole);
        tilName  = v.findViewById(R.id.tilName);
        tilEmail = v.findViewById(R.id.tilEmail);
        tilPhone = v.findViewById(R.id.tilPhone);
        etName   = v.findViewById(R.id.etName);
        etEmail  = v.findViewById(R.id.etEmail);
        etPhone  = v.findViewById(R.id.etPhone);
        btnEditProfile = v.findViewById(R.id.btnEditProfile);
        btnSave        = v.findViewById(R.id.btnSave);
        btnCancel      = v.findViewById(R.id.btnCancel);
        buttonRow      = v.findViewById(R.id.buttonRow);
        switchNotifications = v.findViewById(R.id.switchNotifications);
        toggleAuthMode = v.findViewById(R.id.toggleAuthMode);
        btnModeLogin   = v.findViewById(R.id.btnModeLogin);
        btnModeSignup  = v.findViewById(R.id.btnModeSignup);
        btnPrimary     = v.findViewById(R.id.btnPrimary);

        // If you want Firebase later, re-enable this block:
//        auth = FirebaseAuth.getInstance();
//        db   = FirebaseFirestore.getInstance();
//        if (auth.getCurrentUser() == null) {
//            auth.signInAnonymously().addOnCompleteListener(t -> {
//                if (t.isSuccessful()) {
//                    userId = auth.getCurrentUser().getUid();
//                    authReady = true;
//                    loadProfile();
//                } else {
//                    Toast.makeText(getContext(), "Auth failed", Toast.LENGTH_LONG).show();
//                }
//            });
//        } else {
//            userId = auth.getCurrentUser().getUid();
//            authReady = true;
//            loadProfile();
//        }

        // Start in LOGIN mode
        setMode(Mode.LOGIN);

        // Switch Login ↔ Signup
        toggleAuthMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnModeLogin) setMode(Mode.LOGIN);
            else if (checkedId == R.id.btnModeSignup) setMode(Mode.SIGNUP);
        });

        // 🔹 Primary button (Log in / Sign up) — FORCE NAV TO HOME
        btnPrimary.setOnClickListener(view -> {
            if (!isFormValid()) {
                Toast.makeText(getContext(), "Please fix validation errors", Toast.LENGTH_SHORT).show();
                return;
            }

            // Optional UI polish
            String nameNow = getText(etName);
            tvDisplayName.setText(nameNow.isEmpty() ? "Welcome" : nameNow);
            chipRole.setText("Entrant");

            Toast.makeText(getContext(), (mode == Mode.SIGNUP ? "Signed up" : "Logged in") + " — opening Home…", Toast.LENGTH_SHORT).show();

            // 🚀 Jump to Home immediately
            ((MainActivity) requireActivity()).openHomeDirect();

            // --- Re-enable Firestore later if needed ---
            /*
            if (!authReady || userId == null) {
                Toast.makeText(getContext(), "Please wait, setting up your account…", Toast.LENGTH_SHORT).show();
                return;
            }
            String email = getText(etEmail);
            String phone = getText(etPhone);
            Map<String,Object> data = new HashMap<>();
            data.put("name",  nameNow);
            data.put("email", email);
            data.put("phone", phone);
            data.put("role",  "entrant");
            db.collection("users").document(userId)
              .set(data, SetOptions.merge())
              .addOnSuccessListener(x -> ((MainActivity) requireActivity()).openHomeDirect())
              .addOnFailureListener(err ->
                  Toast.makeText(getContext(), "Failed: " + err.getMessage(), Toast.LENGTH_LONG).show()
              );
            */
        });

        // 🔹 Edit profile mode (email + phone only)
        btnEditProfile.setOnClickListener(view -> setMode(Mode.EDIT));

        btnCancel.setOnClickListener(view -> {
            clearErrors();
            // loadProfile(); // re-enable when Firestore is on
            setMode(Mode.LOGIN);
        });

        // 🔹 Save edited info (kept; will be useful once Firestore is on)
        btnSave.setOnClickListener(view -> {
            if (!isFormValidForEdit()) {
                Toast.makeText(getContext(), "Please fix email/phone", Toast.LENGTH_SHORT).show();
                return;
            }
            // Re-enable Firestore write later
            Toast.makeText(getContext(), "Profile updated (local)", Toast.LENGTH_SHORT).show();
            clearErrors();
            setMode(Mode.LOGIN);
        });

        // 🔹 Live validation
        TextWatcher watcher = simpleWatcher(s -> {
            if (mode == Mode.EDIT) {
                tilEmail.setError(isEmailOk() ? null : "Must contain @");
                tilPhone.setError(isPhoneOk() ? null : "Invalid characters");
                btnSave.setEnabled(isFormValidForEdit());
            } else {
                tilName.setError(isNameOk() ? null : "Name required");
                tilEmail.setError(isEmailOk() ? null : "Must contain @");
                tilPhone.setError(isPhoneOk() ? null : "Invalid characters");
                btnPrimary.setEnabled(isFormValid());
            }
        });
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);
    }

    // ---------- helpers ----------

    private void setMode(Mode m) {
        mode = m;
        boolean inEdit = (m == Mode.EDIT);

        etName.setEnabled(!inEdit);
        etEmail.setEnabled(true);
        etPhone.setEnabled(true);

        btnPrimary.setVisibility(inEdit ? View.GONE : View.VISIBLE);
        btnPrimary.setText(m == Mode.SIGNUP ? "Sign up" : "Log in");
        btnPrimary.setEnabled(!inEdit && isFormValid());

        buttonRow.setVisibility(inEdit ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(inEdit && isFormValidForEdit());
        btnEditProfile.setVisibility(inEdit ? View.GONE : View.VISIBLE);

        String nameNow = getText(etName);
        tvDisplayName.setText(nameNow.isEmpty() ? "Welcome" : nameNow);
    }

    // Re-enable this when you wire Firestore
//    private void loadProfile() {
//        if (userId == null) return;
//        db.collection("users").document(userId).get()
//          .addOnSuccessListener(doc -> {
//              if (!doc.exists()) return;
//              String name  = doc.getString("name");
//              String email = doc.getString("email");
//              String phone = doc.getString("phone");
//              String role  = doc.getString("role");
//
//              etName.setText(name == null ? "" : name);
//              etEmail.setText(email == null ? "" : email);
//              etPhone.setText(phone == null ? "" : phone);
//              tvDisplayName.setText(name == null || name.isEmpty() ? "Welcome" : name);
//              chipRole.setText(role == null || role.isEmpty() ? "Entrant" : role);
//
//              setMode(Mode.LOGIN);
//          })
//          .addOnFailureListener(err ->
//              Toast.makeText(getContext(), "Failed to load profile: " + err.getMessage(), Toast.LENGTH_LONG).show()
//          );
//    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
    }

    private boolean isFormValid() { return isNameOk() && isEmailOk() && isPhoneOk(); }
    private boolean isFormValidForEdit() { return isEmailOk() && isPhoneOk(); }

    private boolean isNameOk() {
        CharSequence s = etName.getText();
        return s != null && s.toString().trim().length() >= 1;
    }

    private boolean isEmailOk() {
        CharSequence s = etEmail.getText();
        return s != null && s.toString().contains("@");
    }

    private boolean isPhoneOk() {
        CharSequence s = etPhone.getText();
        if (s == null) return true;
        String t = s.toString().trim();
        return t.isEmpty() || t.matches("[0-9+()\\-\\s]*");
    }

    private String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private TextWatcher simpleWatcher(OnChange cb) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { cb.run(String.valueOf(s)); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private interface OnChange { void run(String s); }
}
