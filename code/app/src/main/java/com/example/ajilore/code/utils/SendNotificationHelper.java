package com.example.ajilore.code.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SendNotificationHelper {

    /**
     * Sends a notification to a chosen entrant.
     *
     * @param context Application or Fragment context for Toast
     * @param userId  UID of the chosen entrant
     * @param eventId Firestore event ID
     * @param message Notification message
     * @param type    Notification type (e.g., "lottery_winner", "general")
     */
    public static void sendNotification(Context context, String userId, String eventId,
                                        String message, String type) {

        if (userId == null || userId.isEmpty() || eventId == null || eventId.isEmpty()) {
            Toast.makeText(context, "Invalid user or event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare notification data
        Map<String, Object> notifData = new HashMap<>();
        notifData.put("message", message != null ? message : "You have a new notification!");
        notifData.put("type", type != null ? type : "general");
        notifData.put("read", false);        // default unread
        notifData.put("archived", false);    // default not archived
        notifData.put("createdAt", FieldValue.serverTimestamp());
        notifData.put("actionText", "See Details"); // optional for UI

        // Add notification to Firestore
        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId)
                .collection("inbox")
                .add(notifData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(context, "Notification sent successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
