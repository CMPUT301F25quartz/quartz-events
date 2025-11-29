package com.example.ajilore.code.ui.events.list;

import java.util.Date;

/**
 * Model representing a single event row in the events feed, used by adapters for display.
 * Includes additional fields needed for filtering functionality (US 01.01.04)
 */
public class EventRow {

    /** Firestore document ID for the event. */
    public final String id;

    /** Main event title for display. */
    public final String title;

    /** Location string for the event. */
    public final String location;

    /** Formatted date string (pre-formatted for view). */
    public final String dateText;

    /** Status label (e.g., "Open", "Closed"). */
    public final String status;

    /** Drawable resource ID for poster thumbnail. */
    public final int posterRes; //Drawable resource id

    /** Poster Cloudinary URL (if image is remote), or null. */
    public final String posterUrl; //Cloudinary url

    // ========== Additional fields for filtering (US 01.01.04) ==========

    /** Event category for category filtering (e.g., "Music & Concerts", "Sports") */
    public String category;

    /** Event start date/time for date range filtering */
    public Date startsAt;

    /** Registration open date for availability filtering */
    public Date regOpens;

    /** Registration close date for availability filtering */
    public Date regCloses;

    /**
     * Constructs a new immutable event row for display in the feed.
     *
     * @param id         Firestore doc ID of the event.
     * @param title      Event title.
     * @param location   Event location.
     * @param dateText   Pre-formatted date and/or time.
     * @param posterRes  Drawable res ID for thumbnail; used as fallback.
     * @param posterUrl  Remote URL to image (Cloudinary or similar); may be null.
     * @param status     String status to show in UI chips, e.g., "Open" or "Closed".
     */
    public EventRow(String id, String title, String location, String dateText,
                    int posterRes, String posterUrl, String status) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.dateText = dateText;
        this.status = status;
        this.posterRes = posterRes;
        this.posterUrl = posterUrl;
    }
}