package com.example.ajilore.code.ui.inbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final List<NotificationModel> notificationList;
    private final OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onDismissClicked(NotificationModel notification);
        void onDetailsClicked(NotificationModel notification);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notificationList,
                               OnNotificationActionListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
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

        holder.textMessage.setText(notification.getMessage());
        holder.textTime.setText(notification.getTime());
        holder.btnAction.setText(
                notification.getActionText() != null ?
                        notification.getActionText() :
                        context.getString(R.string.notif_details)
        );

        if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(notification.getImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imageProfile);
        } else {
            holder.imageProfile.setImageResource(R.drawable.ic_profile);
        }

        holder.btnDismiss.setOnClickListener(v -> {
            if (listener != null) listener.onDismissClicked(notification);
        });

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onDetailsClicked(notification);
        });

        holder.itemView.setAlpha(notification.isRead() ? 0.5f : 1f);
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public OnNotificationActionListener getListener() { return listener; }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textMessage, textTime;
        MaterialButton btnDismiss, btnAction;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
            btnDismiss = itemView.findViewById(R.id.btnDismiss);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
