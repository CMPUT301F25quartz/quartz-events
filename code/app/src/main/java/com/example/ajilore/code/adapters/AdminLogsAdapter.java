package com.example.ajilore.code.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ajilore.code.R;
import com.example.ajilore.code.models.NotificationLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminLogsAdapter extends RecyclerView.Adapter<AdminLogsAdapter.LogViewHolder> {

    private Context context;
    private List<NotificationLog> logs;
    private List<NotificationLog> logsFull;

    public AdminLogsAdapter(Context context) {
        this.context = context;
        this.logs = new ArrayList<>();
        this.logsFull = new ArrayList<>();
    }

    public void setLogs(List<NotificationLog> logs) {
        this.logs = new ArrayList<>(logs);
        this.logsFull = new ArrayList<>(logs);
        notifyDataSetChanged();
    }

    // Filter by Event ID or Message content
    public void filter(String text) {
        logs.clear();
        if (text.isEmpty()) {
            logs.addAll(logsFull);
        } else {
            text = text.toLowerCase();
            for (NotificationLog item : logsFull) {
                if ((item.getMessage() != null && item.getMessage().toLowerCase().contains(text)) ||
                        (item.getEventId() != null && item.getEventId().toLowerCase().contains(text))) {
                    logs.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        NotificationLog log = logs.get(position);

        holder.tvMessage.setText(log.getMessage());
        holder.tvEventInfo.setText("Event ID: " + log.getEventId());

        // Show audience type (e.g. "Waiting List", "Selected")
        if (log.getAudience() != null) {
            holder.tvAudience.setText(log.getAudience().toUpperCase());
        }
        if (log.getSenderId() != null && !log.getSenderId().isEmpty()) {
            holder.tvSender.setText("Sent by: " + log.getSenderId());
            holder.tvSender.setVisibility(View.VISIBLE);
        } else {
            holder.tvSender.setVisibility(View.GONE);
        }

        if (log.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault());
            holder.tvDate.setText(sdf.format(log.getTimestamp().toDate()));
        }
    }

    @Override
    public int getItemCount() { return logs.size(); }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAudience, tvEventInfo, tvMessage, tvSender;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_log_date);
            tvAudience = itemView.findViewById(R.id.tv_log_audience);
            tvEventInfo = itemView.findViewById(R.id.tv_log_event);
            tvMessage = itemView.findViewById(R.id.tv_log_message);
            tvSender = itemView.findViewById(R.id.tv_log_sender);
        }
    }
}