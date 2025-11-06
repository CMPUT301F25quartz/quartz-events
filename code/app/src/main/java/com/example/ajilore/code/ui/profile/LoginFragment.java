package com.example.ajilore.code.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.EventsScreenFragment; // <-- using the simple screen
// If you prefer an existing concrete events fragment from your team,
// import that instead, e.g.:
// import com.example.ajilore.code.ui.events.EntrantEventsFragment;

import com.example.ajilore.code.ui.events.GeneralEventsFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private MaterialButtonToggleGroup toggleMode;
    private MaterialButton btnLogin, btnSignup, btnPrimary;
    private TextInputLayout tilName, tilEmail, tilPhone;
    private TextInputEditText etName, etEmail, etPhone;

    private enum Mode { LOGIN, SIGNUP }
    private Mode mode = Mode.LOGIN;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        toggleMode = v.findViewById(R.id.toggleMode);
        btnLogin   = v.findViewById(R.id.btnLogin);
        btnSignup  = v.findViewById(R.id.btnSignup);
        btnPrimary = v.findViewById(R.id.btnPrimary);

        tilName  = v.findViewById(R.id.tilName);
        tilEmail = v.findViewById(R.id.tilEmail);
        tilPhone = v.findViewById(R.id.tilPhone);

        etName = v.findViewById(R.id.etName);
        etEmail= v.findViewById(R.id.etEmail);
        etPhone= v.findViewById(R.id.etPhone);

        // Default = LOGIN
        toggleMode.check(R.id.btnLogin);
        setMode(Mode.LOGIN);

        toggleMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnLogin) setMode(Mode.LOGIN);
            else if (checkedId == R.id.btnSignup) setMode(Mode.SIGNUP);
        });

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { validate(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);

        btnPrimary.setOnClickListener(view -> {
            if (mode == Mode.LOGIN) {
                if (!isLoginValid()) {
                    Toast.makeText(getContext(), "Enter your name to log in", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Later: check Firestore for user by name
                navigateToEvents();
            } else { // SIGNUP
                if (!isSignupValid()) {
                    Toast.makeText(getContext(), "Enter your name and a valid email to sign up", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Later: create user in Firestore with name, email, (optional) phone
                navigateToEvents();
            }
        });
    }

    private void setMode(Mode m) {
        mode = m;
        boolean signup = (m == Mode.SIGNUP);

        tilEmail.setVisibility(signup ? View.VISIBLE : View.GONE);
        tilPhone.setVisibility(signup ? View.VISIBLE : View.GONE);

        btnPrimary.setText(signup ? "Sign up" : "Log in");
        validate();
    }

    private void validate() {
        if (mode == Mode.LOGIN) {
            tilName.setError(isNameOk() ? null : "Name is required");
            btnPrimary.setEnabled(isNameOk());
        } else {
            tilName.setError(isNameOk() ? null : "Name is required");
            tilEmail.setError(isEmailOk() ? null : "Invalid email");
            btnPrimary.setEnabled(isNameOk() && isEmailOk());
        }
    }

    private boolean isLoginValid() { return isNameOk(); }

    private boolean isSignupValid() { return isNameOk() && isEmailOk(); }

    private boolean isNameOk() {
        CharSequence s = etName.getText();
        return s != null && s.toString().trim().length() >= 1;
    }

    private boolean isEmailOk() {
        CharSequence s = etEmail.getText();
        return s != null && s.toString().contains("@");
    }

    private void navigateToEvents() {
        // Replace container with your events screen
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new GeneralEventsFragment())
                .commit();

        // If you'd rather use your teammate's concrete fragment, use:
        // .replace(R.id.nav_host_fragment, new EntrantEventsFragment())
    }
}
