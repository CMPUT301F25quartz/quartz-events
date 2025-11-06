package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.EventNotifier;   // << make sure helper is in this package
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Arrays;

/**
 * ManageEventsFragment
 *
 * Purpose: Lets organizers manage a single event: choose an audience
 * (waiting/chosen/selected/cancelled), type a message, and send it.
 * Uses {@link EventNotifier} to write a broadcast and fan out inbox items.
 *
 * Pattern: Fragment as controller + simple “pills” (ToggleButtons) as a filter,
 * delegating data work to a helper class (EventNotifier).
 *
 */

public class ManageEventsFragment extends Fragment {

    // ---- args ----
    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";

    private ListenerRegistration acceptDeclineReg;


    /**
     * Factory method to create a ManageEventsFragment for a specific event.
     *
     * @param eventId   Firestore document id under /org_events
     * @param eventTitle Title to show in the toolbar
     * @return configured fragment instance with arguments set
     */

    public static ManageEventsFragment newInstance(@NonNull String eventId, @NonNull String eventTitle) {
        ManageEventsFragment f = new ManageEventsFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        b.putString(ARG_EVENT_TITLE, eventTitle);
        f.setArguments(b);
        return f;
    }

    // ---- state ----
    private enum Audience { NONE, WAITING, CHOSEN, SELECTED, CANCELLED }
    private Audience audience = Audience.NONE;
    private boolean notifyMode = false;

    // ---- views ----
    private View cardNotify;
    private ToggleButton tgWaiting, tgChosen, tgSelected, tgCancelled;
    private Button btnNotifyTop, btnSend, btnEditEvent;
    private EditText etMessage;
    private TextView tvCounter, tvTitle;
    private CheckBox cbAddLink, cbAddPoster;

    // ---- data ----
    private String eventId, eventTitle;
    private FirebaseFirestore db;

    /**
     * Inflates the “manage event” layout.
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_events_notify, container, false);
    }

    /**
     * Wires UI elements, restores args, sets up pill behavior, and hooks the Send button
     * to call {@link EventNotifier#notifyAudience(FirebaseFirestore, String, String, String, boolean, String, String, EventNotifier.Callback)}.
     */

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
        btnEditEvent    = v.findViewById(R.id.btnEditEvent);

        // audience pills (ensure your XML has these 4 IDs)
        tgWaiting   = v.findViewById(R.id.tgWaiting);
        tgChosen    = v.findViewById(R.id.tgChosen);
        tgSelected  = v.findViewById(R.id.tgSelected);
        tgCancelled = v.findViewById(R.id.tgCancelled);

        // notify card
        cardNotify  = v.findViewById(R.id.cardNotify);
        etMessage   = v.findViewById(R.id.etMessage);
        tvCounter   = v.findViewById(R.id.tvCounter);
        cbAddLink   = v.findViewById(R.id.cbAddLink);
        cbAddPoster = v.findViewById(R.id.cbAddPoster);
        btnSend     = v.findViewById(R.id.btnSend);

        // start state
        tgWaiting.setChecked(false);
        tgChosen.setChecked(false);
        tgSelected.setChecked(false);
        tgCancelled.setChecked(false);
        audience = Audience.NONE;
        notifyMode = false;
        updatePanel();

        // pills: make them mutually exclusive
        View.OnClickListener pillClick = pill -> {
            tgWaiting.setChecked(pill.getId() == R.id.tgWaiting);
            tgChosen.setChecked(pill.getId() == R.id.tgChosen);
            tgSelected.setChecked(pill.getId() == R.id.tgSelected);
            tgCancelled.setChecked(pill.getId() == R.id.tgCancelled);

            if (tgWaiting.isChecked())        audience = Audience.WAITING;
            else if (tgChosen.isChecked())    audience = Audience.CHOSEN;
            else if (tgSelected.isChecked())  audience = Audience.SELECTED;
            else if (tgCancelled.isChecked()) audience = Audience.CANCELLED;
            else                              audience = Audience.NONE;

            updatePanel();
        };
        tgWaiting.setOnClickListener(pillClick);
        tgChosen.setOnClickListener(pillClick);
        tgSelected.setOnClickListener(pillClick);
        tgCancelled.setOnClickListener(pillClick);


