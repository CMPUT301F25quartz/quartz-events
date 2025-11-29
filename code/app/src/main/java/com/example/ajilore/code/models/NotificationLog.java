package com.example.ajilore.code.models;

import com.google.firebase.Timestamp;

/**
 * Model representing an immutable log entry for the Admin.
 * Stored in "notification_logs" collection.
 */
public class NotificationLog {
    private String logId;
    private String eventId;
    private String eventTitle;
    private String message;
    private String audience; // e.g., "waiting", "selected", "chosen", "cancelled"
    private Timestamp timestamp;

    private String senderId;
    private int recipientCount;

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
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public int getRecipientCount() { return recipientCount; }
    public void setRecipientCount(int recipientCount) { this.recipientCount = recipientCount; }

}