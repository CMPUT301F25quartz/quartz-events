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




    /**
     * Default no-argument constructor required by Firestore.
     *
     * <p>Firestore automatically populates fields via reflection, so this must remain public
     * and empty.</p>
     */
    public NotificationModel() {} // Firestore requirement




    /**
     * Creates a fully populated notification model.
     *
     * @param eventId         ID of the event this notification relates to.
     * @param firestoreDocId  Firestore document ID under the inbox subcollection.
     * @param message         Message text sent by the organizer.
     * @param time            User-friendly timestamp string (e.g., "Nov 27, 5:23 PM").
     * @param imageUrl        URL of sender profile image (may be empty).
     * @param isRead          Whether the user has opened/read the notification.
     * @param actionText      Label used for the action button (e.g., "See Details").
     * @param type            Notification category ("general", "chosen", "selected", etc.).
     */
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

    /**
     * Creates a simplified notification model when only basic fields are known.
     *
     * <p>Used for lightweight Firestore mappings where timestamp or images are added later.</p>
     *
     * @param eventId        Related event ID.
     * @param firestoreDocId Firestore inbox document ID.
     * @param message        Notification message text.
     * @param isRead         Read/unread flag.
     * @param type           Notification type category.
     */
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


    /**
     * Two notifications are considered equal if they share the same
     * Firestore inbox document ID. This allows RecyclerView logic to
     * detect duplicates and prevents re-adding existing notifications
     * when Firestore snapshot listeners refresh.
     *
     * @param obj Another object to compare.
     * @return true if both represent the same Firestore inbox document.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NotificationModel)) return false;
        NotificationModel other = (NotificationModel) obj;
        return firestoreDocId != null && firestoreDocId.equals(other.firestoreDocId);
    }


    /**
     * HashCode is based solely on the Firestore document ID so the model
     * works correctly with collections such as HashSet or when RecyclerView
     * checks for item uniqueness.
     *
     * @return Hash based on Firestore document ID.
     */
    @Override
    public int hashCode() {
        return firestoreDocId != null ? firestoreDocId.hashCode() : 0;
    }
}
