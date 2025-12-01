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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ajilore.code.R;
import com.example.ajilore.code.models.ImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying images in admin browse view.
 *
 * <p>This RecyclerView adapter manages the display of image data in a grid layout
 * for the administrator's image browsing interface. It shows event posters and
 * profile pictures with associated metadata and provides callbacks for admin actions.</p>
 *
 * <p>Design Pattern: Implements the Adapter pattern with ViewHolder pattern for
 * efficient grid rendering.</p>
 *
 * <p>User Story: US 03.06.01 - As an administrator, I want to be able to browse images.</p>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ImageViewHolder> {

    private final Context context;
    private List<ImageItem> imageList;
    private final List<ImageItem> imageListFull;  // Store unfiltered list for search
    private final OnImageActionListener listener;

    /**
     * Interface for admin interaction callbacks on images.
     */
    public interface OnImageActionListener {
        /**
         * Called when delete button is pressed for an image.
         * @param imageItem Image to delete.
         */
        void onDeleteClick(ImageItem imageItem);

        /**
         * Called when image is clicked to view full size.
         * @param imageItem Image to view.
         */
        void onImageClick(ImageItem imageItem);
    }

    /**
     * Constructs a new AdminImagesAdapter.
     * @param context Context used for layout inflation and Glide.
     * @param listener Callback interface for admin image actions.
     */
    public AdminImagesAdapter(Context context, OnImageActionListener listener) {
        this.context = context;
        this.imageList = new ArrayList<>();
        this.imageListFull = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Replaces the adapter's image list with new images and refreshes UI.
     * @param images List of images to display.
     */
    public void setImages(List<ImageItem> images) {
        this.imageList = new ArrayList<>(images);
        this.imageListFull.clear();
        this.imageListFull.addAll(images);
        notifyDataSetChanged();
    }

    /**
     * Filters the image list by event title.
     * Searches through event names to match the query.
     *
     * @param query The search query to filter by (case-insensitive)
     */
    public void filter(String query) {
        imageList.clear();

        if (query == null || query.isEmpty()) {
            // If query is empty, show all images
            imageList.addAll(imageListFull);
        } else {
            // Filter by event title (case-insensitive)
            String lowerCaseQuery = query.toLowerCase().trim();
            for (ImageItem imageItem : imageListFull) {
                if (imageItem.title != null &&
                        imageItem.title.toLowerCase().contains(lowerCaseQuery)) {
                    imageList.add(imageItem);
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Inflates the image item view and creates a ViewHolder.
     * @param parent The parent ViewGroup.
     * @param viewType View type (not used).
     * @return A new ImageViewHolder.
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds image data and click listeners to a ViewHolder.
     * @param holder The ViewHolder.
     * @param position Position of the image in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem imageItem = imageList.get(position);

        // Display image title
        holder.tvImageTitle.setText(imageItem.title);

        // Load image using Glide with error handling
        Glide.with(context)
                .load(imageItem.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.ivImage);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onImageClick(imageItem);
        });

    }

    /**
     * @return Number of images being displayed.
     */
    @Override
    public int getItemCount() {
        return imageList.size();
    }

    /**
     * ViewHolder for an image item.
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvImageTitle;

        /**
         * Binds view references from inflated layout.
         * @param itemView Root view of this image grid item.
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvImageTitle = itemView.findViewById(R.id.tv_image_title);
        }
    }
}