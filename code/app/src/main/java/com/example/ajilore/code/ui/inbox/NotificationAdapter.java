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




public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {




    private Context context;
    private List<NotificationModel> notificationList;
    private OnNotificationActionListener listener;
    private boolean isArchivedView;




    public interface OnNotificationActionListener {
        void onDismiss(NotificationModel item);
        void onAction(NotificationModel item);
    }




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




        // Hide dismiss button in archived view
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




    public void updateList(List<NotificationModel> newList, boolean isArchivedView) {
        this.notificationList = newList;
        this.isArchivedView = isArchivedView;
        notifyDataSetChanged();
    }
}
