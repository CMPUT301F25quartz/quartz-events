package com.example.ajilore.code.utils;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class SendNotificationHelper {

    /**
     * Sends a notification to a chosen entrant if their preferences allow it.
     *
     * @param context Application or Fragment context for Toast
     * @param userId  UID of the chosen entrant (Firebase Auth UID)
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
                        Toast.makeText(context, "User has opted out of notifications.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // STEP 2: Build notification data
                    Map<String, Object> notifData = new HashMap<>();
                    notifData.put("message", message != null ? message : "You have a new notification!");
                    notifData.put("type", type != null ? type : "general");
                    notifData.put("read", false);
                    notifData.put("archived", false);
                    notifData.put("createdAt", FieldValue.serverTimestamp());
                    notifData.put("actionText", "See Details");

                    // STEP 3: Send to user's inbox
                    db.collection("org_events")
                            .document(eventId)
                            .collection("waiting_list")
                            .document(userId)
                            .collection("inbox")
                            .add(notifData)
                            .addOnSuccessListener(docRef ->
                                    Toast.makeText(context, "Notification sent successfully!", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to check user preferences: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}