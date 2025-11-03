package com.example.ajilore.code.ui.events.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Events feed that matches the required item_event.xml structure.
 *
 * IMPORTANT: item_event.xml provides these views:
 *  - ivPoster            (banner image)
 *  - tvDate              (small blue badge text sitting on the banner)
 *  - tvEventTitle        (white ribbon text on the banner; we mirror the title here)
 *  - tvRegClosesBanner   (red "REGISTRATION CLOSES..." ribbon; optional)
 *  - tvTitle             (bold title in the content section)
 *  - tvWaitlist          ("20 on Waitlist"; optional — we hide if not used)
 *  - chipStatus          (status chip; "Open"/"Closed"/etc.; optional)
 *  - tvLocation          (location row)
 *  - btnViewDetails      (CTA button at the bottom)
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {

    /** Row click callback. We’ll call it for both the whole card and the CTA button. */
    public interface OnEventClick { void onClick(EventRow row); }

    private final List<EventRow> items = new ArrayList<>();
    private final OnEventClick onClick;
    @LayoutRes private final int layoutId;

    public EventsAdapter(@LayoutRes int layoutId, @NonNull OnEventClick onClick) {
        this.layoutId = layoutId;      // should be R.layout.item_event for this design
        this.onClick = onClick;
    }

    /** Replace entire list; simple + fine for labs. */
    public void replaceAll(@NonNull List<EventRow> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), onClick);
    }

    @Override
    public int getItemCount() { return items.size(); }

    /** Holds references to item_event.xml views and binds one EventRow into them. */
    static class VH extends RecyclerView.ViewHolder {
        private final ImageView ivPoster;
        private final TextView tvDate;
        private final TextView tvEventTitle;       // ribbon over image
        private final TextView tvRegClosesBanner;  // red banner (optional)
        private final TextView tvTitle;            // bold title in content
        private final TextView tvWaitlist;         // optional; we’ll hide by default
        private final TextView chipStatus;         // optional status chip
        private final TextView tvLocation;
        private final Button btnViewDetails;

        VH(@NonNull View itemView) {
            super(itemView);
            ivPoster          = itemView.findViewById(R.id.ivPoster);
            tvDate            = itemView.findViewById(R.id.tvDate);
            tvEventTitle      = itemView.findViewById(R.id.tvEventTitle);
            tvRegClosesBanner = itemView.findViewById(R.id.tvRegClosesBanner);
            tvTitle           = itemView.findViewById(R.id.tvTitle);
            tvWaitlist        = itemView.findViewById(R.id.tvWaitlist);
            chipStatus        = itemView.findViewById(R.id.chipStatus);
            tvLocation        = itemView.findViewById(R.id.tvLocation);
            btnViewDetails    = itemView.findViewById(R.id.btnViewDetails);
        }

        void bind(@NonNull EventRow row, @NonNull OnEventClick onClick) {
            // 1) Poster image
            ivPoster.setImageResource(row.posterRes);

            // 2) Title binding:
            //    - tvEventTitle = white ribbon on the banner (visual flare)
            //    - tvTitle      = main title text in content area
            tvEventTitle.setText(row.title);
            tvTitle.setText(row.title);

            // 3) Date badge text (your EventsFragment already formats row.dateText)
            tvDate.setText(row.dateText);

            // 4) Location line
            tvLocation.setText(row.location);

            // 5) Optional waitlist (we don’t have a count in EventRow yet -> hide)
            if (tvWaitlist != null) {
                tvWaitlist.setVisibility(View.GONE);
            }

            // 6) Optional status chip (show if non-empty; color is basic)
            if (chipStatus != null) {
                if (row.status == null || row.status.trim().isEmpty()) {
                    chipStatus.setVisibility(View.GONE);
                } else {
                    chipStatus.setVisibility(View.VISIBLE);
                    chipStatus.setText(row.status);
                    // simple tinting rule-of-thumb; you can swap to Material chips later
                    if ("open".equalsIgnoreCase(row.status)) {
                        chipStatus.setBackgroundResource(R.drawable.bg_chip_green);
                    } else if ("closed".equalsIgnoreCase(row.status)) {
                        chipStatus.setBackgroundResource(R.drawable.bg_chip_red);
                    } else {
                        chipStatus.setBackgroundResource(R.drawable.bg_chip_grey);
                    }
                }
            }

            // 7) Optional red registration banner — hide for now unless you later pass text
            if (tvRegClosesBanner != null) {
                tvRegClosesBanner.setVisibility(View.GONE);
            }

            // 8) Clicks: both the whole card and the CTA button route to onClick(row)
            itemView.setOnClickListener(v -> onClick.onClick(row));
            if (btnViewDetails != null) {
                btnViewDetails.setOnClickListener(v -> onClick.onClick(row));
            }
        }
    }
}
