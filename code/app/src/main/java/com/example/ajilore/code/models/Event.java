package com.example.ajilore.code.models;

/**
 * Event model class representing an event in the lottery system.
 * Matches Firebase Firestore schema with fields:
 * OrganiserDeviceId, capacity, date, description, imageURL, location, price, time, title
 */
public class Event {

    /** Firestore document ID for the event. */
    private String eventId;

    /** Device ID of the organiser, maps to Firestore's field "OrganiserDeviceId". */
    private String OrganiserDeviceId;

    /** Maximum number of attendees. */
    private int capacity;

    /** Event date string (e.g., "Sunday 13 November"). */
    private String date;                 // e.g., "Sunday 13 November"

    /** Event description. */
    private String description;

    /** URL or path to the event's promotional image, follows Firestore "imageURL". */
    private String imageURL;             // Note: matches Firebase casing

    /** Event physical or virtual location string. */
    private String location;

    /** Price as an integer (e.g., cents or dollars). */
    private int price;

    /** Time string for the event (e.g., "6:00 PM"). */
    private String time;                 // e.g., "6:00 PM"

    /** Title of the event. */
    private String title;

    /**
     * Empty constructor required for Firebase/Firestore deserialization.
     */
    public Event() {}

    /**
     * Constructs an event model with all data fields.
     *
     * @param eventId           Firestore document ID
     * @param organiserDeviceId Device ID of organiser (field name case matters)
     * @param capacity          Maximum capacity
     * @param date              String date for display
     * @param description       Event description
     * @param imageURL          URL/path to poster image
     * @param location          Event location
     * @param price             Event price
     * @param time              Time string (display)
     * @param title             Display and search title
     */
    public Event(String eventId, String organiserDeviceId, int capacity, String date,
                 String description, String imageURL, String location, int price,
                 String time, String title) {
        this.eventId = eventId;
        this.OrganiserDeviceId = organiserDeviceId;
        this.capacity = capacity;
        this.date = date;
        this.description = description;
        this.imageURL = imageURL;
        this.location = location;
        this.price = price;
        this.time = time;
        this.title = title;
    }

    // Getters and Setters - IMPORTANT: Match Firebase field names exactly
    /** @return Firestore doc ID for this event */
    public String getEventId() { return eventId; }

    /** Sets the Firestore document ID. @param eventId Firestore doc id. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return Organiser's device (user) ID. */
    public String getOrganiserDeviceId() { return OrganiserDeviceId; }

    /** Sets the organiser's device ID for this event. @param organiserDeviceId Device/user id (case matches backend). */
    public void setOrganiserDeviceId(String organiserDeviceId) {
        this.OrganiserDeviceId = organiserDeviceId;
    }

    /** @return Event maximum capacity. */
    public int getCapacity() { return capacity; }

    /** Sets event capacity. @param capacity Max entrants for event. */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /** @return Event date string. */
    public String getDate() { return date; }

    /** Sets the event date. @param date Display date string. */
    public void setDate(String date) { this.date = date; }

    /** @return Event description. */
    public String getDescription() { return description; }

    /** Sets the event description. @param description Long-form event details. */
    public void setDescription(String description) { this.description = description; }

    /** @return URL or path for main poster image. */
    public String getImageURL() { return imageURL; }

    /** Sets the event's promotional image URL. @param imageURL Remote path or URL. */
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    /** @return Event location string. */
    public String getLocation() { return location; }

    /** Sets event location. @param location Display location string. */
    public void setLocation(String location) { this.location = location; }

    /** @return Event price as integer. */
    public int getPrice() { return price; }

    /** Sets the event price. @param price Display price/cost as int. */
    public void setPrice(int price) { this.price = price; }

    /** @return Time string for event start. */
    public String getTime() { return time; }

    /** Sets the event time. @param time Display time string. */
    public void setTime(String time) { this.time = time; }

    /** @return Event title to show. */
    public String getTitle() { return title; }

    /** Sets the event's title. @param title Main title string. */
    public void setTitle(String title) { this.title = title; }

    /**
     * Helper method to get formatted date and time string
     * @return Formatted string like "Sunday 13 November - 6:00 PM"
     */
    public String getFormattedDateTime() {
        if (date != null && time != null) {
            return date + " - " + time;
        } else if (date != null) {
            return date;
        } else if (time != null) {
            return time;
        }
        return "Date TBA";
    }
}