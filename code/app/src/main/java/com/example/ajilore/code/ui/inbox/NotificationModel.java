package com.example.ajilore.code.ui.inbox;

public class NotificationModel {
    private String id;
    private String message;
    private String time;
    private String imageUrl;
    private boolean isRead;
    private String actionText;
    private String type;
    private boolean isArchived;

    public NotificationModel() {}

    public NotificationModel(String id, String message, String time, String imageUrl,
                             boolean isRead, String actionText, String type, boolean isArchived) {
        this.id = id;
        this.message = message;
        this.time = time;
        this.imageUrl = imageUrl;
        this.isRead = isRead;
        this.actionText = actionText;
        this.type = type;
        this.isArchived = isArchived;
    }

    // Getters
    public String getId() { return id; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getImageUrl() { return imageUrl; }
    public boolean isRead() { return isRead; }
    public String getActionText() { return actionText; }
    public String getType() { return type; }
    public boolean isArchived() { return isArchived; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setMessage(String message) { this.message = message; }
    public void setTime(String time) { this.time = time; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setRead(boolean read) { isRead = read; }
    public void setActionText(String actionText) { this.actionText = actionText; }
    public void setType(String type) { this.type = type; }
    public void setArchived(boolean archived) { isArchived = archived; }
}
