package com.example.ajilore.code.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private MaterialButtonToggleGroup toggleMode;
    private MaterialButton btnModeLogin, btnModeSignup;
    private TextInputLayout tilName, tilEmail, tilPhone;
    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnPrimary;

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

        toggleMode   = v.findViewById(R.id.toggleMode);
        btnModeLogin = v.findViewById(R.id.btnModeLogin);
        btnModeSignup= v.findViewById(R.id.btnModeSignup);
        tilName  = v.findViewById(R.id.tilName);
        tilEmail = v.findViewById(R.id.tilEmail);
        tilPhone = v.findViewById(R.id.tilPhone);
        etName   = v.findViewById(R.id.etName);
        etEmail  = v.findViewById(R.id.etEmail);
        etPhone  = v.findViewById(R.id.etPhone);
        btnPrimary = v.findViewById(R.id.btnPrimary);

        // Default mode = LOGIN
        setMode(Mode.LOGIN);

        toggleMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            setMode(checkedId == R.id.btnModeLogin ? Mode.LOGIN : Mode.SIGNUP);
        });

        // Validation watcher
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { updateValidation(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);

        btnPrimary.setOnClickListener(view -> {
            if (mode == Mode.LOGIN) {
                if (!isLoginValid()) {
                    Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(), "Welcome " + getText(etName), Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).switchToEvents();

            } else { // SIGNUP
                if (!isSignupValid()) {
                    Toast.makeText(getContext(), "Please enter your name and a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).switchToEvents();
            }
        });
    }

    private void setMode(Mode m) {
        mode = m;
        tilEmail.setVisibility(m == Mode.SIGNUP ? View.VISIBLE : View.GONE);
        tilPhone.setVisibility(m == Mode.SIGNUP ? View.VISIBLE : View.GONE);
        btnPrimary.setText(m == Mode.LOGIN ? "Continue" : "Create account");
        clearErrors();
        updateValidation();

        if (m == Mode.LOGIN) toggleMode.check(R.id.btnModeLogin);
        else toggleMode.check(R.id.btnModeSignup);
    }

    private void updateValidation() {
        if (mode == Mode.LOGIN) {
            tilName.setError(isNameOk() ? null : "Name is required");
            btnPrimary.setEnabled(isNameOk());
        } else {
            tilName.setError(isNameOk() ? null : "Name is required");
            tilEmail.setError(isEmailOk() ? null : "Invalid email");
            btnPrimary.setEnabled(isSignupValid());
        }
    }

    private boolean isLoginValid() {
        return isNameOk();
    }

    private boolean isSignupValid() {
        return isNameOk() && isEmailOk();
    }

    private boolean isNameOk() {
        CharSequence s = etName.getText();
        return s != null && s.toString().trim().length() >= 1;
    }

    private boolean isEmailOk() {
        CharSequence s = etEmail.getText();
        return s != null && s.toString().contains("@");
    }

    private String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
    }
}
