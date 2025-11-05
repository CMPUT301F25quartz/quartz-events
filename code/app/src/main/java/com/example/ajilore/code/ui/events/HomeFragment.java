package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // UI
    private MaterialToolbar homeToolbar;
    private TextView tvGreeting;
    private MaterialButton btnViewHistory;
    private RecyclerView rvAvailableEvents; // placeholder for later

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Bind views from fragment_events_home.xml
        homeToolbar       = v.findViewById(R.id.homeToolbar);
        tvGreeting        = v.findViewById(R.id.tvGreeting);
        btnViewHistory    = v.findViewById(R.id.btnViewHistory);
        rvAvailableEvents = v.findViewById(R.id.rvAvailableEvents);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Ensure we have a user (anonymous OK for dev)
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) loadGreeting();
                else Toast.makeText(getContext(), "Auth failed", Toast.LENGTH_LONG).show();
            });
        } else {
            loadGreeting();
        }

        // Overflow menu actions
        homeToolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_switch_organizer) {
                updateRole("organizer");
                return true;
            } else if (id == R.id.action_switch_admin) {
                updateRole("admin");
                return true;
            } else if (id == R.id.action_delete_profile) {
                confirmDeleteProfile();
                return true;
            } else if (id == R.id.action_sign_out) {
                signOut();
                return true;
            }
            return false;
        });

        // History button → switch to the History tab via MainActivity helper
        btnViewHistory.setOnClickListener(click ->
                ((MainActivity) requireActivity()).switchToHistory()
        );
    }

    // --- helpers ---

    private void loadGreeting() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    if (name == null || name.trim().isEmpty()) {
                        tvGreeting.setText("Welcome!");
                    } else {
                        tvGreeting.setText("Welcome, " + name + "!");
                    }
                })
                .addOnFailureListener(err -> tvGreeting.setText("Welcome!"));
    }

    private void updateRole(String role) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Not signed in", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .update("role", role)
                .addOnSuccessListener(v ->
                        Toast.makeText(getContext(), "Switched to " + role, Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(err ->
                        Toast.makeText(getContext(), "Role update failed: " + err.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void signOut() {
        if (auth != null) auth.signOut();
        Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT).show();
        // Send them back to Profile tab to “log in” again via profile screen
        ((MainActivity) requireActivity()).switchToProfile();
    }

    private void confirmDeleteProfile() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete profile?")
                .setMessage("This will remove your profile and registration history. This cannot be undone.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> deleteProfileNow())
                .show();
    }

    private void deleteProfileNow() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Not signed in", Toast.LENGTH_LONG).show();
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        // 1) delete users/{uid}/registrations/*
        db.collection("users").document(uid).collection("registrations")
                .get().addOnSuccessListener(snap -> {
                    List<com.google.android.gms.tasks.Task<Void>> deletes = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        deletes.add(d.getReference().delete());
                    }
                    Tasks.whenAllComplete(deletes).addOnSuccessListener(v -> {
                        // 2) delete users/{uid}
                        db.collection("users").document(uid).delete()
                                .addOnSuccessListener(v2 -> {
                                    // 3) delete auth user
                                    auth.getCurrentUser().delete()
                                            .addOnSuccessListener(v3 -> {
                                                Toast.makeText(getContext(), "Profile deleted", Toast.LENGTH_LONG).show();
                                                requireActivity().finish(); // or navigate to sign-in
                                            })
                                            .addOnFailureListener(err ->
                                                    Toast.makeText(getContext(),
                                                            "Data deleted, but account deletion failed: " + err.getMessage(),
                                                            Toast.LENGTH_LONG).show()
                                            );
                                })
                                .addOnFailureListener(err ->
                                        Toast.makeText(getContext(),
                                                "Failed to delete user doc: " + err.getMessage(),
                                                Toast.LENGTH_LONG).show()
                                );
                    });
                }).addOnFailureListener(err ->
                        Toast.makeText(getContext(),
                                "Failed to load registrations: " + err.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}
