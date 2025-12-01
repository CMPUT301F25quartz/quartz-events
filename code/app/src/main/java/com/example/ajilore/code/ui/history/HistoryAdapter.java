package com.example.ajilore.code.ui.history;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ajilore.code.R;

import com.google.firebase.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code HistoryAdapter} binds a user's event registration history to a
 * {@link RecyclerView}, displaying each registered event with its title,
 * date, location, and poster image.
 *
 * <h3>Displayed Fields</h3>
 * <ul>
 *     <li><b>eventTitle</b> – title of the event</li>
 *     <li><b>location</b> – event location string</li>
 *     <li><b>startsAt</b> – event timestamp (formatted for display)</li>
 *     <li><b>posterUrl</b> – image URL for the event poster</li>
 * </ul>
 *
 * <p>The adapter accepts a list of Firestore document maps, each representing
 * a single registration entry from {@code users/{deviceId}/registrations}.</p>
 *
 * <h3>Click Handling</h3>
 * A callback interface {@link OnHistoryClickListener} is provided to notify
 * the parent fragment when an item is selected. This allows navigation to
 * {@link com.example.ajilore.code.ui.events.EventDetailsFragment}.
 *
 * @author
 *     Temi Akindele
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(Map<String, Object> item);
    }

    private final List<Map<String, Object>> items;
    private final OnHistoryClickListener listener;

    /**
     * Creates a new adapter to display history items.
     *
     * @param items    A list of maps representing event registration documents.
     * @param listener Callback invoked when a history entry is clicked.
     */
    public HistoryAdapter(List<Map<String, Object>> items, OnHistoryClickListener listener) {

        this.items = items;
        this.listener = listener;
    }

    /**
     * Inflates the history item layout and creates a new {@link ViewHolder}.
     *
     * @param parent The parent RecyclerView
     * @param viewType The type of view (unused—only one type exists)
     * @return The created ViewHolder instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds event registration data to an item in the RecyclerView.
     *
     * <p>This includes:</p>
     * <ul>
     *     <li>Formatting and displaying the event date</li>
     *     <li>Loading the event poster image using Glide</li>
     *     <li>Setting title and location text</li>
     *     <li>Adding a click listener to notify the parent fragment</li>
     * </ul>
     *
     * @param holder The ViewHolder to bind data into
     * @param position The item index within the dataset
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> data = items.get(position);
        String title = (String) data.get("eventTitle");
        String location = (String) data.get("location");
        String posterUrl = (String) data.get("posterUrl");
        Timestamp ts = (Timestamp) data.get("startsAt");

        holder.tvTitle.setText(title != null ? title : "Untitled Event");
        holder.tvLocation.setText(location != null ? location : "Location unavailable");


        if (ts != null) {
            Date d = ts.toDate();
            SimpleDateFormat fmt = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.ENGLISH);
            holder.tvDate.setText(fmt.format(d));
        } else {
            holder.tvDate.setText("Date TBD");
        }

        // Load poster image
        if (posterUrl != null && !posterUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(posterUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.imgPoster);
        } else {
            holder.imgPoster.setImageResource(R.drawable.image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryClick(data);
            }
        });

    }

    /**
     * Returns the number of history entries available.
     *
     * @return The total count of displayed items
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class that stores direct references to each UI element
     * within a history list item.
     *
     * <p>Improves performance by avoiding repeated calls to
     * {@code findViewById()} during scrolling.</p>
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvDate, tvLocation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster   = itemView.findViewById(R.id.imgPoster);
            tvTitle     = itemView.findViewById(R.id.tvEventTitle);
            tvDate      = itemView.findViewById(R.id.tvEventDate);
            tvLocation  = itemView.findViewById(R.id.tvEventLocation);
        }
    }
}
