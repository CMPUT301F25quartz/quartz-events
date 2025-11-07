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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnSave, btnCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etName  = v.findViewById(R.id.etName);
        etEmail = v.findViewById(R.id.etEmail);
        etPhone = v.findViewById(R.id.etPhone);
        btnSave = v.findViewById(R.id.btnSave);
        btnCancel = v.findViewById(R.id.btnCancel);



        TextWatcher watcher = new SimpleWatcher(() -> {
            // Enable save if at least one field has some text
            boolean any = notEmpty(etName) || notEmpty(etEmail) || notEmpty(etPhone);
            btnSave.setEnabled(any);
        });
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);

        btnCancel.setOnClickListener(v1 -> requireActivity().onBackPressed());

        btnSave.setOnClickListener(v12 -> {
            var user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Not signed in", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = user.getUid();

            Map<String, Object> patch = new HashMap<>();
            String name  = get(etName);
            String email = get(etEmail);
            String phone = get(etPhone);

            if (!name.isEmpty()) {
                patch.put("name", name);
                patch.put("nameLower", name.toLowerCase(Locale.ROOT));
            }
            if (!email.isEmpty()) patch.put("email", email);
            if (!phone.isEmpty()) patch.put("phone", phone);

            if (patch.isEmpty()) {
                Toast.makeText(getContext(), "Nothing to update", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .set(patch, SetOptions.merge())
                    .addOnSuccessListener(x -> {
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        // Initial state
        btnSave.setEnabled(false);
    }

    private boolean notEmpty(TextInputEditText et) {
        return et.getText() != null && et.getText().toString().trim().length() > 0;
    }

    private String get(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static class SimpleWatcher implements TextWatcher {
        private final Runnable r;
        SimpleWatcher(Runnable r) { this.r = r; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { r.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
