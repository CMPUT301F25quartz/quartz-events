package com.example.ajilore.code.ui.events;

import android.app.AlertDialog;
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
import com.example.ajilore.code.ui.events.EventNotifier;

/**
 * SelectEntrantsFragment
 *
 * Purpose: Organizer screen to (1) watch the current pool of CHOSEN entrants
 * and their responses, and (2) run a random draw that moves N users from
 * WAITING → CHOSEN (responded=pending).
 *
 * Pattern: Fragment as controller. Firestore is the single source of truth.
 * - A snapshot listener keeps the UI list in sync with entrants in Firestore.
 * - The "Run Draw" button reads WAITING entrants, shuffles, and writes updates in a batch.
 *
 * Additional Context:
 * waiting: User is on the event waiting list and not yet chosen/selected.
 *
 * chosen: User has been selected in the draw and invited—awaiting their response (pending).
 *
 * selected: User has accepted the invite—confirmed to attend.
 *
 * cancelled (or similar): User was chosen but declined, or was removed.
 *
 */


public class SelectEntrantsFragment extends Fragment {

    //Interface
    public interface OnCancelClick{
        void onCancel(Entrant entrant);

    }

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";

    /**
     * Creates a new fragment instance scoped to a single event.
     *
     * @param eventId   document id in /org_events
     * @param eventTitle title shown in the UI
     * @return configured fragment instance with arguments set
     */

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

