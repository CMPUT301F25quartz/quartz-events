package com.example.ajilore.code.models;

/**
 * Model class representing an image item for admin browsing.
 *
 * <p>This class encapsulates information about uploaded images including
 * event posters and profile pictures. Used in the admin interface to
 * display, filter, and manage images.</p>
 *
 * <p>User Story: US 03.06.01 - Browse images</p>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class ImageItem {

    /** The URL of the image */
    public String imageUrl;

    /** The ID of the associated event (if event poster) */
    public String eventId;

    /** The title/name of the associated event or user */
    public String title;

    /** The type of image: "event" or "profile" */
    public String type;

    /** Upload timestamp in milliseconds */
    public long uploadedAt;

    /**
     * Empty constructor required for Firestore
     */
    public ImageItem() {
    }

    /**
     * Constructor for event poster images
     *
     * @param imageUrl URL of the image
     * @param eventId ID of the associated event
     * @param title Title of the event
     * @param uploadedAt Upload timestamp
     */
    public ImageItem(String imageUrl, String eventId, String title, long uploadedAt) {
        this.imageUrl = imageUrl;
        this.eventId = eventId;
        this.title = title;
        this.type = "event";
        this.uploadedAt = uploadedAt;
    }
}