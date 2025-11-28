package com.example.ajilore.code.ui.events.list; // or ui/events/adapter

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.data.Entrant;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView.Adapter for displaying a waiting list of Entrants in a RecyclerView.
 * Binds entrant data to rows and styles them according to their display status.
 */
public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.VH> {
    private final List<Entrant> items;
    private final Context context;


    /**
     * Constructs a new WaitingListAdapter.
     *
     * @param ctx    Context from host Activity or Fragment (for theming resources).
     * @param items  List of Entrant objects to show in the waiting list.
     */
    public WaitingListAdapter(Context ctx, List<Entrant> items) {
        this.context = ctx;
        this.items = new ArrayList<>(items);
    }

    /**
     * Replaces the current list with a new set of entrants and refreshes the adapter.
     * @param newItems The new list of entrants to display.
     */
    public void updateList(List<Entrant> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Inflates the ViewHolder for a waiting list entrant row.
     * @param parent   Parent ViewGroup for inflation context.
     * @param viewType View type (unused for single row layout).
     * @return A new ViewHolder for a waiting list entrant row.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waiting_entrant, parent, false);
        return new VH(v);
    }

    /**
     * Binds an Entrant object to an existing ViewHolder row, updating name, status, and avatar.
     * @param holder The row's ViewHolder.
     * @param pos    The position of the Entrant in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        holder.bind(items.get(pos), context);
    }

    /**
     * Returns the number of entrants in the waiting list.
     * @return The number of entrants.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for displaying a single Entrant in the waiting list.
     * Handles UI binding and dynamic row styling based on Entrant state.
     */
    static class VH extends RecyclerView.ViewHolder {
        private final ImageView ivAvatar;
        private final TextView tvName, tvStatus;

        /**
         * Binds row views from the item layout.
         * @param itemView Root of the item_waiting_entrant layout.
         */
        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }


        /**
         * Populates row UI with Entrant data, including name, status label, avatar, and color.
         * @param e       The Entrant model for this row.
         * @param context Context for theming (color resources).
         */
        void bind(Entrant e, Context context) {
            tvName.setText(e.nameOrUid);

            switch (e.displayStatus.toLowerCase()) {
                case "accepted":
                    tvStatus.setText("Accepted");
                    tvStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.status_accepted));
                    break;
                case "declined":
                    tvStatus.setText("Declined");
                    tvStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.status_declined));
                    break;
                default:
                    tvStatus.setText("Pending");
                    tvStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.status_pending));
                    break;
            }

            ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }
}
