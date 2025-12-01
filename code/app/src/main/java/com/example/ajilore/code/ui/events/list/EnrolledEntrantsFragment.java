package com.example.ajilore.code.ui.events.list;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ajilore.code.R;
import com.example.ajilore.code.adapters.EntrantAdapter;
import com.example.ajilore.code.models.Entrant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EnrolledEntrantsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private List<Entrant> entrants;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FirebaseFirestore db;

    private String eventId;
    
    private ImageButton btnExportCsv;

    public static EnrolledEntrantsFragment newInstance(@NonNull String eventId) {
        Bundle b = new Bundle();
        b.putString("eventId", eventId);      // store the event id in arguments
        EnrolledEntrantsFragment f = new EnrolledEntrantsFragment();
        f.setArguments(b);
        return f;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        entrants = new ArrayList<>();

        Bundle args = getArguments();
        eventId = args != null ? args.getString("eventId") : null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enrolled_entrants, container, false);

        initViews(view);
        setupRecyclerView();
        loadEnrolledEntrants();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_enrolled_entrants);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.text_empty);
        btnExportCsv = view.findViewById(R.id.btn_export_csv);
        //btnExportCsv.setOnClickListener(v -> exportCsv());
    }


    private void setupRecyclerView() {
        adapter = new EntrantAdapter("enrolled");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnEntrantClickListener(entrant -> {
            // Handle entrant click - you can show details or other actions
        });
    }

    private void loadEnrolledEntrants() {
        showLoading(true);

        db.collection("org_events").document(eventId)
                .collection("waiting_list")
                .whereEqualTo("status", "chosen")
                .whereEqualTo("responded", "accepted")
                .orderBy("joinedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        showLoading(false);
                        return;
                    }

                    if (snapshot != null) {
                        entrants.clear();
                        for (DocumentSnapshot doc : snapshot) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                entrant.setId(doc.getId());
                                entrants.add(entrant);
                            }
                        }

                        adapter.setEntrants(entrants);
                        updateEmptyView();
                        btnExportCsv.setVisibility(entrants.isEmpty() ? View.GONE: View.VISIBLE);
                        showLoading(false);
                    }
                });
    }

    private void updateEmptyView() {
        if (entrants.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}