package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.google.firebase.firestore.*;

import java.util.*;

public class SelectEntrantsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";

    public static SelectEntrantsFragment newInstance(@NonNull String eventId,
                                                     @NonNull String eventTitle) {
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        b.putString(ARG_EVENT_TITLE, eventTitle);
        SelectEntrantsFragment f = new SelectEntrantsFragment();
        f.setArguments(b);
        return f;
    }

    // Firestore
    private FirebaseFirestore db;

    // Args
    private String eventId, eventTitle;

    // Views
    private TextView tvEventTitle, tvEmpty;
    private EditText etCount;
    private Button btnRunDraw;
    private RecyclerView rvSelected;
    private SelectedAdapter adapter;
    private final List<Entrant> selectedList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_entrants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        db = FirebaseFirestore.getInstance();
        Bundle args = getArguments();
        eventId = args != null ? args.getString(ARG_EVENT_ID) : null;
        eventTitle = args != null ? args.getString(ARG_EVENT_TITLE) : "";

        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed();
            return;
        }

        // Top bar
        ImageButton back = v.findViewById(R.id.btnBack);
        back.setOnClickListener(x -> requireActivity().onBackPressed());
        tvEventTitle = v.findViewById(R.id.tvEventTitle);
        tvEventTitle.setText(eventTitle);

        // Draw controls
        etCount = v.findViewById(R.id.etCount);
        btnRunDraw = v.findViewById(R.id.btnRunDraw);

        // Selected list
        tvEmpty = v.findViewById(R.id.tvEmpty);
        rvSelected = v.findViewById(R.id.rvSelected);
        rvSelected.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SelectedAdapter(selectedList);
        rvSelected.setAdapter(adapter);

        // Load current “selected (pending/accepted/declined)”
        listenForSelected();

        // Run draw
        btnRunDraw.setOnClickListener(x -> runDraw());
    }

    private void listenForSelected() {
        db.collection("org_events").document(eventId)
                .collection("entrants")
                .whereEqualTo("status", "chosen")   // <— was "selected"
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(requireContext(), "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    selectedList.clear();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String uid = d.getId();
                            String name = d.getString("name");        // or "displayName" if that’s what you saved
                            String responded = d.getString("responded"); // pending/accepted/declined
                            selectedList.add(new Entrant(uid, name, responded));
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(selectedList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }


    private void runDraw() {
        // How many winners?
        int n;
        try {
            String txt = etCount.getText() == null ? "" : etCount.getText().toString().trim();
            n = Integer.parseInt(TextUtils.isEmpty(txt) ? "0" : txt);
        } catch (NumberFormatException ex) {
            n = 0;
        }
        if (n <= 0) {
            Toast.makeText(requireContext(), "Enter how many entrants to pick.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRunDraw.setEnabled(false);
        final int numberofWinners = n;

        // Get everyone still on the waiting list
        db.collection("org_events").document(eventId)
                .collection("entrants")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) {
                        Toast.makeText(requireContext(), "No one on the waiting list.", Toast.LENGTH_SHORT).show();
                        btnRunDraw.setEnabled(true);
                        return;
                    }

                    // Shuffle and pick the first N
                    List<DocumentSnapshot> waiting = new ArrayList<>(snap.getDocuments());
                    Collections.shuffle(waiting, new Random());
                    List<DocumentSnapshot> winners = waiting.subList(0, Math.min(numberofWinners, waiting.size()));

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot d : winners) {
                        DocumentReference ref =
                                db.collection("org_events").document(eventId)
                                        .collection("entrants").document(d.getId());

                        Map<String, Object> upd = new HashMap<>();
                        upd.put("status", "chosen");              // was "selected"
                        upd.put("responded", "pending");          // waiting on accept
                        upd.put("selectedAt", FieldValue.serverTimestamp());
                        batch.set(ref, upd, SetOptions.merge());
                    }


                    batch.commit()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(requireContext(),
                                        "Selected " + winners.size() + " entrant(s).",
                                        Toast.LENGTH_SHORT).show();
                                btnRunDraw.setEnabled(true);

                                // Optional: jump to your Notify screen
                                // requireActivity().getSupportFragmentManager().popBackStack();
                                // requireActivity().getSupportFragmentManager().beginTransaction()
                                //     .replace(R.id.nav_host_fragment,
                                //         ManageEventsFragment.newInstance(eventId, eventTitle))
                                //     .addToBackStack(null).commit();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Draw failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                btnRunDraw.setEnabled(true);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Load waiting list failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnRunDraw.setEnabled(true);
                });
    }

    // --- tiny model
    static class Entrant {
        final String uid;
        final String nameOrUid;
        final String responded; // pending/accepted/declined/null

        Entrant(String uid, @Nullable String name, @Nullable String responded) {
            this.uid = uid;
            this.nameOrUid = TextUtils.isEmpty(name) ? uid : name;
            this.responded = TextUtils.isEmpty(responded) ? "pending" : responded;
        }
    }

    // --- adapter
    static class SelectedAdapter extends RecyclerView.Adapter<VH> {
        private final List<Entrant> items;
        SelectedAdapter(List<Entrant> items) { this.items = items; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_selected_entrant, p, false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos)); }
        @Override public int getItemCount() { return items.size(); }
    }




    static class VH extends RecyclerView.ViewHolder {
        private final TextView tvName, tvBadge;
        private final ImageView ivAvatar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
        void bind(Entrant e) {
            tvName.setText(e.nameOrUid);
            tvBadge.setText(cap(e.responded)); // Pending/Accepted/Declined
            // simple placeholder avatar
            ivAvatar.setImageResource(R.drawable.jazz);
        }
        private String cap(String s) { return s.substring(0,1).toUpperCase() + s.substring(1); }
    }
}
