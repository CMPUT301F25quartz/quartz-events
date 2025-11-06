package com.example.ajilore.code.ui.inbox;

public class NotificationModel {
    private String id;
    private String message;
    private String time;
    private String imageUrl;
    private boolean isRead;
    private String actionText; // e.g. "See Details" or "See Event"
    private String type;       // e.g. "lottery_winner", "lottery_loser", "organizer"

    public NotificationModel() {} // Firestore requirement

    public NotificationModel(String id, String message, String time, String imageUrl,
                             boolean isRead, String actionText, String type) {
        this.id = id;
        this.message = message;
        this.time = time;
        this.imageUrl = imageUrl;
        this.isRead = isRead;
        this.actionText = actionText;
        this.type = type;
    }

    // Short constructor for local/demo data
    public NotificationModel(String id, String message, boolean isRead, String type) {
        this.id = id;
        this.message = message;
        this.isRead = isRead;
        this.type = type;
        this.time = "";
        this.imageUrl = "";
        this.actionText = "See Details";
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getImageUrl() { return imageUrl; }
    public boolean isRead() { return isRead; }
    public String getActionText() { return actionText; }
    public String getType() { return type; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
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
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
