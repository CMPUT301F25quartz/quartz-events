package com.example.ajilore.code.ui.inbox;


/**
 * Model representing a single inbox notification for an entrant.
 *
 * <p>This class mirrors the Firestore structure stored under:</p>
 * <pre>
 * users/{deviceId}/registrations/{eventId}/inbox/{notificationId}
 * </pre>
 *
 * <p>Each notification may include:</p>
 * <ul>
 *     <li>A reference to the related event</li>
 *     <li>A message body written by the organizer</li>
 *     <li>A formatted timestamp string</li>
 *     <li>An optional sender profile image URL</li>
 *     <li>A read/unread state</li>
 *     <li>An action label (e.g., “See Details”)</li>
 *     <li>A type (“general”, “chosen”, “selected”, etc.)</li>
 * </ul>
 *
 * <p>Instances of this model are displayed inside the
 * {@link com.example.ajilore.code.ui.inbox.NotificationAdapter} and controlled by
 * {@link com.example.ajilore.code.ui.inbox.InboxFragment}.</p>
 *
 * <p>Firestore requires a public no-arg constructor for deserialization.</p>
 */





public class NotificationModel {
    private String eventId;          // Event this notification belongs to
    private String firestoreDocId;   // Firestore document ID of this inbox notification
    private String message;
    private String time;
    private String imageUrl;
    private boolean isRead;
    private String actionText;
    private String type;








    public NotificationModel() {} // Firestore requirement








    // Full constructor
    public NotificationModel(String eventId, String firestoreDocId, String message, String time, String imageUrl,
                             boolean isRead, String actionText, String type) {
        this.eventId = eventId;
        this.firestoreDocId = firestoreDocId;
        this.message = message;
        this.time = time;
        this.imageUrl = imageUrl;
        this.isRead = isRead;
        this.actionText = actionText;
        this.type = type;
    }








    // Short constructor
    public NotificationModel(String eventId, String firestoreDocId, String message, boolean isRead, String type) {
        this.eventId = eventId;
        this.firestoreDocId = firestoreDocId;
        this.message = message;
        this.isRead = isRead;
        this.type = type;
        this.time = "";
        this.imageUrl = "";
        this.actionText = "See Details";
    }








    // --- Getters & Setters ---
    public String getEventId() { return eventId; }
    public String getFirestoreDocId() { return firestoreDocId; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getImageUrl() { return imageUrl; }
    public boolean isRead() { return isRead; }




    public String getActionText() { return actionText; }
    public String getType() { return type; }








    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setFirestoreDocId(String firestoreDocId) { this.firestoreDocId = firestoreDocId; }
    public void setMessage(String message) { this.message = message; }
    public void setTime(String time) { this.time = time; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setRead(boolean read) { this.isRead = read; }
    public void setActionText(String actionText) { this.actionText = actionText; }
    public void setType(String type) { this.type = type; }








    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NotificationModel)) return false;
        NotificationModel other = (NotificationModel) obj;
        return firestoreDocId != null && firestoreDocId.equals(other.firestoreDocId);
    }








    @Override
    public int hashCode() {
        return firestoreDocId != null ? firestoreDocId.hashCode() : 0;
    }
}
