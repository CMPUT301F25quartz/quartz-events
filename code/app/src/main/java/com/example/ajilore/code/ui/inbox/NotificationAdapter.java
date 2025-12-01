package com.example.ajilore.code.ui.inbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * RecyclerView adapter for displaying inbox notifications for an entrant.
 *
 * <p>This adapter supports two modes:</p>
 * <ul>
 *     <li><b>Inbox View</b> – notifications can be dismissed or opened.</li>
 *     <li><b>Archived View</b> – dismiss button is hidden, notifications are read-only.</li>
 * </ul>
 *
 * <p>Each row displays:</p>
 * <ul>
 *     <li>Sender profile image (loaded via Glide)</li>
 *     <li>Message body text</li>
 *     <li>Formatted timestamp</li>
 *     <li>Action button (e.g., “See Details”)</li>
 *     <li>Optional Dismiss button</li>
 * </ul>
 *
 * <p>Used by {@link com.example.ajilore.code.ui.inbox.InboxFragment} to render real-time Firestore notifications.</p>
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotificationModel> notificationList;
    private OnNotificationActionListener listener;
    private boolean isArchivedView;

    /**
     * Listener used by {@link InboxFragment} to respond to notification actions.
     *
     * <p>Includes two possible operations:</p>
     * <ul>
     *     <li><b>onDismiss()</b> — moves the notification into the "archived" bucket.</li>
     *     <li><b>onAction()</b> — triggered when the user presses the action button
     *         (typically navigating to event details).</li>
     * </ul>
     */
    public interface OnNotificationActionListener {
        void onDismiss(NotificationModel item);
        void onAction(NotificationModel item);
    }

    /**
     * Creates a NotificationAdapter for displaying notification items.
     *
     * @param context           Activity or Fragment context.
     * @param notificationList  Initial list of notifications to display.
     * @param listener          Callback listener for dismiss / action events.
     * @param isArchivedView    True if adapter is rendering archived notifications
     *                          (hides dismiss buttons).
     */
    public NotificationAdapter(Context context, List<NotificationModel> notificationList,
                               OnNotificationActionListener listener, boolean isArchivedView) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
        this.isArchivedView = isArchivedView;
    }

    /**
     * Inflates the layout for a single notification row.
     *
     * @param parent   Parent ViewGroup.
     * @param viewType Not used; only a single row type exists.
     * @return A fully constructed {@link NotificationViewHolder}.
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Binds notification data into the row UI:
     * <ul>
     *     <li>Loads profile image using Glide</li>
     *     <li>Displays message, timestamp, and action label</li>
     *     <li>Shows or hides the dismiss button depending on view mode</li>
     * </ul>
     *
     * @param holder   ViewHolder containing row views.
     * @param position Position of the notification in the adapter.
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);

        // 🔹 load profile image from imageUrl
        String imageUrl = notification.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.imageProfile);
        } else {
            holder.imageProfile.setImageResource(R.drawable.ic_profile);
        }

        // 🔹 text fields
        holder.textMessage.setText(notification.getMessage());
        holder.textTime.setText(
                notification.getTime() != null && !notification.getTime().isEmpty()
                        ? notification.getTime()
                        : "Just now"
        );
        holder.btnAction.setText(
                notification.getActionText() != null ? notification.getActionText() : "View"
        );

        // 🔹 hide dismiss button in archived view
        holder.btnDismiss.setVisibility(isArchivedView ? View.GONE : View.VISIBLE);

        holder.btnDismiss.setOnClickListener(v -> {
            if (listener != null) listener.onDismiss(notification);
        });

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onAction(notification);
        });
    }


    /**
     * @return Total number of notifications currently displayed.
     */
    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    /**
     * ViewHolder class representing a single notification item.
     *
     * <p>Contains:</p>
     * <ul>
     *     <li>Profile image</li>
     *     <li>Message text</li>
     *     <li>Timestamp</li>
     *     <li>Dismiss and action buttons</li>
     * </ul>
     *
     * <p>UI elements are bound once here and reused for performance.</p>
     */
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textMessage, textTime;
        LinearLayout buttonContainer;
        MaterialButton btnDismiss, btnAction;

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
     * Updates the adapter with a new list of notifications and toggles
     * between inbox view and archived view.
     *
     * @param newList        Updated list of notifications.
     * @param isArchivedView Whether the adapter should display archived messages
     *                       (dismiss button hidden).
     */
    public void updateList(List<NotificationModel> newList, boolean isArchivedView) {
        this.notificationList = newList;
        this.isArchivedView = isArchivedView;
        notifyDataSetChanged();
    }
}
