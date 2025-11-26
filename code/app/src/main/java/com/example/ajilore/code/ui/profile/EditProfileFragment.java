package com.example.ajilore.code.ui.profile;

import android.os.Bundle;
import android.provider.Settings;
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
/**
 * {@code EditProfileFragment} allows a signed-in user to update their
 * personal information such as name, email, and phone number.
 * <p>
 * The fragment performs live validation to enable the "Save" button only
 * when at least one field has been modified. Updated data is written to
 * Firestore under the {@code users} collection, merged with the existing document.
 * </p>
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *     <li>Fetches the current Firebase user</li>
 *     <li>Enables dynamic validation using a {@link TextWatcher}</li>
 *     <li>Allows partial profile updates without overwriting unchanged fields</li>
 *     <li>Updates the Firestore user document with {@link SetOptions#merge()}</li>
 * </ul>
 *
 * <p><b>Limitations:</b></p>
 * <ul>
 *     <li>Does not currently re-validate email format</li>
 *     <li>No profile image editing functionality (future enhancement)</li>
 * </ul>
 *
 * @author
 *  Temi Akindele
 */
public class EditProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnSave, btnCancel;
    private FirebaseFirestore db;
    private String deviceId;


    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater  LayoutInflater to inflate the view.
     * @param container Parent container.
     * @param savedInstanceState Previously saved instance state (if any).
     * @return The inflated view for the edit profile screen.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }
    /**
     * Called once the view has been created.
     * <p>
     * Binds UI components, adds text watchers for validation, and defines the
     * save/cancel button actions. Updates are pushed to Firestore when the user
     * clicks "Save".
     * </p>
     *
     * @param v The root view.
     * @param savedInstanceState Saved state if available.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etName  = v.findViewById(R.id.etName);
        etEmail = v.findViewById(R.id.etEmail);
        etPhone = v.findViewById(R.id.etPhone);
        btnSave = v.findViewById(R.id.btnSave);
        btnCancel = v.findViewById(R.id.btnCancel);

        db = FirebaseFirestore.getInstance();

        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );


        TextWatcher watcher = new SimpleWatcher(() -> {
            // Enable save if at least one field has some text
            boolean any = notEmpty(etName) || notEmpty(etEmail) || notEmpty(etPhone);
            btnSave.setEnabled(any);
        });
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);

        btnCancel.setOnClickListener(v1 -> requireActivity().onBackPressed());

        btnSave.setOnClickListener(v1 -> saveChanges());

        // Initial state
        btnSave.setEnabled(false);
    }

    private void saveChanges() {
        Map<String, Object> patch = new HashMap<>();

        if (notEmpty(etName)) patch.put("name", get(etName));
        if (notEmpty(etEmail)) patch.put("email", get(etEmail));
        if (notEmpty(etPhone)) patch.put("phone", get(etPhone));

        if (patch.isEmpty()) {
            Toast.makeText(getContext(), "Nothing to update", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(deviceId)
                .set(patch, SetOptions.merge())
                .addOnSuccessListener(x -> {
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Update failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
    /**
     * Checks if the given text field contains non-empty input.
     *
     * @param et The {@link TextInputEditText} to check.
     * @return true if not empty, false otherwise.
     */
    private boolean notEmpty(TextInputEditText et) {
        return et.getText() != null && et.getText().toString().trim().length() > 0;
    }
    /**
     * Retrieves the trimmed string value from a text field.
     *
     * @param et The {@link TextInputEditText} to read.
     * @return Trimmed text, or an empty string if null.
     */
    private String get(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    /**
     * Simple {@link TextWatcher} helper class used for real-time validation.
     * <p>Whenever text changes in any input field, the provided {@link Runnable}
     * is executed (e.g., to enable or disable the Save button).</p>
     */
    private static class SimpleWatcher implements TextWatcher {
        private final Runnable r;
        SimpleWatcher(Runnable r) { this.r = r; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { r.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
