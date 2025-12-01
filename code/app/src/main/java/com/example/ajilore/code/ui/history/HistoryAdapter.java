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
 * Simple RecyclerView adapter for displaying event history.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(Map<String, Object> item);
    }

    private final List<Map<String, Object>> items;
    private final OnHistoryClickListener listener;

    public HistoryAdapter(List<Map<String, Object>> items, OnHistoryClickListener listener) {

        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return items.size();
    }

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
