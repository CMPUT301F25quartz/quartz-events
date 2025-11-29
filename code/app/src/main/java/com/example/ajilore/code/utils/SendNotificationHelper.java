package com.example.ajilore.code.utils;

import android.content.Context;
import android.util.Log; // Import Log
import android.widget.Toast;
import com.example.ajilore.code.utils.AdminAuthManager; // Import AdminAuthManager
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class SendNotificationHelper {

    private static final String TAG = "SendNotificationHelper"; // Log tag

    /**
     * Sends a notification to a chosen entrant if their preferences allow it.
     * Writes to the new structure: /users/{userId}/registrations/{eventId}/inbox
     *
     * @param context Application or Fragment context for Toast
     * @param userId  UID of the chosen entrant (should be the device ID used by AdminAuthManager)
     * @param eventId Firestore event ID
     * @param message Notification message
     * @param type    Notification type (e.g., "lottery_winner", "general")
     */
    public static void sendNotification(Context context, String userId, String eventId,
                                        String message, String type) {
        if (userId == null || userId.isEmpty() || eventId == null || eventId.isEmpty()) {
            Toast.makeText(context, "Invalid user or event ID", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "sendNotification: Invalid userId (" + userId + ") or eventId (" + eventId + ")");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // STEP 1: Check notification preferences before sending
        db.collection("users")
                .document(userId)
                .collection("preferences")
                .document("notifications")
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean enabled = true; // Default to true
                    if (snapshot.exists()) {
                        Boolean prefValue = snapshot.getBoolean("enabled");
                        if (prefValue != null) enabled = prefValue;
                    }

                    if (!enabled) {
                        String msg = "User has opted out of notifications: " + userId;
                        Log.d(TAG, msg);
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        return; // Exit early if opted out
                    }

                    Log.d(TAG, "Sending notification to user: " + userId + " for event: " + eventId);

                    // STEP 2: Build notification data
                    Map<String, Object> notifData = new HashMap<>();
                    notifData.put("message", message != null ? message : "You have a new notification!");
                    notifData.put("type", type != null ? type : "general");
                    notifData.put("read", false);
                    notifData.put("archived", false);
                    notifData.put("createdAt", FieldValue.serverTimestamp());
                    notifData.put("actionText", "See Details");

                    // STEP 3: Send to user's inbox under the specific event registration
                    db.collection("users")
                            .document(userId) // Use the provided userId (device ID)
                            .collection("registrations")
                            .document(eventId) // Use the provided eventId
                            .collection("inbox") // Target the inbox subcollection
                            .add(notifData) // Add creates a new document with a unique ID
                            .addOnSuccessListener(docRef -> {
                                String msg = "Notification sent successfully to user: " + userId + " for event: " + eventId;
                                Log.d(TAG, msg);
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                String errorMsg = "Failed to send notification to user: " + userId + " for event: " + eventId + ". Error: " + e.getMessage();
                                Log.e(TAG, errorMsg, e);
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // If preferences check fails, assume enabled and send anyway, or handle differently
                    Log.w(TAG, "Failed to check preferences for user: " + userId + ", sending notification anyway.", e);
                    // You might choose to send the notification anyway, or handle this differently
                    // For now, let's send it but log the warning.

                    Map<String, Object> notifData = new HashMap<>();
                    notifData.put("message", message != null ? message : "You have a new notification!");
                    notifData.put("type", type != null ? type : "general");
                    notifData.put("read", false);
                    notifData.put("archived", false);
                    notifData.put("createdAt", FieldValue.serverTimestamp());
                    notifData.put("actionText", "See Details");

                    db.collection("users")
                            .document(userId)
                            .collection("registrations")
                            .document(eventId)
                            .collection("inbox")
                            .add(notifData)
                            .addOnSuccessListener(docRef -> {
                                String msg = "Notification sent (preferences check failed) to user: " + userId + " for event: " + eventId;
                                Log.d(TAG, msg);
                                // Optionally inform user that notification was sent despite preference check failure
                                // Toast.makeText(context, "Notification sent (preferences check failed)", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(sendE -> {
                                String errorMsg = "Failed to send notification (after preference failure) to user: " + userId + " for event: " + eventId + ". Error: " + sendE.getMessage();
                                Log.e(TAG, errorMsg, sendE);
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                            });
                });
    }
}