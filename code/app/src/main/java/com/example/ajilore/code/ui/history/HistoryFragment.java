package com.example.ajilore.code.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HistoryFragment - Displays all events a user has registered for.
 *
 * US 01.02.03: As an entrant, I want to have a history of events
 * I have registered for, whether I was selected or not.
 */
public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private HistoryAdapter adapter;
    private List<Map<String, Object>> historyList = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        recyclerView = v.findViewById(R.id.recyclerHistory);
        progressBar = v.findViewById(R.id.progressHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadHistory();
    }

    private void loadHistory() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .document(uid)
                .collection("registrations")
                .orderBy("registeredAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    historyList.clear();
                    for (DocumentSnapshot doc : query) {
                        historyList.add(doc.getData());
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
