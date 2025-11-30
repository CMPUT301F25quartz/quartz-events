package com.example.ajilore.code.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ajilore.code.R;
import com.example.ajilore.code.models.Entrant;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {
    private List<Entrant> entrants;
    private OnEntrantClickListener listener;
    private String viewType; // "invited", "cancelled", "enrolled"

    public interface OnEntrantClickListener {
        void onEntrantClick(Entrant entrant);
    }

    public EntrantAdapter(String viewType) {
        this.viewType = viewType;
    }

    public void setEntrants(List<Entrant> entrants) {
        this.entrants = entrants;
        notifyDataSetChanged();
    }

    public void setOnEntrantClickListener(OnEntrantClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        if (entrants != null && position < entrants.size()) {
            Entrant entrant = entrants.get(position);
            holder.bind(entrant, viewType);
        }
    }

    @Override
    public int getItemCount() {
        return entrants != null ? entrants.size() : 0;
    }

    class EntrantViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText, emailText, statusText, dateText, reasonText;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_entrant_name);
            emailText = itemView.findViewById(R.id.text_entrant_email);
            statusText = itemView.findViewById(R.id.text_entrant_status);
            dateText = itemView.findViewById(R.id.text_entrant_date);
            reasonText = itemView.findViewById(R.id.text_entrant_reason);
        }

        public void bind(Entrant entrant, String viewType) {
            nameText.setText(entrant.getName());
            emailText.setText(entrant.getEmail());

            // Set status text and color based on status
            setStatusTextAndColor(entrant.getStatus());

            // Format and display date based on view type
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            switch (viewType) {
                case "invited":
                    if (entrant.getInvitedDate() != null) {
                        dateText.setText("Invited: " + sdf.format(entrant.getInvitedDate().toDate()));
                    } else {
                        dateText.setText("Date: N/A");
                    }
                    reasonText.setVisibility(View.GONE);
                    break;
                case "cancelled":
                    if (entrant.getCancelledDate() != null) {
                        dateText.setText("Cancelled: " + sdf.format(entrant.getCancelledDate().toDate()));
                    } else {
                        dateText.setText("Date: N/A");
                    }
                    if (entrant.getReason() != null && !entrant.getReason().isEmpty()) {
                        reasonText.setVisibility(View.VISIBLE);
                        reasonText.setText("Reason: " + entrant.getReason());
                    } else {
                        reasonText.setVisibility(View.GONE);
                    }
                    break;
                case "enrolled":
                    if (entrant.getEnrolledDate() != null) {
                        dateText.setText("Enrolled: " + sdf.format(entrant.getEnrolledDate().toDate()));
                    } else {
                        dateText.setText("Date: N/A");
                    }
                    reasonText.setVisibility(View.GONE);
                    break;
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEntrantClick(entrant);
                }
            });
        }

        private void setStatusTextAndColor(String status) {
            statusText.setText(status.toUpperCase());
            int color;
            switch (status.toLowerCase()) {
                case "accepted":
                    color = 0xFF4CAF50; // Green
                    break;
                case "declined":
                    color = 0xFFF44336; // Red
                    break;
                case "pending":
                    color = 0xFFFF9800; // Orange
                    break;
                case "cancelled":
                    color = 0xFF9E9E9E; // Gray
                    break;
                case "enrolled":
                    color = 0xFF2196F3; // Blue
                    break;
                case "invited":
                    color = 0xFF9C27B0; // Purple
                    break;
                default:
                    color = 0xFF757575; // Default gray
                    break;
            }
            statusText.setBackgroundColor(color);
            statusText.setTextColor(0xFFFFFFFF); // White text
        }
    }
}