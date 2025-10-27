package com.example.ajilore.code.ui.events;

import android.content.res.ColorStateList;
import android.graphics.Color;
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

    private String eventId, eventTitle;

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
        cardNotify = v.findViewById(R.id.cardNotify); // in XML this container starts as GONE
        etMessage  = v.findViewById(R.id.etMessage);
        tvCounter  = v.findViewById(R.id.tvCounter);
        CheckBox cbLink   = v.findViewById(R.id.cbAddLink);
        CheckBox cbPoster = v.findViewById(R.id.cbAddPoster);
        Button btnSend    = v.findViewById(R.id.btnSend);

        // start state: nothing selected, not in notify mode
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

        // notify button enters notify mode (show form only if a pill is picked)
        btnNotifyTop.setOnClickListener(x -> {
            notifyMode = true;
            updatePanel();
        });

        // other top actions leave notify mode (hide form)
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

        // counter for message
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                int len = s == null ? 0 : s.length();
                tvCounter.setText(len + "/240");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // send (stub)
        btnSend.setOnClickListener(z -> {
            if (audience != Audience.SELECTED) {
                Toast.makeText(requireContext(),
                        "This message form is only for Selected entrants (others coming soon).",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String msg = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();
            if (msg.isEmpty()) { etMessage.setError("Message required"); return; }

            // TODO: Firestore write for SELECTED audience
            Toast.makeText(requireContext(),
                    "Queued to selected entrants for " + eventTitle, Toast.LENGTH_SHORT).show();
        });

    }

    private void updatePanel() {
        // was: boolean showForm = notifyMode && audience != Audience.NONE;
        boolean showForm = notifyMode && audience == Audience.SELECTED;
        cardNotify.setVisibility(showForm ? View.VISIBLE : View.GONE);

        stylePill(tgWaiting, tgWaiting.isChecked());
        stylePill(tgSelected, tgSelected.isChecked());
        stylePill(tgNotSelected, tgNotSelected.isChecked());

        int tint = notifyMode ? android.graphics.Color.parseColor("#17C172")
                : android.graphics.Color.parseColor("#BDBDBD");
        androidx.core.view.ViewCompat.setBackgroundTintList(
                btnNotifyTop, android.content.res.ColorStateList.valueOf(tint));
    }

    private void stylePill(ToggleButton pill, boolean selected) {
        pill.setBackgroundResource(selected ? R.drawable.bg_pill_deepblue : R.drawable.bg_pill_grey);
        pill.setTextColor(selected ? 0xFFFFFFFF : 0xFF333333);
    }
}
