package com.example.ajilore.code.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.adapters.AdminImagesAdapter;
import com.example.ajilore.code.controllers.AdminController;
import com.example.ajilore.code.models.ImageItem;

import java.util.List;

/**
 * Fragment for browsing and managing images in the administrative interface.
 *
 * <p>This fragment provides administrators with the ability to view all uploaded images
 * in the system, specifically event posters. Administrators can moderate content by
 * removing inappropriate images that violate application policies.</p>
 *
 * <p>Design Pattern: Fragment pattern from Android framework. Part of the overall
 * admin navigation structure implementing the Model-View-Controller pattern.</p>
 *
 * <p>User Stories Implemented:</p>
 * <ul>
 *   <li>US 03.06.01 - As an administrator, I want to be able to browse images
 *       that are uploaded so I can remove them if necessary</li>
 *   <li>US 03.03.01 - As an administrator, I want to be able to remove images</li>
 * </ul>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Display all uploaded event poster images in a grid layout</li>
 *   <li>Remove inappropriate or policy-violating images</li>
 *   <li>View image with associated event name</li>
 *   <li>Clean, non-cluttered UI with only actual images (no defaults/nulls)</li>
 * </ul>
 *
 * @author Dinma (Team Quartz)
 * @version 1.1
 * @since 2025-11-01
 */
public class AdminImagesFragment extends Fragment implements AdminImagesAdapter.OnImageActionListener {

    private RecyclerView rvImages;
    private AdminImagesAdapter adapter;
    private AdminController adminController;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    /**
     * Creates and returns the view hierarchy for the images browsing interface.
     *
     * @param inflater The LayoutInflater object to inflate views in the fragment
     * @param container The parent view that the fragment's UI will be attached to
     * @param savedInstanceState Previous saved state of the fragment, or null
     * @return The root View of the fragment's layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

        initializeViews(view);
        adminController = new AdminController();
        setupRecyclerView();
        setupBackButton();
        loadImages();

        return view;
    }

    /**
     * Initializes all view references.
     * @param view Root view of the fragment
     */
    private void initializeViews(View view) {
        rvImages = view.findViewById(R.id.rv_images);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
        btnBack = view.findViewById(R.id.btn_back);
    }

    /**
     * Sets up the RecyclerView with GridLayoutManager for image grid display.
     */
    private void setupRecyclerView() {
        adapter = new AdminImagesAdapter(requireContext(), this);

        // Use GridLayoutManager with 2 columns for clean grid display
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvImages.setLayoutManager(gridLayoutManager);
        rvImages.setAdapter(adapter);
    }

    /**
     * Sets up the back button to navigate to the previous screen.
     */
    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    /**
     * Loads all event poster images from Firestore.
     * Only displays events that have actual poster images (no nulls/defaults).
     */
    private void loadImages() {
        showLoading(true);

        adminController.fetchAllImages(new AdminController.DataCallback<List<ImageItem>>() {
            @Override
            public void onSuccess(List<ImageItem> images) {
                if (isAdded()) {
                    showLoading(false);
                    adapter.setImages(images);
                    updateEmptyState();

                    String message = images.isEmpty() ?
                            "No images found" :
                            "Loaded " + images.size() + " images";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            "Error loading images: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    updateEmptyState();
                }
            }
        });
    }

    /**
     * Shows or hides the loading indicator.
     * @param show True to show loading, false to hide
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvImages.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Updates the empty state visibility based on whether images are loaded.
     */
    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvImages.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Called when an image is clicked to view full details.
     * @param imageItem The clicked image item
     */
    @Override
    public void onImageClick(ImageItem imageItem) {
        // Show image details in a toast
        Toast.makeText(requireContext(),
                "Event: " + imageItem.title,
                Toast.LENGTH_SHORT).show();

        // Future enhancement: Open full-screen image viewer
    }

    /**
     * Called when delete button is clicked for an image.
     * Shows confirmation dialog before deletion.
     * @param imageItem The image item to delete
     */
    @Override
    public void onDeleteClick(ImageItem imageItem) {
        showDeleteDialog(imageItem);
    }

    /**
     * Shows a confirmation dialog before deleting an image.
     * @param imageItem The image item to delete
     */
    private void showDeleteDialog(ImageItem imageItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_delete_confirmation, null);

        TextView messageText = dialogView.findViewById(R.id.tv_dialog_message);
        messageText.setText("Are you sure you want to remove the poster for \"" +
                imageItem.title + "\"? This action cannot be undone.");

        Button deleteButton = dialogView.findViewById(R.id.btn_delete);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.setView(dialogView).create();

        deleteButton.setOnClickListener(v -> {
            deleteImage(imageItem);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Deletes an image from Firebase Storage and updates Firestore.
     * @param imageItem The image item to delete
     */
    private void deleteImage(ImageItem imageItem) {
        adminController.removeImage(imageItem, new AdminController.OperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Image deleted",
                            Toast.LENGTH_SHORT).show();
                    loadImages(); // Reload the grid
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Error deleting image: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}