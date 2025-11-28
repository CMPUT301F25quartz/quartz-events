package com.example.ajilore.code.models;

import com.google.firebase.Timestamp;

/**
 * Model representing an immutable log entry for the Admin.
 * Stored in "notification_logs" collection.
 */
public class NotificationLog {
    private String logId;
    private String eventId;
    private String message;
    private String audience; // e.g., "waiting", "selected", "chosen", "cancelled"
    private Timestamp timestamp;

    private String senderId;

    public NotificationLog() {} // Required for Firestore

    public NotificationLog(String eventId, String message, String audience, Timestamp timestamp) {
        this.eventId = eventId;
        this.message = message;
        this.audience = audience;
        this.timestamp = timestamp;
        this.senderId = senderId;
    }

    // Getters and Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getEventId() { return eventId; }
    public String getMessage() { return message; }
    public String getAudience() { return audience; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getSenderId() { return senderId; }
}