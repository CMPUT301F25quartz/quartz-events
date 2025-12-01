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

/**
 * RecyclerView adapter used to display entrant information for different organizer views
 * (Invited, Cancelled, Enrolled).
 *
 * <p>This adapter supports:</p>
 * <ul>
 *     <li>Dynamic row displays based on the list type (invited, cancelled, enrolled).</li>
 *     <li>Showing entrant name, email, status, date, and cancellation reason (when relevant).</li>
 *     <li>Callback handling via {@link OnEntrantClickListener} for row tap actions.</li>
 * </ul>
 *
 * <p>Used by:</p>
 * <ul>
 *     <li>{@link com.example.ajilore.code.ui.events.list.InvitedEntrantsFragment}</li>
 *     <li>{@link com.example.ajilore.code.ui.events.list.CancelledEntrantsFragment}</li>
 *     <li>{@link com.example.ajilore.code.ui.events.list.EnrolledEntrantsFragment}</li>
 * </ul>
 */
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

    /**
     * Updates the adapter with a new list of entrants.
     *
     * @param entrants List of {@link Entrant} objects to display.
     */
    public void setEntrants(List<Entrant> entrants) {
        this.entrants = entrants;
        notifyDataSetChanged();
    }

    /**
     * Registers a listener that triggers whenever an entrant row is clicked.
     *
     * @param listener Callback implementing {@link OnEntrantClickListener}.
     */
    public void setOnEntrantClickListener(OnEntrantClickListener listener) {
        this.listener = listener;
    }

    /**
     * Inflates the entrant row layout and creates a ViewHolder.
     *
     * @param parent  Parent ViewGroup into which the row will be attached.
     * @param viewType Not used—single view type supported.
     * @return A new {@link EntrantViewHolder}.
     */
    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    /**
     * Binds the data for a single entrant into the row UI.
     *
     * @param holder   The ViewHolder representing the row.
     * @param position The position of the entrant in the adapter list.
     */
    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        if (entrants != null && position < entrants.size()) {
            Entrant entrant = entrants.get(position);
            holder.bind(entrant, viewType);
        }
    }

    /**
     * @return Number of entrants currently displayed.
     */
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

        /**
         * Binds an entrant's details into the row, adjusting the visible fields
         * based on the adapter's view type (invited, cancelled, enrolled).
         *
         * <p>This includes:</p>
         * <ul>
         *     <li>Rendering the entrant's display status with color-coded badge</li>
         *     <li>Formatting timestamps to "MMM dd, yyyy"</li>
         *     <li>Showing cancellation reason only for cancelled entrants</li>
         *     <li>Calling the click listener when a row is selected</li>
         * </ul>
         *
         * @param entrant  The entrant record to bind.
         * @param viewType The current list type being displayed.
         */
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

        /**
         * Sets the entrant status label text and background color.
         *
         * <p>Color coding:</p>
         * <ul>
         *     <li>accepted → Green</li>
         *     <li>declined → Red</li>
         *     <li>pending → Orange</li>
         *     <li>cancelled → Gray</li>
         *     <li>enrolled → Blue</li>
         *     <li>invited → Purple</li>
         *     <li>default → Dark Gray</li>
         * </ul>
         *
         * @param status The entrant's current status string.
         */
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