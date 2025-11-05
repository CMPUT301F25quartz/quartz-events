package com.example.ajilore.code.ui.events;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.data.Entrant;
import com.example.ajilore.code.ui.events.list.WaitingListAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WaitingListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WaitingListFragment extends Fragment {
    private static final String ARG_EVENT_ID = "eventId";

    private RecyclerView rvEntrants;
    private TextView tvTotal, tvAccepted, tvDeclined, tvPending, tvEmpty;
    private EditText etSearch;
    private WaitingListAdapter adapter;
    private List<Entrant> entrantList = new ArrayList<>();
    private FirebaseFirestore db;
    private String eventId;

    private ImageButton btnBack;


    public WaitingListFragment() {}

    public static WaitingListFragment newInstance(String eventId) {
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        WaitingListFragment fragment = new WaitingListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;
        if (eventId == null) {
            Toast.makeText(requireContext(), "Missing event ID", Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed();
            return;
        }

        db = FirebaseFirestore.getInstance();

        rvEntrants = view.findViewById(R.id.rvEntrants);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvAccepted = view.findViewById(R.id.tvAccepted);
        tvDeclined = view.findViewById(R.id.tvDeclined);
        tvPending = view.findViewById(R.id.tvPending);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        etSearch = view.findViewById(R.id.etSearch);

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(x -> requireActivity().onBackPressed());


        rvEntrants.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WaitingListAdapter(requireContext(), entrantList);
        rvEntrants.setAdapter(adapter);

        db.collection("org_events").document(eventId)
                .collection("waiting_list")
                .addSnapshotListener((snap, err) -> {
                    entrantList.clear();
                    int total = 0, accepted = 0, declined = 0, pending = 0;

                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String uid = d.getId();
                            String name = d.getString("name");
                            String status = d.getString("status");
                            String responded = d.getString("responded");

                            String displayStatus;
                            if ("chosen".equalsIgnoreCase(status) && "pending".equalsIgnoreCase(responded)) {
                                displayStatus = "Pending";
                            } else if ("selected".equalsIgnoreCase(status) && "accepted".equalsIgnoreCase(responded)) {
                                displayStatus = "Accepted";
                            } else if ("waiting".equalsIgnoreCase(status) && "declined".equalsIgnoreCase(responded)) {
                                displayStatus = "Declined";
                            } else if ("waiting".equalsIgnoreCase(status)) {
                                displayStatus = "Waiting";
                            } else {
                                // fallback if anything else
                                displayStatus = responded != null ? responded : "Pending";
                            }
                            entrantList.add(new Entrant(uid, name, displayStatus));

                            total++;
                            String safeResponded = responded != null ? responded.toLowerCase() : "pending";

                            switch (safeResponded) {
                                case "accepted": accepted++; break;
                                case "declined": declined++; break;
                                case "waiting" :
                                    pending++; break;
                                default: pending++; break;
                            }
                        }
                    }

                    adapter.updateList(entrantList);
                    tvEmpty.setVisibility(entrantList.isEmpty() ? View.VISIBLE : View.GONE);
                    tvTotal.setText(total + " Total");
                    tvAccepted.setText(accepted + " Accepted");
                    tvDeclined.setText(declined + " Declined");
                    tvPending.setText(pending + " Pending");
                });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterEntrants(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        });
    }

    private void filterEntrants(String query) {
        List<Entrant> filtered = new ArrayList<>();
        for (Entrant e : entrantList) {
            if (e.nameOrUid.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(e);
            }
        }
        adapter.updateList(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}