package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
//will include the import statement once we can authenticate
//import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.R;
import java.util.HashMap;
import java.util.Map;

public class ManageEventsFragment extends Fragment {

    // ---- args ----
    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";

    public static ManageEventsFragment newInstance(@NonNull String eventId, @NonNull String eventTitle) {
        ManageEventsFragment f = new ManageEventsFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        b.putString(ARG_EVENT_TITLE, eventTitle);
        f.setArguments(b);
        return f;
    }

    // ---- state ----
    private enum Audience { NONE, WAITING, SELECTED, NOT_SELECTED }
    private Audience audience = Audience.NONE;
    private boolean notifyMode = false;

    // ---- views ----
    private View cardNotify;
    private ToggleButton tgWaiting, tgSelected, tgNotSelected;
    private Button btnNotifyTop;
    private EditText etMessage;
    private TextView tvCounter, tvTitle;
    private CheckBox cbAddLink, cbAddPoster;
    private Button btnSend;

    // ---- data ----
    private String eventId, eventTitle;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_events_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // Firestore
        db = FirebaseFirestore.getInstance();

        // args
        Bundle args = getArguments();
        eventId = args != null ? args.getString(ARG_EVENT_ID) : null;
        eventTitle = args != null ? args.getString(ARG_EVENT_TITLE) : "";

        // top bar
        tvTitle = v.findViewById(R.id.tvEventTitle);
        tvTitle.setText(eventTitle);
        ImageButton back = v.findViewById(R.id.btnBack);
        back.setOnClickListener(x -> requireActivity().onBackPressed());

        // action buttons
        Button btnWaitingTop = v.findViewById(R.id.btnWaitingList);
        Button btnSelectTop  = v.findViewById(R.id.btnSelectEntrants);
        Button btnQR         = v.findViewById(R.id.btnQR);
        btnNotifyTop         = v.findViewById(R.id.btnNotifyEntrants);
        Button btnEditTop    = v.findViewById(R.id.btnEditEvent);

        // type pills
        tgWaiting    = v.findViewById(R.id.tgWaiting);
        tgSelected   = v.findViewById(R.id.tgSelected);
        tgNotSelected= v.findViewById(R.id.tgNotSelected);

        // notify card
        cardNotify   = v.findViewById(R.id.cardNotify);
        etMessage    = v.findViewById(R.id.etMessage);
        tvCounter    = v.findViewById(R.id.tvCounter);
        cbAddLink    = v.findViewById(R.id.cbAddLink);
        cbAddPoster  = v.findViewById(R.id.cbAddPoster);
        btnSend      = v.findViewById(R.id.btnSend);

        // start state
        tgWaiting.setChecked(false);
        tgSelected.setChecked(false);
        tgNotSelected.setChecked(false);
        audience = Audience.NONE;
        notifyMode = false;
        updatePanel();

        // pill clicks (exclusive)
        View.OnClickListener pillClick = pill -> {
            tgWaiting.setChecked(pill.getId() == R.id.tgWaiting);
            tgSelected.setChecked(pill.getId() == R.id.tgSelected);
            tgNotSelected.setChecked(pill.getId() == R.id.tgNotSelected);

            if (tgWaiting.isChecked())        audience = Audience.WAITING;
            else if (tgSelected.isChecked())  audience = Audience.SELECTED;
            else if (tgNotSelected.isChecked()) audience = Audience.NOT_SELECTED;
            else                               audience = Audience.NONE;

            updatePanel();
        };
        tgWaiting.setOnClickListener(pillClick);
        tgSelected.setOnClickListener(pillClick);
        tgNotSelected.setOnClickListener(pillClick);

        // notify button enters notify mode
        btnNotifyTop.setOnClickListener(x -> { notifyMode = true; updatePanel(); });

        // other top actions leave notify mode
        View.OnClickListener exitNotify = y -> {
            notifyMode = false;
            updatePanel();
            Toast.makeText(requireContext(),
                    ((Button) y).getText() + " (not wired yet)", Toast.LENGTH_SHORT).show();
        };
        btnWaitingTop.setOnClickListener(exitNotify);
        btnSelectTop.setOnClickListener(exitNotify);
        btnQR.setOnClickListener(exitNotify);
        btnEditTop.setOnClickListener(exitNotify);

