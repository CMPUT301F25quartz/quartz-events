package com.example.ajilore.code.ui.inbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Adapter class for displaying notifications in a RecyclerView.
 * Handles showing notification messages, time, profile image, and action buttons.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotificationModel> notificationList;
    private OnNotificationActionListener listener;
    private boolean isArchivedView;

    /**
     * Listener interface for handling notification actions.
     */
    public interface OnNotificationActionListener {
        /**
         * Called when the user dismisses a notification.
         *
         * @param item The notification that was dismissed.
         */
        void onDismiss(NotificationModel item);

        /**
         * Called when the user clicks the notification action button.
         *
         * @param item The notification that the action is performed on.
         */
        void onAction(NotificationModel item);
    }

    /**
     * Constructor for NotificationAdapter.
     *
     * @param context          The context.
     * @param notificationList List of notifications to display.
     * @param listener         Listener for notification actions.
     * @param isArchivedView   Flag indicating if the view is showing archived notifications.
     */
    public NotificationAdapter(Context context, List<NotificationModel> notificationList,
                               OnNotificationActionListener listener, boolean isArchivedView) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
        this.isArchivedView = isArchivedView;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);

        holder.imageProfile.setImageResource(R.drawable.ic_profile);
        holder.textMessage.setText(notification.getMessage());
        holder.textTime.setText(
                notification.getTime() != null && !notification.getTime().isEmpty() ? notification.getTime() : "Just now"
        );
        holder.btnAction.setText(notification.getActionText() != null ? notification.getActionText() : "View");

        // Hide dismiss button if this is an archived view
        holder.btnDismiss.setVisibility(isArchivedView ? View.GONE : View.VISIBLE);

        holder.btnDismiss.setOnClickListener(v -> {
            if (listener != null) listener.onDismiss(notification);
        });

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onAction(notification);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    /**
     * ViewHolder class for individual notification items.
     */
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textMessage, textTime;
        LinearLayout buttonContainer;
        MaterialButton btnDismiss, btnAction;

        /**
         * Constructor for NotificationViewHolder.
         *
         * @param itemView The root view of the notification item.
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
            buttonContainer = itemView.findViewById(R.id.buttonContainer);
            btnDismiss = itemView.findViewById(R.id.btnDismiss);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }

    /**
     * Updates the list of notifications and refreshes the view.
     *
     * @param newList        The new list of notifications.
     * @param isArchivedView Flag indicating if the view is showing archived notifications.
     */
    public void updateList(List<NotificationModel> newList, boolean isArchivedView) {
        this.notificationList = newList;
        this.isArchivedView = isArchivedView;
        notifyDataSetChanged();
    }
}
