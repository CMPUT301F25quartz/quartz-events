package com.example.ajilore.code.models;

/**
 * User model class representing a user in the system.
 * Maps to the "users" collection in Firestore.
 */
public class User {

    /** Unique device or Firestore document ID for the user. */
    private String userId;

    /** User's display name. */
    private String name;

    /** User's email address. */
    private String email;

    /** User's phone number. */
    private String phone;

    /** User's role in the system. */
    private String role;

    /** URL for the user's profile image. */
    private String profileImageUrl;

    /** Whether notifications are enabled for the user. */
    private boolean notificationsEnabled;

    /**
     * No-arg constructor required for Firestore deserialization.
     */
    public User() {}

    /**
     * Constructs a new User with required basic fields.
     * Notifications are enabled by default.
     *
     * @param userId User/device ID or Firestore document ID.
     * @param name User's full name.
     * @param email User's email address.
     * @param phone User's phone number.
     * @param role User's role (entrant, organizer, admin).
     */
    public User(String userId, String name, String email, String phone, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.notificationsEnabled = true;
    }


    // Getters and Setters

    /** @return User/document ID. */
    public String getUserId() { return userId; }

    /** Set the user's ID. @param userId Unique ID. */
    public void setUserId(String userId) { this.userId = userId; }


    /** @return User's display name. */
    public String getName() { return name; }

    /** Set the user's name. @param name Full name. */
    public void setName(String name) { this.name = name; }

    /** @return User's email address. */
    public String getEmail() { return email; }

    /** Set the user's email. @param email Email address. */
    public void setEmail(String email) { this.email = email; }

    /** @return User's phone number. */
    public String getPhone() { return phone; }

    /** Set the user's phone number. @param phone Phone string. */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return User's role ("entrant", "organizer", or "admin"). */
    public String getRole() { return role; }

    /** Set the user's role. @param role "entrant", "organizer", or "admin". */
    public void setRole(String role) { this.role = role; }

    /** @return URL for the user's profile image. */
    public String getProfileImageUrl() { return profileImageUrl; }

    /** Set the user's profile image URL. @param url Image URL string. */
    public void setProfileImageUrl(String url) { this.profileImageUrl = url; }

    /** @return True if notifications are enabled for the user; false otherwise. */
    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    /** Enable or disable user notifications. @param enabled True to enable. */
    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }
}