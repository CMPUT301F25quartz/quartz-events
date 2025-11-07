package com.example.ajilore.code.ui.events.data;

/**
 * Lightweight model representing a single entrant row for waitlists or event invites.
 * Stores the user's display name, UID, and current waiting/responded status.
 */
public class Entrant {

    /** Unique user ID for this entrant (Firestore doc id). */
    public final String uid;

    /** Display name or UID used for showing in the UI. */
    public final String nameOrUid;

    /** Status label for display ("Pending", "Accepted", "Declined", etc.). */
    public final String displayStatus;

    /**
     * Constructs a new Entrant for use in lists and adapters.
     *
     * @param uid           Unique user/document ID (never null).
     * @param name          Display name; will fall back to UID if empty or null.
     * @param displayStatus Display status label; defaults to "Pending" if empty or null.
     */
    public Entrant(String uid, String name, String displayStatus) {
        this.uid = uid;
        this.nameOrUid = (name == null || name.isEmpty()) ? uid : name;
        this.displayStatus = (displayStatus == null || displayStatus.isEmpty()) ? "Pending" : displayStatus;
    }
}