        // Select Entrants -> navigate to selection screen
        btnSelectTop.setOnClickListener(view -> {
            Fragment f = SelectEntrantsFragment.newInstance(eventId, eventTitle);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, f)
                    .addToBackStack(null)
                    .commit();
        });

        // Notify
        btnNotifyTop.setOnClickListener(x -> { notifyMode = true; updatePanel(); });

        // other top actions (leave notify mode for now)
        View.OnClickListener exitNotify = y -> {
            notifyMode = false;
            updatePanel();
            Toast.makeText(requireContext(),
                    ((Button) y).getText() + " (not wired yet)", Toast.LENGTH_SHORT).show();
        };
        //Linking the waiting list fragment to the waiting list button
        //btnWaitingTop.setOnClickListener(exitNotify);
        btnWaitingTop.setOnClickListener(view -> {
            Fragment f = WaitingListFragment.newInstance(eventId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, f)
                    .addToBackStack(null)
                    .commit();
        });
        //btnQR.setOnClickListener(exitNotify);
        btnEditEvent.setOnClickListener(view -> {
            Fragment editFragment = CreateEventFragment.newInstance(eventId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, editFragment)
                    .addToBackStack(null)
                    .commit();
        });

        //QR Page/Generator
        btnQR.setOnClickListener(x -> {
            Fragment f = ManageEventQRFragment.newInstance(eventId, eventTitle);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, f)
                    .addToBackStack(null)
                    .commit();
        });


        // message counter
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                tvCounter.setText(((s == null) ? 0 : s.length()) + "/240");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // SEND -> delegate to EventNotifier
        btnSend.setOnClickListener(z -> {
            if (!notifyMode || audience == Audience.NONE) {
                Toast.makeText(requireContext(), "Pick an audience and tap Notify first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_LONG).show();
                return;
            }

            String msg = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();
            if (msg.isEmpty()) { etMessage.setError("Message required"); return; }

            String targetStatus = statusFor(audience);
            if (targetStatus == null) {
                Toast.makeText(requireContext(), "Unknown audience.", Toast.LENGTH_SHORT).show();
                return;
            }

            z.setEnabled(false);

            // Send EXACTLY what you typed to everyone matching that status
            com.example.ajilore.code.ui.events.EventNotifier.notifyAudience(
                    db,
                    eventId,
                    eventTitle != null ? eventTitle : "",
                    msg,
                    cbAddPoster.isChecked(),
                    /*linkUrl*/ null,
                    targetStatus,
                    new com.example.ajilore.code.ui.events.EventNotifier.Callback() {
                        @Override public void onSuccess(int delivered, @NonNull String broadcastId) {
                            Toast.makeText(requireContext(),
                                    "Sent to " + delivered + " " + targetStatus + " entrant(s).",
                                    Toast.LENGTH_SHORT).show();
                            etMessage.setText("");
                            notifyMode = false;
                            updatePanel();
                            z.setEnabled(true);
                        }
                        @Override public void onError(@NonNull Exception e) {
                            Toast.makeText(requireContext(), "Send failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            z.setEnabled(true);
                        }
                    }
            );
        });

    }


    /**
     * Shows/hides the message card based on current mode and pill.
     * Also sets a helpful hint for the message box and tints the Notify button.
     */

    private void updatePanel() {
        boolean showForm = notifyMode && (
                audience == Audience.WAITING ||
                        audience == Audience.CHOSEN  ||
                        audience == Audience.SELECTED||
                        audience == Audience.CANCELLED
        );
        cardNotify.setVisibility(showForm ? View.VISIBLE : View.GONE);

        stylePill(tgWaiting,   tgWaiting.isChecked());
        stylePill(tgChosen,    tgChosen.isChecked());
        stylePill(tgSelected,  tgSelected.isChecked());
        stylePill(tgCancelled, tgCancelled.isChecked());

        if (showForm) {
            switch (audience) {
                case CHOSEN:
                    etMessage.setHint("You've been chosen! Please accept by Friday @ 5pm.");
                    break;
                case SELECTED:
                    etMessage.setHint("Welcome aboard! Details for accepted entrants…");
                    break;
                case WAITING:
                    etMessage.setHint("You're still on the waitlist. Quick update…");
                    break;
                case CANCELLED:
                    etMessage.setHint("Your spot was cancelled. Here’s what happens next…");
                    break;
                default: etMessage.setHint("");
            }
        }

        int tint = notifyMode ? 0xFF17C172 : 0xFFBDBDBD;
        ViewCompat.setBackgroundTintList(btnNotifyTop, android.content.res.ColorStateList.valueOf(tint));
    }



    /**
     * Helper function to style a pill button as selected or not.
     *
     * @param pill     the ToggleButton to style
     * @param selected whether it is active
     */

    private void stylePill(ToggleButton pill, boolean selected) {
        pill.setBackgroundResource(selected ? R.drawable.bg_pill_deepblue : R.drawable.bg_pill_grey);
        pill.setTextColor(selected ? 0xFFFFFFFF : 0xFF333333);
    }


    /**
     * Maps the current Audience enum to the status string used in Firestore.
     *
     * @param a enum value from the selected pill
     * @return one of "chosen", "selected", "waiting", "cancelled", or null if NONE
     */
    private @Nullable String statusFor(Audience a) {
        switch (a) {
            case CHOSEN:    return "chosen";
            case SELECTED:  return "selected";
            case WAITING:   return "waiting";
            case CANCELLED: return "cancelled";
            default:        return null;
        }
    }



}

