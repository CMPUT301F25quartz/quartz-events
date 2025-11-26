package com.example.ajilore.code.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.ajilore.code.R;

public class DeleteDialogHelper {

    /**
     * Shows a generic delete confirmation dialog.
     *
     * @param context The context (Activity or requireContext())
     * @param itemType The type of item (e.g., "Event", "Image", "Profile") - for the Title
     * @param itemName The specific name (e.g., "Intro to Java", "poster_123.jpg") - for the Message
     * @param onConfirm The action to run if the user clicks Delete
     */
    public static void showDeleteDialog(Context context, String itemType, String itemName, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate the generic layout
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_delete_confirmation, null);

        // 1. Setup Title dynamically
        TextView titleText = dialogView.findViewById(R.id.tv_dialog_title);
        titleText.setText("Delete " + itemType + "?");

        // 2. Setup Message dynamically
        TextView messageText = dialogView.findViewById(R.id.tv_dialog_message);
        // Assuming your string resource is: "Are you sure you want to delete %1$s?"
        String confirmationMessage = context.getString(
                R.string.delete_confirmation_message,
                itemName
        );
        messageText.setText(confirmationMessage);

        Button deleteButton = dialogView.findViewById(R.id.btn_delete);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.setView(dialogView).create();

        // 3. Setup Buttons
        deleteButton.setOnClickListener(v -> {
            // Run the specific delete logic passed from the Fragment
            onConfirm.run();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}