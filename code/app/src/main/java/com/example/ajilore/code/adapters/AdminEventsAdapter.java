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
import com.example.ajilore.code.models.Event;

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

    public interface OnEventActionListener {
        void onDeleteClick(Event event);
        void onEventClick(Event event);
    }

    public AdminEventsAdapter(Context context, OnEventActionListener listener) {
        this.context = context;
        this.eventList = new ArrayList<>();
        this.eventListFull = new ArrayList<>();
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.eventList = new ArrayList<>(events);
        this.eventListFull.clear();
        this.eventListFull.addAll(events);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        eventList.clear();
        if (query.isEmpty()) {
            eventList.addAll(eventListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Event event : eventListFull) {
                if (event.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        event.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                        (event.getLocation() != null &&
                                event.getLocation().toLowerCase().contains(lowerCaseQuery))) {
                    eventList.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Display formatted date and time
        holder.tvEventDate.setText(event.getFormattedDateTime());
        holder.tvEventTitle.setText(event.getTitle());

        // Show location if available
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            holder.layoutLocation.setVisibility(View.VISIBLE);
            holder.tvEventLocation.setText(event.getLocation());
        } else {
            holder.layoutLocation.setVisibility(View.GONE);
        }

        // Load image using Glide - FIXED: Use getImageURL() not getPosterUrl()
        String imageURL = event.getImageURL();
        if (imageURL != null && !imageURL.isEmpty() && !imageURL.equals("\"\"")) {
            Glide.with(context)
                    .load(imageURL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.ivEventPoster);
        } else {
            holder.ivEventPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(event);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(event);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventPoster;
        TextView tvEventDate;
        TextView tvEventTitle;
        LinearLayout layoutLocation;
        TextView tvEventLocation;
        ImageButton btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventPoster = itemView.findViewById(R.id.iv_event_poster);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            layoutLocation = itemView.findViewById(R.id.layout_location);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}