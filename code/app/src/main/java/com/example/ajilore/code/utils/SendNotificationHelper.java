package com.example.ajilore.code.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

import com.example.ajilore.code.MainActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class SendNotificationHelper {

    private static final String CHANNEL_ID = "notification_channel";

    /**
     * Sends a notification to a chosen entrant if their preferences allow it.
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

        db.collection("users").document(userId).get()
                .addOnSuccessListener(userSnap -> {
                    // DEFAULT = enabled true
                    Boolean enabled = userSnap.getBoolean("notificationsEnabled");

                    if (enabled != null) {
                        // We got preference from User model
                        writeInboxNotification(db, userId, eventId, message, type, enabled, context);
                    } else {
                        // Try fallback sub-collection
                        db.collection("users").document(userId)
                                .collection("preferences").document("notifications")
                                .get()
                                .addOnSuccessListener(prefSnap -> {
                                    Boolean fallbackEnabled = prefSnap.getBoolean("enabled");
                                    writeInboxNotification(
                                            db,
                                            userId,
                                            eventId,
                                            message,
                                            type,
                                            fallbackEnabled != null ? fallbackEnabled : true,
                                            context
                                    );
                                })
                                .addOnFailureListener(e -> {
                                    // Could not read fallback → set default true
                                    writeInboxNotification(db, userId, eventId, message, type, true, context);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Could not read user doc → set default true
                    writeInboxNotification(db, userId, eventId, message, type, true, context);
                });
    }


    // helper to actually write the inbox notification doc and potentially show popup
    private static void writeInboxNotification(FirebaseFirestore db, String userId, String eventId,
                                               String message, String type, boolean showPopup, Context context) {

        Map<String, Object> notifData = new HashMap<>();
        notifData.put("message", message != null ? message : "You have a new notification!");
        notifData.put("type", type != null ? type : "general");
        notifData.put("read", false);
        notifData.put("archived", false);
        notifData.put("createdAt", FieldValue.serverTimestamp());
        notifData.put("actionText", "See Details");
        notifData.put("showPopup", showPopup); // Store this for potential popup display

        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId)
                .collection("inbox")
                .add(notifData)
                .addOnSuccessListener(docRef -> {
                    // Show popup only if user has enabled notifications
                    if (showPopup) {
                        showPopupNotification(context, message, type, docRef.getId());
                    }

                    Toast.makeText(context, "Notification recorded in inbox", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Method to show actual popup notification
    private static void showPopupNotification(Context context, String message, String type, String notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Notifications";
            String channelDescription = "App notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            notificationManager.createNotificationChannel(channel);
        }

        // Create intent to open app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Fix: Use PendingIntent.FLAG_IMMUTABLE for Android 12+
        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your actual icon
                .setContentTitle("New Notification")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Generate a unique ID for the notification
        int notificationIdInt = Math.abs(notificationId.hashCode()) % 10000; // Use hash of doc ID as notification ID
        notificationManager.notify(notificationIdInt, builder.build());
    }
}