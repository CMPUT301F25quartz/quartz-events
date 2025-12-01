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

/**
 * RecyclerView adapter responsible for displaying admin notification logs.
 *
 * <p>This adapter supports:</p>
 * <ul>
 *     <li>Displaying timestamp, audience, sender, event info, and message.</li>
 *     <li>Dynamic filtering by search query and audience category
 *         (waiting, selected, chosen, cancelled, all).</li>
 *     <li>Efficient list updates using an internal full copy of logs.</li>
 * </ul>
 *
 * <p>Used in: {@link com.example.ajilore.code.ui.admin.AdminNotificationLogsFragment}</p>
 */
public class AdminLogsAdapter extends RecyclerView.Adapter<AdminLogsAdapter.LogViewHolder> {

    private Context context;
    private List<NotificationLog> logs;
    private List<NotificationLog> logsFull;

    public AdminLogsAdapter(Context context) {
        this.context = context;
        this.logs = new ArrayList<>();
        this.logsFull = new ArrayList<>();
    }

    /**
     * RecyclerView adapter responsible for displaying admin notification logs.
     *
     * <p>This adapter supports:</p>
     * <ul>
     *     <li>Displaying timestamp, audience, sender, event info, and message.</li>
     *     <li>Dynamic filtering by search query and audience category
     *         (waiting, selected, chosen, cancelled, all).</li>
     *     <li>Efficient list updates using an internal full copy of logs.</li>
     * </ul>
     *
     * <p>Used in: {@link com.example.ajilore.code.ui.admin.AdminNotificationLogsFragment}</p>
     */
    public void setLogs(List<NotificationLog> logs) {
        this.logs = new ArrayList<>(logs);
        this.logsFull = new ArrayList<>(logs);
        notifyDataSetChanged();
    }

    // Filter by Event ID or Message content
    /**
     * Filters logs by text query AND audience category.
     *
     * <p>Search is matched against:</p>
     * <ul>
     *     <li>Event ID</li>
     *     <li>Notification message</li>
     * </ul>
     *
     * <p>Audience filter can be one of:</p>
     * <ul>
     *     <li>"all"</li>
     *     <li>"waiting"</li>
     *     <li>"selected"</li>
     *     <li>"chosen"</li>
     *     <li>"cancelled"</li>
     * </ul>
     *
     * @param text            Search text entered by the admin.
     * @param audienceFilter  The selected audience category.
     */
    public void filter(String text, String audienceFilter) {
        logs.clear();

        String lowerCaseText = text.toLowerCase();

        for (NotificationLog item : logsFull) {
            boolean matchesSearch = false;
            boolean matchesAudience = false;

            // 1. Check search text
            if (text.isEmpty()) {
                matchesSearch = true;
            } else {
                if ((item.getMessage() != null && item.getMessage().toLowerCase().contains(lowerCaseText)) ||
                        (item.getEventId() != null && item.getEventId().toLowerCase().contains(lowerCaseText))) {
                    matchesSearch = true;
                }
            }

            // 2. Check audience filter
            if (audienceFilter.equals("all")) {
                matchesAudience = true;
            } else {
                if (item.getAudience() != null && item.getAudience().equalsIgnoreCase(audienceFilter)) {
                    matchesAudience = true;
                }
            }

            // 3. Add if BOTH match
            if (matchesSearch && matchesAudience) {
                logs.add(item);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Inflates the log row layout and creates the ViewHolder.
     *
     * @param parent  The parent view group.
     * @param viewType Ignored (single view type).
     * @return A new {@link LogViewHolder} instance.
     */
    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_log, parent, false);
        return new LogViewHolder(view);
    }

    /**
     * Binds a single notification log item to the ViewHolder.
     *
     * <p>Handles:</p>
     * <ul>
     *     <li>Date formatting</li>
     *     <li>Audience badge styling</li>
     *     <li>Sender label</li>
     *     <li>Event title / ID</li>
     *     <li>Message visibility</li>
     *     <li>Recipient count</li>
     * </ul>
     *
     * @param holder   The ViewHolder with all UI references.
     * @param position The index of the log in the display list.
     */
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        NotificationLog log = logs.get(position);

        //Format and display timestamp
        if (log.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault());
            holder.tvDate.setText(sdf.format(log.getTimestamp().toDate()));
        } else {
            holder.tvDate.setText("Date unknown");
        }

