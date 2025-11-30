package com.example.ajilore.code.ui.events.list;

/**
 * Lightweight UI model representing a single organizer event row for display in lists.
 * Holds all data needed to bind organizer event views, including a thumbnail and subtitle.
 */
public class EventItem {
    /** Firestore document ID for the event. */
    public final String eventId;

    /** Title to present in the organizer's list. */
    public final String title;

    /** Human-readable event date/time, pre-formatted for the view. */
    public final String dateText;

    /** Optional subtitle (e.g., type, location, or capacity string). */
    public final String subtitle;

    /** Drawable resource ID for the poster thumbnail. */
    public final int posterRes;

    /** Poster Cloudinary URL (if event poster is uploaded remotely); may be null. */
    public final String posterUrl;

    /** Status of the event (e.g., "flagged"). */
    public final String status;




    /**
     * Constructs a new immutable event item row for the organizer's event list.
     *
     * @param eventId   Firestore document ID
     * @param title     Event title for display
     * @param dateText  Pre-formatted human-readable date/time string
     * @param posterRes Drawable resource for the list image (for fallback or asset posters)
     * @param subtitle  Optional subtitle (location, type, capacity, etc.), or null
     * @param posterUrl Remote poster image URL if set (Cloudinary or similar), or null
     * @param status    Status of the event (e.g., "flagged")
     */
    public EventItem(String eventId, String title, String dateText, int posterRes, String subtitle, String posterUrl, String status) {
        this.eventId = eventId;
        this.title = title;
        this.dateText = dateText;
        this.posterRes = posterRes;
        this.subtitle = subtitle;
        this.posterUrl = posterUrl;
        this.status = status;
    }
}
