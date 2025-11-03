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

public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.VH> {
    private final List<Entrant> items;
    private final Context context;

    public WaitingListAdapter(Context ctx, List<Entrant> items) {
        this.context = ctx;
        this.items = new ArrayList<>(items);
    }

    public void updateList(List<Entrant> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waiting_entrant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        holder.bind(items.get(pos), context);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ImageView ivAvatar;
        private final TextView tvName, tvStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

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
