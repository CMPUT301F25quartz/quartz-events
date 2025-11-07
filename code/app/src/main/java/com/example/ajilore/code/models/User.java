package com.example.ajilore.code.models;

/**
 * User model class representing a user in the system.
 * Maps to the "users" collection in Firestore.
 */
public class User {
    private String userId;           // Device ID / Document ID
    private String name;
    private String email;
    private String phone;
    private String role;             // "entrant", "organizer", "admin"
    private String profileImageUrl;
    private boolean notificationsEnabled;

    // Empty constructor required for Firestore
    public User() {}

    public User(String userId, String name, String email, String phone, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.notificationsEnabled = true;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String url) { this.profileImageUrl = url; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }
}