        //Display audience badge with recipient count
        if (log.getAudience() != null) {
            String audienceText = log.getAudience().toUpperCase();
            holder.tvAudience.setText(audienceText);

            // Set background color based on audience type
            setAudienceBadgeStyle(holder.tvAudience, log.getAudience());
        } else {
            holder.tvAudience.setText("GENERAL");
            holder.tvAudience.setBackgroundResource(R.drawable.bg_pill_grey);
        }

        //Display sender
        if (log.getSenderId() != null && !log.getSenderId().isEmpty()) {
            holder.tvSender.setText("Sent by: " + log.getSenderId());
            holder.tvSender.setVisibility(View.VISIBLE);
        } else {
            holder.tvSender.setVisibility(View.GONE);
        }

        //Display event TITLE (with ID as fallback)
        if (log.getEventTitle() != null && !log.getEventTitle().isEmpty()) {
            holder.tvEventInfo.setText(log.getEventTitle());

            // Optionally show event ID in small text below
            if (log.getEventId() != null) {
                holder.tvEventId.setText("ID: " + log.getEventId());
                holder.tvEventId.setVisibility(View.VISIBLE);
            } else {
                holder.tvEventId.setVisibility(View.GONE);
            }
        } else if (log.getEventId() != null) {
            // Fallback: Show event ID if no title
            holder.tvEventInfo.setText("Event ID: " + log.getEventId());
            holder.tvEventId.setVisibility(View.GONE);
        } else {
            holder.tvEventInfo.setText("Unknown Event");
            holder.tvEventId.setVisibility(View.GONE);
        }

        //Display message
        if (log.getMessage() != null && !log.getMessage().isEmpty()) {
            holder.tvMessage.setText(log.getMessage());
            holder.tvMessage.setVisibility(View.VISIBLE);
        } else {
            holder.tvMessage.setVisibility(View.GONE);
        }

        //Display recipient count (if broadcast notification)
        if (log.getRecipientCount() > 0) {
            String countText = "Sent to " + log.getRecipientCount() +
                    (log.getRecipientCount() == 1 ? " entrant" : " entrants");
            holder.tvRecipientCount.setText(countText);
            holder.tvRecipientCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvRecipientCount.setVisibility(View.GONE);
        }
    }

    /**
     * Applies color styling to the audience pill badge based on audience type.
     *
     * <p>Mappings:</p>
     * <ul>
     *     <li>waiting → Grey/Blue badge</li>
     *     <li>selected → Deep blue badge</li>
     *     <li>chosen → Green badge</li>
     *     <li>cancelled → Red badge</li>
     *     <li>default → Grey badge</li>
     * </ul>
     *
     * @param badge     TextView representing the audience pill.
     * @param audience  Audience category string.
     */
    private void setAudienceBadgeStyle(TextView badge, String audience) {
        if (audience == null) {
            badge.setBackgroundResource(R.drawable.bg_pill_grey);
            return;
        }

        switch (audience.toLowerCase()) {
            case "waiting":
                badge.setBackgroundResource(R.drawable.bg_pill_dark); // Grey/Blue
                break;
            case "selected":
                badge.setBackgroundResource(R.drawable.bg_pill_deepblue); // Blue
                break;
            case "chosen":
                badge.setBackgroundResource(R.drawable.bg_pill_green); // Green
                break;
            case "cancelled":
                badge.setBackgroundResource(R.drawable.bg_chip_red); // Red
                break;
            default:
                badge.setBackgroundResource(R.drawable.bg_pill_grey); // Default grey
                break;
        }
    }
    /**
     * @return Number of logs currently being displayed.
     */
    @Override
    public int getItemCount() { return logs.size(); }

    /**
     * ViewHolder containing all UI references for a single log entry row.
     *
     * <p>Binds:</p>
     * <ul>
     *     <li>Date</li>
     *     <li>Audience badge</li>
     *     <li>Event title & ID</li>
     *     <li>Message</li>
     *     <li>Sender name</li>
     *     <li>Recipient count</li>
     * </ul>
     */
    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAudience, tvEventInfo, tvEventId, tvMessage, tvSender, tvRecipientCount;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_log_date);
            tvAudience = itemView.findViewById(R.id.tv_log_audience);
            tvEventInfo = itemView.findViewById(R.id.tv_log_event);
            tvEventId = itemView.findViewById(R.id.tv_log_event_id);
            tvMessage = itemView.findViewById(R.id.tv_log_message);
            tvSender = itemView.findViewById(R.id.tv_log_sender);
            tvRecipientCount = itemView.findViewById(R.id.tv_recipient_count);
        }
    }
}