    /**
     * Inflates the Select Entrants layout.
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_entrants, container, false);
    }


    /**
     * Wires UI, restores args, attaches a snapshot listener to CHOSEN entrants,
     * and hooks up the "Run Draw" button.
     */

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

// Adapter with cancel dialog + replacement draw
        adapter = new SelectedAdapter(selectedList, entrant -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cancel entrant?")
                    .setMessage("Are you sure you want to cancel this entrant?")
                    .setPositiveButton("Cancel Entrant", (dialog, which) -> {

                        String msg = "You were removed from "
                                + (eventTitle != null ? eventTitle : "this event")
                                + " by the organizer.";

                        // 1) send automatic message with status "removed"
                        EventNotifier.notifySingle(
                                db,
                                eventId,
                                eventTitle != null ? eventTitle : "",
                                entrant.uid,
                                "removed",          // status label for the notification
                                msg,
                                /* includePoster */ true,
                                /* linkUrl */ null,
                                new EventNotifier.Callback() {
                                    @Override
                                    public void onSuccess(int delivered, @NonNull String broadcastId) {
                                        // 2) now remove them from waiting_list
                                        db.collection("org_events").document(eventId)
                                                .collection("waiting_list").document(entrant.uid)
                                                .delete()
                                                .addOnSuccessListener(ve -> {
                                                    Toast.makeText(
                                                            requireContext(),
                                                            "Entrant removed and notified",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                    // pick 1 replacement if possible
                                                    SelectEntrantsFragment.drawReplacementsFromWaitingList(
                                                            db,
                                                            requireContext(),
                                                            eventId,
                                                            eventTitle,
                                                            1
                                                    );
                                                })
                                                .addOnFailureListener(err -> Toast.makeText(
                                                        requireContext(),
                                                        "Removed but failed to update list: " + err.getMessage(),
                                                        Toast.LENGTH_SHORT
                                                ).show());
                                    }

                                    @Override
                                    public void onError(@NonNull Exception e) {
                                        Toast.makeText(
                                                requireContext(),
                                                "Failed to notify entrant: " + e.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
                    })
                    .setNegativeButton("Keep", null)
                    .show();
        });

        rvSelected.setAdapter(adapter);


        // Load current “selected (pending/accepted/declined)”
        listenForSelected();

        // Run draw
        btnRunDraw.setOnClickListener(x -> runDraw());
    }


    /**
     * Subscribes to entrants with status == "chosen" for this event.
     * Updates the RecyclerView whenever Firestore changes.
     */

    private void listenForSelected() {
        db.collection("org_events").document(eventId)
                .collection("waiting_list")
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
                            String responded = d.getString("responded"); // pending/accepted/decline

                            // if declined, do not show them in the chosen list
                            if ("declined".equalsIgnoreCase(responded)) {
                                continue;
                            }

                            db.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        String userName = userDoc.getString("name");
                                        if(userName == null || userName.isEmpty()){
                                            userName = uid; // use the device id
                                            }
                                        selectedList.add(new Entrant(uid, userName, responded));
                                        adapter.notifyDataSetChanged();
                                        tvEmpty.setVisibility(selectedList.isEmpty() ? View.VISIBLE : View.GONE);

                                    }).addOnFailureListener(e1 -> {
                                        selectedList.add(new Entrant(uid, uid, responded));
                                        adapter.notifyDataSetChanged();
                                        tvEmpty.setVisibility(selectedList.isEmpty() ? View.VISIBLE : View.GONE);
                                    });
                                    }
                        } else {
                            //no chosen entrants
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(selectedList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });



    }

    /**
     * Runs a simple lottery:
     * - Reads all WAITING entrants,
     * - Shuffles them,
     * - Promotes the first N to CHOSEN with responded=pending.
     */

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
        final int numberOfWinners = n;

        // Get everyone still on the waiting list
        db.collection("org_events").document(eventId)
                .collection("waiting_list")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No one on the waiting list.",
                                Toast.LENGTH_SHORT).show();
                        btnRunDraw.setEnabled(true);
                        return;
                    }

                    // All waiting entrants
                    List<DocumentSnapshot> waiting = new ArrayList<>(snap.getDocuments());
                    Collections.shuffle(waiting, new Random());

                    int available = waiting.size();

                    //warning if they asked for more than available
                    if (numberOfWinners > available) {
                        Toast.makeText(
                                requireContext(),
                                "You asked to pick " + numberOfWinners
                                        + " entrants, but only " + available
                                        + " are left. Running draw for " + available + ".",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    int maxWinners = Math.min(numberOfWinners, available);
                    List<DocumentSnapshot> winners    = waiting.subList(0, maxWinners);
                    List<DocumentSnapshot> nonWinners = waiting.subList(maxWinners, waiting.size());


                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot d : winners) {
                        DocumentReference ref =
                                db.collection("org_events").document(eventId)
                                        .collection("waiting_list").document(d.getId());

                        Map<String, Object> upd = new HashMap<>();
                        upd.put("status", "chosen");
                        upd.put("responded", "pending");
                        upd.put("selectedAt", FieldValue.serverTimestamp());
                        batch.set(ref, upd, SetOptions.merge());
                    }

                    batch.commit()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(requireContext(),
                                        "Selected " + winners.size() + " entrant(s).",
                                        Toast.LENGTH_SHORT).show();
                                btnRunDraw.setEnabled(true);

                                // 1) Notify winners (lottery winners)
                                String chosenMsgTemplate =
                                        "You’ve been chosen in the lottery for "
                                                + (eventTitle != null ? eventTitle : "this event")
                                                + ". Please open the app to accept or decline your spot.";

                                for (DocumentSnapshot d : winners) {
                                    String uid = d.getId();

                                    EventNotifier.notifySingle(
                                            db,
                                            eventId,
                                            eventTitle != null ? eventTitle : "",
                                            uid,
                                            "chosen",
                                            chosenMsgTemplate,
                                            /* includePoster */ true,
                                            /* linkUrl */ null,
                                            new EventNotifier.Callback() {
                                                @Override
                                                public void onSuccess(int delivered, @NonNull String broadcastId) {
                                                    // optional log
                                                }

                                                @Override
                                                public void onError(@NonNull Exception e) {
                                                    // optional log
                                                }
                                            }
                                    );
                                }

                                // 2) Notify non-winners (still on waiting list)
                                if (!nonWinners.isEmpty()) {
                                    String notChosenMsgTemplate =
                                            "You were not selected in this lottery draw for "
                                                    + (eventTitle != null ? eventTitle : "this event")
                                                    + ", but you are still on the waiting list. "
                                                    + "If a spot opens up or another draw is run, you may be selected.";

                                    for (DocumentSnapshot d : nonWinners) {
                                        String uid = d.getId();

                                        EventNotifier.notifySingle(
                                                db,
                                                eventId,
                                                eventTitle != null ? eventTitle : "",
                                                uid,
                                                "waiting",   // they remain waiting
                                                notChosenMsgTemplate,
                                                /* includePoster */ true,
                                                /* linkUrl */ null,
                                                new EventNotifier.Callback() {
                                                    @Override
                                                    public void onSuccess(int delivered, @NonNull String broadcastId) { }

                                                    @Override
                                                    public void onError(@NonNull Exception e) { }
                                                }
                                        );
                                    }
                                }

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

    /**
     * Draws a single replacement from the waiting pool:
     * - Finds one WAITING entrant at random,
     * - Promotes them to CHOSEN (responded = pending),
     * - Sends them a notification.
     *
     * US 02.05.03: As an organizer I want to be able to draw a replacement
     * applicant from the pooling system when a previously selected applicant
     * cancels or rejects the invitation.
     */
    /**
     * Draws replacement entrants from the waiting pool.
     * - Looks for entrants with status == "waiting"
     * - Promotes up to numSlots of them to CHOSEN (responded = pending)
     * - Sends each a notification.
     */
    public static void drawReplacementsFromWaitingList(@NonNull FirebaseFirestore db,
                                                       @NonNull android.content.Context context,
                                                       @NonNull String eventId,
                                                       @Nullable String eventTitle,
                                                       int numSlots) {

        if (numSlots <= 0) return;

        db.collection("org_events").document(eventId)
                .collection("waiting_list")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) {
                        Toast.makeText(context,
                                "No one left on the waiting list.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DocumentSnapshot> waiting = new ArrayList<>(snap.getDocuments());
                    Collections.shuffle(waiting, new Random());

                    int winnersCount = Math.min(numSlots, waiting.size());
                    List<DocumentSnapshot> replacements = waiting.subList(0, winnersCount);

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot d : replacements) {
                        String uid = d.getId();
                        DocumentReference ref = db.collection("org_events")
                                .document(eventId)
                                .collection("waiting_list")
                                .document(uid);

                        Map<String, Object> upd = new HashMap<>();
                        upd.put("status", "chosen");
                        upd.put("responded", "pending");
                        upd.put("selectedAt", FieldValue.serverTimestamp());
                        batch.set(ref, upd, SetOptions.merge());
                    }

                    batch.commit()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(context,
                                        "Selected " + replacements.size() + " replacement entrant(s).",
                                        Toast.LENGTH_SHORT).show();

                                String msg = "A spot has opened up for "
                                        + (eventTitle != null ? eventTitle : "this event")
                                        + ". You have been selected from the waiting list. "
                                        + "Please open the app to accept or decline.";

                                for (DocumentSnapshot d : replacements) {
                                    String uid = d.getId();

                                    EventNotifier.notifySingle(
                                            db,
                                            eventId,
                                            eventTitle != null ? eventTitle : "",
                                            uid,
                                            "chosen",
                                            msg,
                                            /* includePoster */ true,
                                            /* linkUrl */ null,
                                            new EventNotifier.Callback() {
                                                @Override
                                                public void onSuccess(int delivered, @NonNull String broadcastId) { }

                                                @Override
                                                public void onError(@NonNull Exception e) { }
                                            }
                                    );
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context,
                                        "Failed to pick replacement(s): " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context,
                            "Failed to load waiting pool: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }







    /**
     * Lightweight model for a single entrant row in the list.
     * Holds display name (UID) and their response state.
     */
    static class Entrant {
        final String uid;
        final String nameOrUid;
        final String responded; // pending/accepted/declined/null


        /**
         * Builds an entrant view-model for the list.
         *
         * @param uid        document id under /entrants
         * @param name       display name (may be null/empty)
         * @param responded  pending/accepted/declined
         */

        Entrant(String uid, @Nullable String name, @Nullable String responded) {
            this.uid = uid;
            this.nameOrUid = TextUtils.isEmpty(name) ? uid : name;
            this.responded = TextUtils.isEmpty(responded) ? "pending" : responded;
        }
    }

    // --- adapter

    /**
     * Simple RecyclerView adapter that shows entrants in the CHOSEN list.
     */
    static class SelectedAdapter extends RecyclerView.Adapter<VH> {
        private final List<Entrant> items;
        private final OnCancelClick onCancelClick;
        SelectedAdapter(List<Entrant> items, OnCancelClick onCancelClick) {
            this.items = items;
            this.onCancelClick = onCancelClick;
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_selected_entrant, p, false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            h.bind(items.get(pos), onCancelClick);
        }
        @Override public int getItemCount() { return items.size(); }
    }



    /**
     * ViewHolder that binds an {@link Entrant} to a simple row:
     * name + a small symbol for response state.
     */
    static class VH extends RecyclerView.ViewHolder {
        private final TextView tvName, tvBadge;
        private final ImageView ivAvatar, ivCancel;

        /**
         * Constructs a ViewHolder for a selected entrant row view.
         *
         * @param itemView The inflated item view.
         */
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivCancel = itemView.findViewById(R.id.ivCancel);
        }
        /**
         * Binds an {@link Entrant}'s details to the row, including avatar and status label.
         *
         * @param e Entrant data for this row.
         */
        void bind(Entrant e, OnCancelClick onCancelClick) {
            tvName.setText(e.nameOrUid);
            // Pending/Accepted/Declined
            tvBadge.setText(cap(e.responded));
            // simple placeholder avatar
            ivAvatar.setImageResource(R.drawable.jazz);

            if ("accepted".equalsIgnoreCase(e.responded)) {
                ivCancel.setVisibility(View.GONE);
            } else {
                ivCancel.setVisibility(View.VISIBLE);
                ivCancel.setOnClickListener(x -> onCancelClick.onCancel(e));
            }
        }
        /**
         * Capitalizes the first letter of a response status.
         *
         * @param s The status string
         * @return String with the first character in uppercase.
         */
        private String cap(String s) { return s.substring(0,1).toUpperCase() + s.substring(1); }
    }
}
