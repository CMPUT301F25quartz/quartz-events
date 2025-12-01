package com.example.ajilore.code.ui.profile;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
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
 * {@code EditProfileFragment} allows an authenticated user to update their
 * personal information stored in Firestore, including name, email, phone
 * number, and profile image.
 *
 * <p>This fragment supports partial updates using {@link SetOptions#merge()},
 * meaning only modified fields are written to Firestore while all other
 * user data remains untouched.</p>
 *
 * <h3>Features</h3>
 * <ul>
 *     <li>Fetches the device's unique ID to identify the user document</li>
 *     <li>Real-time validation using a lightweight {@link TextWatcher}</li>
 *     <li>Save button becomes enabled only when the user modifies at least one field</li>
 *     <li>Profile picture updates supported through randomly generated images from Pravatar</li>
 *     <li>Uses Glide to preview selected profile images</li>
 * </ul>
 *
 * @author
 *     Temi Akindele
 */

public class EditProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnSave, btnCancel, btnRandomPhoto;
    private FirebaseFirestore db;
    private String deviceId;

    private String chosenImageUrl = null;
    private ImageView ivProfile;


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
     * Called after the view hierarchy has been created.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Binds UI components (input fields, buttons, image preview)</li>
     *     <li>Initializes Firestore</li>
     *     <li>Retrieves the device's ID to locate the user document</li>
     *     <li>Configures live validation for input fields to enable the Save button</li>
     *     <li>Sets up click listeners for Cancel, Save, and Random Photo actions</li>
     * </ul>
     *
     * @param v The root view returned by {@link #onCreateView}
     * @param savedInstanceState Saved UI state if available
     */

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etName  = v.findViewById(R.id.etName);
        etEmail = v.findViewById(R.id.etEmail);
        etPhone = v.findViewById(R.id.etPhone);
        btnSave = v.findViewById(R.id.btnSave);
        btnCancel = v.findViewById(R.id.btnCancel);
        ivProfile = v.findViewById(R.id.ivProfile);
        btnRandomPhoto = v.findViewById(R.id.btnRandomPhoto);

        db = FirebaseFirestore.getInstance();

        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );


        TextWatcher watcher = new SimpleWatcher(() -> {
            // Enable save if at least one field has some text
            boolean any = notEmpty(etName) || notEmpty(etEmail) || notEmpty(etPhone) || chosenImageUrl != null;
            btnSave.setEnabled(any);
        });
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);

        btnCancel.setOnClickListener(v1 -> requireActivity()
                .getSupportFragmentManager()
                .popBackStack());
        ;

        btnSave.setOnClickListener(v1 -> saveChanges());

        // Random Photo button (Pravatar)
        btnRandomPhoto.setOnClickListener(v1 -> {
            int num = (int) (Math.random() * 70) + 1;  // 1–70
            chosenImageUrl = "https://i.pravatar.cc/150?img=" + num;

            // Show preview
            Glide.with(this)
                    .load(chosenImageUrl)
                    .circleCrop()
                    .into(ivProfile);

            btnSave.setEnabled(true);
        });
        // Initial state
        btnSave.setEnabled(false);
    }

    /**
     * Collects all modified fields and writes them to Firestore.
     *
     * <p>Only fields with user-modified values are included in the update map.
     * Firestore writes use {@link SetOptions#merge()} to avoid overwriting other
     * unrelated profile information.</p>
     *
     * <p>If no changes were made, a message is shown and the update is skipped.</p>
     */

    private void saveChanges() {
        Map<String, Object> patch = new HashMap<>();

        if (notEmpty(etName)) patch.put("name", get(etName));
        if (notEmpty(etEmail)) patch.put("email", get(etEmail));
        if (notEmpty(etPhone)) patch.put("phone", get(etPhone));

        // Only update profile picture if user picked one
        if (chosenImageUrl != null) {
            patch.put("profilepicture", chosenImageUrl);
        }

        if (patch.isEmpty()) {
            Toast.makeText(getContext(), "Nothing to update", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(deviceId)
                .set(patch, SetOptions.merge())
                .addOnSuccessListener(x -> {
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack();
                    ;
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Update failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
    /**
     * Returns whether the given text field contains non-empty, non-whitespace input.
     *
     * @param et The {@link TextInputEditText} to inspect
     * @return {@code true} if the field contains a value, {@code false} otherwise
     */
    private boolean notEmpty(TextInputEditText et) {
        return et.getText() != null && et.getText().toString().trim().length() > 0;
    }
    /**
     * Retrieves the trimmed text from a {@link TextInputEditText}.
     *
     * @param et The edit text field
     * @return A trimmed string value, or an empty string if null
     */
    private String get(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    /**
     * A lightweight {@link TextWatcher} implementation that executes a provided
     * {@link Runnable} whenever text changes in any monitored field.
     *
     * <p>Used to dynamically enable or disable the Save button based on whether
     * the user has modified any profile fields.</p>
     */
    private static class SimpleWatcher implements TextWatcher {
        private final Runnable r;
        SimpleWatcher(Runnable r) { this.r = r; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { r.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
