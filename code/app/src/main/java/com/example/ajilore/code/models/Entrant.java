package com.example.ajilore.code.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Entrant {
    @DocumentId
    private String id;
    private String userId;
    private String eventId;
    private String name;
    private String email;
    private String phone;
    private String status; // "pending", "invited", "accepted", "declined", "cancelled", "enrolled"
    private String reason; // for cancelled status
    private Timestamp invitedDate;
    private Timestamp responseDate;
    private Timestamp cancelledDate;
    private Timestamp enrolledDate;

    public Entrant() {}

    public Entrant(String userId, String eventId, String name, String email, String phone) {
        this.userId = userId;
        this.eventId = eventId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = "pending";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Timestamp getInvitedDate() { return invitedDate; }
    public void setInvitedDate(Timestamp invitedDate) { this.invitedDate = invitedDate; }

    public Timestamp getResponseDate() { return responseDate; }
    public void setResponseDate(Timestamp responseDate) { this.responseDate = responseDate; }

    public Timestamp getCancelledDate() { return cancelledDate; }
    public void setCancelledDate(Timestamp cancelledDate) { this.cancelledDate = cancelledDate; }

    public Timestamp getEnrolledDate() { return enrolledDate; }
    public void setEnrolledDate(Timestamp enrolledDate) { this.enrolledDate = enrolledDate; }
}