        // message counter
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                int len = s == null ? 0 : s.length();
                tvCounter.setText(len + "/240");
            }
            @Override public void afterTextChanged(Editable s) {}
        });


        // SEND -> Firestore write
        btnSend.setOnClickListener(z -> {
            if (audience != Audience.SELECTED) {
                Toast.makeText(requireContext(),
                        "This message form is only for Selected entrants (others coming soon).",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_LONG).show();
                return;
            }
            String msg = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();
            if (msg.isEmpty()) { etMessage.setError("Message required"); return; }

            String uid = "unknown"; // no auth yet

            Map<String, Object> payload = new HashMap<>();
            payload.put("audience", "selected");
            payload.put("message", msg);
            payload.put("includePoster", cbAddPoster.isChecked());
            payload.put("linkUrl", null);
            payload.put("createdAt", FieldValue.serverTimestamp());
            payload.put("createdByUid", uid);
            payload.put("eventId", eventId);

            z.setEnabled(false);

            db.collection("org_events")
                    .document(eventId)
                    .collection("broadcasts")
                    .add(payload)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(requireContext(), "Notification sent", Toast.LENGTH_SHORT).show();


                        etMessage.setText("");
                        notifyMode = false;
                        updatePanel();
                        z.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to queue: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        z.setEnabled(true);
                    });
        });


    }

    private void updatePanel() {
        boolean showForm = notifyMode && audience == Audience.SELECTED;
        cardNotify.setVisibility(showForm ? View.VISIBLE : View.GONE);

        stylePill(tgWaiting, tgWaiting.isChecked());
        stylePill(tgSelected, tgSelected.isChecked());
        stylePill(tgNotSelected, tgNotSelected.isChecked());

        int tint = notifyMode ? 0xFF17C172 : 0xFFBDBDBD;
        androidx.core.view.ViewCompat.setBackgroundTintList(
                btnNotifyTop, android.content.res.ColorStateList.valueOf(tint));
    }

    private void stylePill(ToggleButton pill, boolean selected) {
        pill.setBackgroundResource(selected ? R.drawable.bg_pill_deepblue : R.drawable.bg_pill_grey);
        pill.setTextColor(selected ? 0xFFFFFFFF : 0xFF333333);
    }

    private void fanOutToInbox(String eventId, String message, boolean includePoster, @Nullable String linkUrl) {
        // 1) get recipients from entrants subcollection
        db.collection("org_events").document(eventId)
                .collection("entrants")
                .whereEqualTo("status", "selected")
                .get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    if (snap == null || snap.isEmpty()) {
                        Toast.makeText(requireContext(), "No selected entrants found (demo).", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2) write inbox items in a single batch
                    WriteBatch batch = db.batch();
                    int count = 0;

                    for (QueryDocumentSnapshot d : snap) {
                        String uid = d.getId(); // entrant UID (doc id)
                        DocumentReference inboxRef = db.collection("users").document(uid)
                                .collection("inbox").document();

                        Map<String, Object> inboxItem = new HashMap<>();
                        inboxItem.put("type", "event_broadcast");
                        inboxItem.put("eventId", eventId);
                        inboxItem.put("audience", "selected");
                        inboxItem.put("title", "Event update");
                        inboxItem.put("message", message);
                        inboxItem.put("includePoster", includePoster);
                        inboxItem.put("linkUrl", linkUrl);
                        inboxItem.put("createdAt", FieldValue.serverTimestamp());
                        inboxItem.put("read", false);

                        batch.set(inboxRef, inboxItem);
                        count++;
                    }

                    final int finalCount = count;
                    batch.commit()
                            .addOnSuccessListener(v -> Toast.makeText(requireContext(),
                                    "Delivered to " + finalCount + " inbox(es) (demo).", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(requireContext(),
                                    "Inbox fan-out failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Load entrants failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

}
