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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying users in admin browse view.
 *
 * <p>This RecyclerView adapter is responsible for binding user profile data to list items
 * in the administrator's user browsing interface. It displays user information and provides
 * interaction callbacks for administrative actions such as viewing details or removing users.</p>
 *
 * <p>Design Pattern: This class implements the Adapter pattern (part of RecyclerView pattern)
 * and uses the ViewHolder pattern for efficient view recycling.</p>
 *
 * <p>User Story: US 03.05.01 - As an administrator, I want to be able to browse profiles.</p>
 *
 * <p>Outstanding Issues:</p>
 * <ul>
 *   <li>Consider implementing pagination for large user lists</li>
 *   <li>Add search/filter functionality for better user experience</li>
 * </ul>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private List<User> userListFull;  // For filtering/searching
    private OnUserActionListener listener;

    /**
     * Interface for handling user action callbacks.
     *
     * <p>Implementing classes can respond to user deletion and viewing actions
     * initiated from the adapter's list items.</p>
     */
    public interface OnUserActionListener {
        /**
         * Called when the delete action is triggered for a user.
         *
         * @param user The user object to be deleted
         */
        void onDeleteClick(User user);
        /**
         * Called when a user item is clicked to view details.
         *
         * @param user The user object whose details should be displayed
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
     * Updates/sets the user list for the adapter.
     * @param users The full set of User profiles to display
     */
    public void setUsers(List<User> users) {
        this.userList = new ArrayList<>(users);
        this.userListFull = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    /**
     * Filters user profiles based on query string (by name or email).
     * @param query Filter string, case-insensitive
     */
    public void filter(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (User user : userListFull) {
                if (user.getName() != null && user.getName().toLowerCase().contains(lowerCaseQuery) ||
                        user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    userList.add(user);
                }
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

        holder.tvUserName.setText(user.getName() != null ? user.getName() : "Unknown User");

        // Load profile image
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(holder.ivUserImage);
        } else {
            holder.ivUserImage.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        // Click listeners
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
     * ViewHolder for user list items.
     *
     * <p>Holds references to views within each user list item for efficient recycling.</p>
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserImage;
        TextView tvUserName;
        ImageButton btnDelete;

        /**
         * Binds row view references.
         * @param itemView The user row's root view
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserImage = itemView.findViewById(R.id.iv_user_image);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}