package com.example.ajilore.code.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ajilore.code.R;
import com.example.ajilore.code.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for rendering the list of User Profiles in the Admin Dashboard.
 *
 * <p><b>Implements US 03.05.01 (Browse Profiles):</b>
 * Displays a list of users including their avatar, name, and role.
 * Includes search filtering capabilities by name and role filtering (Entrant vs Organizer).</p>
 *
 * @author Dinma (Team Quartz)
 * @version 1.2
 */
public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {

    private final Context context;
    private List<User> userList;        // The currently displayed (filtered) list
    private final List<User> userListFull; // The complete dataset (unfiltered)
    private final OnUserActionListener listener;

    /**
     * Interface for handling user action callbacks.
     *
     * <p>Implementing classes can respond to user deletion and viewing actions
     * initiated from the adapter's list items.</p>
     */
    public interface OnUserActionListener {
        /**
         * Triggered when the "Delete" button is clicked on a user row.
         * @param user The user targeted for deletion.
         */
        void onDeleteClick(User user);
        /**
         * Triggered when a user row is clicked (usually to view details).
         * @param user The selected user.
         */
        void onUserClick(User user);
    }

    /**
     * Constructs a new AdminUsersAdapter.
     *
     * @param context The context in which the adapter is operating
     * @param listener The listener for user action callbacks
     */
    public AdminUsersAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.userList = new ArrayList<>();
        this.userListFull = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the underlying data set and refreshes the view.
     * Creates a copy of the data for the 'Full' list to enable client-side filtering.
     *
     * @param users List of User objects to display.
     */
    public void setUsers(List<User> users) {
        this.userList = new ArrayList<>(users);
        this.userListFull.clear();
        this.userListFull.addAll(users);
        notifyDataSetChanged();
    }

    /**
     * Filters the displayed list based on search text and role selection.
     *
     * @param query      Search string to match against Name or Email (case-insensitive).
     * @param roleFilter Role criteria: "All", "Organizers", or "Entrants".
     */

    public void filter(String query, String roleFilter) {
        userList.clear();

        // Normalize query
        String lowerCaseQuery = query.toLowerCase();

        for (User user : userListFull) {
            boolean matchesSearch = false;
            boolean matchesRole = false;

            // 1. Text Search Logic
            if (query.isEmpty()) {
                matchesSearch = true;
            } else {
                if ((user.getName() != null && user.getName().toLowerCase().contains(lowerCaseQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseQuery))) {
                    matchesSearch = true;
                }
            }

            // 2. Role Filter Logic
            // Note: Firebase stores role as "organiser" (British spelling)
            if (roleFilter.equals("All")) {
                matchesRole = true;
            } else if (roleFilter.equals("Organizers") && "organiser".equalsIgnoreCase(user.getRole())) {
                matchesRole = true;
            } else if (roleFilter.equals("Entrants") && !"organiser".equalsIgnoreCase(user.getRole())) {
                matchesRole = true;
            }

            // 3. Add if BOTH match
            if (matchesSearch && matchesRole) {
                userList.add(user);
            }
        }
        notifyDataSetChanged();
    }
    /**
     * Inflates a view and ViewHolder for a user row.
     * @param parent   Parent ViewGroup
     * @param viewType Not used
     * @return A UserViewHolder bound to its views
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds the user data and click actions for a single item.
     * @param holder  The UserViewHolder being bound
     * @param position Position of the user in the filtered list
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Bind Name
        holder.tvUserName.setText(user.getName() != null ? user.getName() : "Unknown User");

        // Bind Role Badge (Color coding helps Admins quickly distinguish roles)
        holder.tvRoleBadge.setVisibility(View.VISIBLE);

        if ("organiser".equalsIgnoreCase(user.getRole())) {
            holder.tvRoleBadge.setText("Organizer");
            holder.tvRoleBadge.setBackgroundResource(R.drawable.badge_organizer);
            holder.tvRoleBadge.setTextColor(android.graphics.Color.parseColor("#E65100")); // Dark Orange text
        } else {
            holder.tvRoleBadge.setText("Entrant");
            holder.tvRoleBadge.setBackgroundResource(R.drawable.badge_entrant);
            holder.tvRoleBadge.setTextColor(android.graphics.Color.parseColor("#1565C0")); // Dark Blue text
        }

// Bind Image
        String imgUrl = user.getProfileImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(context)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .circleCrop()
                    .into(holder.ivUserImage);
        } else {
            holder.ivUserImage.setImageResource(R.drawable.ic_default_profile);
        }

        // Bind Actions
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(user);
        });
    }


    /**
     * @return The number of filtered users in the adapter.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder pattern to minimize expensive findViewById calls.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivUserImage;
        final TextView tvUserName;
        final ImageButton btnDelete;
        final TextView tvRoleBadge;

        /**
         * Binds row view references.
         * @param itemView The user row's root view
         */
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserImage = itemView.findViewById(R.id.iv_user_image);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            tvRoleBadge = itemView.findViewById(R.id.tv_role_badge);
        }
    }
}