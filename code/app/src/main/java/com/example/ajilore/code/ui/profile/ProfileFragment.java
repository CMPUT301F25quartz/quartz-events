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

import com.example.ajilore.code.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.ajilore.code.ui.profile.Entrant;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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
    private Entrant original;  // what we loaded from Firestore last
    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId; // auth UID (or "demoUser1" during dev)



    // Local state
    private boolean editMode = false;

    // Fragment args (kept from template)
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // 1) Bind views (IDs from fragment_profile.xml)
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

        // 2) Prefill demo values so you can see the UI changing (replace when loading from DB)
        tvDisplayName.setText("John");
        chipRole.setText("Entrant");
        etName.setText("John Doe");
        etEmail.setText("john@gmail.com");
        etPhone.setText("+1 234 567 8900");

// Quick auth so each device has a UID; swap to email/password later if you want


        // 3) Start in view mode (fields disabled; only Edit shown)
        setEditMode(false);

        // 4) Button actions
        btnEditProfile.setOnClickListener(view -> setEditMode(true));

        btnCancel.setOnClickListener(view -> {
            clearErrors();
            setEditMode(false); // discard edits visually (DB load will replace defaults later)
        });

        btnSave.setOnClickListener(view -> {
            if (!isFormValid()) {
                Toast.makeText(getContext(), "Please fix validation errors", Toast.LENGTH_SHORT).show();
                return;
            }
            // For now: update header name and exit edit mode (DB save comes later)
            tvDisplayName.setText(etName.getText());
            clearErrors();
            setEditMode(false);
            Toast.makeText(getContext(), "Saved (local only for now)", Toast.LENGTH_SHORT).show();
        });

        // 5) Live validation → enable Save only when valid
        TextWatcher watcher = simpleWatcher(s -> {
            btnSave.setEnabled(isFormValid());
            // inline errors
            tilName.setError(isNameOk() ? null : "Name required");
            tilEmail.setError(isEmailOk() ? null : "Must contain @");
//            tilPhone.setError(isPhoneOk() ? null);
        });
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);
    }

    // ----------------- helpers -----------------

    private void setEditMode(boolean enabled) {
        editMode = enabled;
        etName.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etPhone.setEnabled(enabled);

        btnEditProfile.setVisibility(enabled ? View.GONE : View.VISIBLE);
        buttonRow.setVisibility(enabled ? View.VISIBLE : View.GONE);

        // recompute Save state when entering edit mode
        btnSave.setEnabled(enabled && isFormValid());
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
    }

    private boolean isFormValid() { return isNameOk() && isEmailOk() && isPhoneOk(); }

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
        // Phone number is optional — always valid unless it has weird symbols
        if (t.isEmpty()) return true;
        // Basic sanity check (optional): allow numbers, spaces, +, -, ()
        return t.matches("[0-9+()\\-\\s]*");
    }
    private void bindEntrant(Entrant e) {
        // Save this as the "original" version for later comparison
        original = new Entrant();
        original.setName(e.getName());
        original.setEmail(e.getEmail());
        original.setPhone(e.getPhone());

        // Fill in all UI fields
        etName.setText(e.getName());
        etEmail.setText(e.getEmail());
        etPhone.setText(e.getPhone());
        tvDisplayName.setText(e.getName() == null ? "" : e.getName());
    }



    // Small TextWatcher helper that calls back on changes (no Java 8 Consumer needed)
    private TextWatcher simpleWatcher(OnChange cb) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { cb.run(String.valueOf(s)); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private interface OnChange { void run(String s); }
}
