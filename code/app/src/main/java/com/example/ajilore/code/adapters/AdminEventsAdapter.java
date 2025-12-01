package com.example.ajilore.code.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying events in admin browse view.
 *
 * <p>This RecyclerView adapter manages the display of event data in the administrator's
 * event browsing interface. It presents event information in a scrollable list and provides
 * callbacks for administrative actions like viewing event details or removing events.</p>
 *
 * <p>Design Pattern: Implements the Adapter pattern with ViewHolder pattern for
 * efficient list rendering.</p>
 *
 * <p>User Story: US 03.04.01 - As an administrator, I want to be able to browse events.</p>
 *
 * <p>Outstanding Issues:</p>
 * <ul>
 *   <li>Need to add status indicators (Open/Closed/Full) for events</li>
 *   <li>Consider adding sorting options (by date, name, number of entrants)</li>
 * </ul>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminEventsAdapter extends RecyclerView.Adapter<AdminEventsAdapter.EventViewHolder> {

    private final Context context;
    private List<Event> eventList;
    private final List<Event> eventListFull;
    private final OnEventActionListener listener;

    /**
     * Interface for admin interaction callbacks on events.
     */
    public interface OnEventActionListener {
        /**
         * Called when delete button is pressed for an event.
         * @param event Event to delete.
         */
        void onDeleteClick(Event event);

        /**
         * Called when event row is clicked for details.
         * @param event Event to view.
         */
        void onEventClick(Event event);
    }

    /**
     * Constructs a new AdminEventsAdapter.
     * @param context Context used for layout inflation and Glide.
     * @param listener Callback interface for admin event actions.
     */
    public AdminEventsAdapter(Context context, OnEventActionListener listener) {
        this.context = context;
        this.eventList = new ArrayList<>();
        this.eventListFull = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Replaces the adapter's event list with new events and refreshes UI.
     * @param events List of events to display.
     */
    public void setEvents(List<Event> events) {
        this.eventList = new ArrayList<>(events);
        this.eventListFull.clear();
        this.eventListFull.addAll(events);
        notifyDataSetChanged();
    }

    /**
     * Filters the event list by matching query against title, description, or location.
     * @param query Query string, case-insensitive.
     */
    public void filter(String query) {
        eventList.clear();
        if (query.isEmpty()) {
            eventList.addAll(eventListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Event event : eventListFull) {
                if (event.title.toLowerCase().contains(lowerCaseQuery) ||
                        (event.location != null &&
                                event.location.toLowerCase().contains(lowerCaseQuery))) {
                    eventList.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Inflates the event item view and creates a ViewHolder.
     * @param parent The parent ViewGroup.
     * @param viewType View type (not used).
     * @return A new EventViewHolder.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data and click listeners to a ViewHolder.
     * @param holder The ViewHolder.
     * @param position Position of the event in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Display formatted date and time
        String formattedDate = formatEventDate(event.startsAt);
        holder.tvEventDate.setText(formattedDate);
        holder.tvEventTitle.setText(event.title);

        // Show location if available
        if (event.location != null && !event.location.isEmpty()) {
            holder.layoutLocation.setVisibility(View.VISIBLE);
            holder.tvEventLocation.setText(event.location);
        } else {
            holder.layoutLocation.setVisibility(View.GONE);
        }

        // Load image using Glide - Use posterUrl field
        String posterUrl = event.posterUrl;
        if (posterUrl != null && !posterUrl.isEmpty() && !posterUrl.equals("\"\"")) {
            Glide.with(context)
                    .load(posterUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.ivEventPoster);
        } else {
            holder.ivEventPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if ("flagged".equalsIgnoreCase(event.status)) {
            holder.ivFlagIcon.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(0.9f);
        } else {
            holder.ivFlagIcon.setVisibility(View.GONE);
            holder.itemView.setAlpha(1.0f);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(event);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(event);
        });
    }

    /**
     * Format a Timestamp for display.
     * @param timestamp The Firestore Timestamp to format
     * @return Formatted date string
     */
    private String formatEventDate(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) {
            return "Date TBA";
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "MMM dd, yyyy 'at' h:mm a",
                java.util.Locale.getDefault()
        );
        return sdf.format(timestamp.toDate());
    }

    /**
     * @return Number of events being displayed.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder for an event item row.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventPoster;
        TextView tvEventDate;
        TextView tvEventTitle;
        LinearLayout layoutLocation;
        TextView tvEventLocation;
        ImageButton btnDelete;
        ImageView ivFlagIcon;


        /**
         * Binds view references from inflated layout.
         * @param itemView Root view of this event list item.
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventPoster = itemView.findViewById(R.id.iv_event_poster);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            layoutLocation = itemView.findViewById(R.id.layout_location);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            ivFlagIcon = itemView.findViewById(R.id.iv_flag_icon);
        }
    }
}