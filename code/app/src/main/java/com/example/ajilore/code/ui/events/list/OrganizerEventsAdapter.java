package com.example.ajilore.code.ui.events.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.OrganizerEventsFragment;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * RecyclerView.Adapter that binds {@link EventItem} objects for the organizer's event list.
 * Handles image loading for event poster, subtitle visibility, and click actions for both row and edit icon.
 */
public class OrganizerEventsAdapter extends RecyclerView.Adapter<OrganizerEventsAdapter.EventVH> {
    /** Click handler for an event row. */
    public interface OnEventClick {
        /**
         * Called when a row or edit icon is clicked.
         * @param item The {@link EventItem} instance that was clicked.
         */
        void onClick(EventItem item); }
    private final List<EventItem> items;
    private final OnEventClick click;

    /**
     * Constructs an OrganizerEventsAdapter.
     *
     * @param items List of {@link EventItem} data to display.
     * @param click Callback invoked for row or edit icon clicks.
     */
    public OrganizerEventsAdapter(List<EventItem> items, OnEventClick click) {
        this.items = items; this.click = click;
    }

    /**
     * Inflates view for one organizer event row.
     *
     * @param p Parent ViewGroup for context.
     * @param v View type (unused as single layout).
     * @return ViewHolder for event row.
     */
    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_organizer_event, p, false);
        return new EventVH(view);
    }

    /**
     * Binds an {@link EventItem} to the ViewHolder and updates all child views.
     *
     * @param h    The ViewHolder instance.
     * @param pos  Index of the event in the adapter.
     */
    @Override public void onBindViewHolder(@NonNull EventVH h, int pos) { h.bind(items.get(pos), click); }

    /**
     * Returns the number of events currently in the adapter.
     * @return List size.
     */
    @Override public int getItemCount() { return items.size();

    }

    // ---------- view holder ----------
    /**
     * ViewHolder class for binding a single organizer event row.
     * Wires up view references, populates image, title, date, subtitle, and click actions.
     */
    public static class EventVH extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDate, tvSubtitle;
        private final ImageView ivEdit, ivPoster;

        /**
         * Constructs a ViewHolder for organizer event row.
         *
         * @param itemView Root of the item_organizer_event layout.
         */
        public EventVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvDate  = itemView.findViewById(R.id.tvDate);
            ivEdit  = itemView.findViewById(R.id.ivEdit);
            ivPoster= itemView.findViewById(R.id.ivPoster);
        }

        /**
         * Binds one EventItem to the row and wires up clicks.
         * @param e     event data to show
         * @param click callback for row/edit icon clicks
         */
        void bind(EventItem e, OnEventClick click) {
            tvTitle.setText(e.title);
            tvDate.setText(e.dateText);
            //ivPoster.setImageResource(e.posterRes);
            //---THIS SETS THE IMAGE IN THE ORGANIZER LIST-----
            if(e.posterUrl != null && e.posterUrl.startsWith("http")){
                Glide.with(itemView.getContext())
                        .load(e.posterUrl)
                        .placeholder(e.posterRes)
                        .into(ivPoster);
            } else {
                ivPoster.setImageResource(e.posterRes);
            }
            //------------------------

            //show/hide subtitle gracefully
            if (e.subtitle != null && !e.subtitle.isEmpty()) {
                tvSubtitle.setText(e.subtitle);
                tvSubtitle.setVisibility(View.VISIBLE);
            }else{
                tvSubtitle.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> click.onClick(e));
            ivEdit.setOnClickListener(v -> click.onClick(e));
        }
    }
}
