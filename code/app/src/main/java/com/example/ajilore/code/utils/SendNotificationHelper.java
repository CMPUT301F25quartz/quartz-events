package com.example.ajilore.code.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SendNotificationHelper {

    /**
     * Sends a notification to a chosen entrant AND logs it for the admin.
     *
     * @param context Application or Fragment context for Toast
     * @param userId  UID of the chosen entrant
     * @param eventId Firestore event ID
     * @param message Notification message
     * @param type    Notification type (e.g., "lottery_winner", "general")
     * @param senderId Sender ID
     */
    public static void sendNotification(Context context, String userId, String eventId,
                                        String message, String type, String senderId) {

        if (userId == null || userId.isEmpty() || eventId == null || eventId.isEmpty()) {
            Toast.makeText(context, "Invalid user or event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Prepare User Notification Data (Existing Logic)
        Map<String, Object> notifData = new HashMap<>();
        notifData.put("message", message != null ? message : "You have a new notification!");
        notifData.put("type", type != null ? type : "general");
        notifData.put("read", false);
        notifData.put("createdAt", FieldValue.serverTimestamp());
        notifData.put("actionText", "See Details");

        // 2. Prepare Admin Log Data (NEW LOGIC)
        // We capture this separately so Admin has a central list
        Map<String, Object> logData = new HashMap<>();
        logData.put("eventId", eventId);
        logData.put("message", message);
        logData.put("audience", "Single User (" + userId + ")"); // Track who it went to
        logData.put("type", type);
        logData.put("senderId", senderId);
        logData.put("timestamp", FieldValue.serverTimestamp());

        // 3. Perform Batch Write (Optional but cleaner) or Chained Writes

        // Write to User Inbox
        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId)
                .collection("inbox")
                .add(notifData)
                .addOnSuccessListener(docRef -> {


                    String inboxId = docRef.getId();

                    // NEW: mirror the same inbox item under users/{userId}/registrations/{eventId}/inbox
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .collection("registrations")
                            .document(eventId)
                            .collection("inbox")
                            .document(inboxId)
                            .set(notifData);


                    // ON SUCCESS: Write to Admin Log
                    db.collection("notification_logs")
                            .add(logData)
                            .addOnSuccessListener(logRef -> {
                                // Log created silently
                            });

                    Toast.makeText(context, "Notification sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}