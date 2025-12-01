package com.example.ajilore.code.ui.events;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.ajilore.code.R;

/**
 * Dialog for accepting or declining event invitations
 */
public class EventActionDialogFragment extends DialogFragment {

    private static final String ARG_ACTION_TYPE = "action_type";
    private static final String ARG_EVENT_TITLE = "event_title";

    public static final String ACTION_ACCEPT = "accept";
    public static final String ACTION_DECLINE = "decline";

    private String actionType;
    private String eventTitle;
    private OnActionListener actionListener;

    public interface OnActionListener {
        void onConfirm(String actionType);
        void onCancel();
    }

    public static EventActionDialogFragment newInstance(String actionType, String eventTitle) {
        EventActionDialogFragment fragment = new EventActionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACTION_TYPE, actionType);
        args.putString(ARG_EVENT_TITLE, eventTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            actionType = getArguments().getString(ARG_ACTION_TYPE);
            eventTitle = getArguments().getString(ARG_EVENT_TITLE);
        }
        // Set dialog style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundedDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_event_action, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView icon = view.findViewById(R.id.dialogIcon);
        TextView message = view.findViewById(R.id.dialogMessage);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        // Configure dialog based on action type
        if (ACTION_ACCEPT.equals(actionType)) {
            icon.setImageResource(R.drawable.ic_check_circle);
            message.setText("You have accepted to attend this event");
            btnConfirm.setText("See Events");
        } else if (ACTION_DECLINE.equals(actionType)) {
            icon.setImageResource(R.drawable.ic_cancel);
            message.setText("You have declined this invite and will be removed from the waitlist");
            btnConfirm.setText("See Events");
        }

        // Set up listeners
        btnCancel.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCancel();
            }
            dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onConfirm(actionType);
            }
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Make dialog width match parent with padding
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    public void setActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }
}