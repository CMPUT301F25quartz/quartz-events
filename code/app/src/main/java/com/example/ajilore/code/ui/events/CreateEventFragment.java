package com.example.ajilore.code.ui.events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ajilore.code.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateEventFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private EditText etTitle;
    private EditText etEventType;
    private Button btnSave;
    private FirebaseFirestore db;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateEventFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateEventFragment newInstance(String param1, String param2) {
        CreateEventFragment fragment = new CreateEventFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment, the views will exist after this
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //This helps to bind and attach listeners
        super.onViewCreated(view, savedInstanceState);

        etTitle = view.findViewById(R.id.etTitle);
        btnSave = view.findViewById(R.id.btnSave);
        etEventType = view.findViewById(R.id.etEventType);

        db = FirebaseFirestore.getInstance();

        btnSave.setOnClickListener(click -> {
            String title = etTitle.getText().toString().trim();
            String eventType = etEventType.getText().toString().trim();


            if (title.isEmpty()) {
                etTitle.setError("Title required");
                return;
            }

            Map<String, Object> event = new HashMap<>();
            event.put("title", title);
            //TODO: Use a real picker later
            event.put("startsAt", com.google.firebase.Timestamp.now());
            //TODO: Use a real poster later
            event.put("posterKey", "jazz");
            event.put("type", eventType);
            event.put("capacity", 100);
            event.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            event.put("status","published");

            btnSave.setEnabled(false);
            db.collection("org_events").add(event)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(requireContext(), "Event created ✅", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(err -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(requireContext(), "Failed to create: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

}