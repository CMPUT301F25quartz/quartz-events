package com.example.ajilore.code.models;

/**
 * Event model class representing an event in the lottery system.
 * Matches Firebase Firestore schema with fields:
 * OrganiserDeviceId, capacity, date, description, imageURL, location, price, time, title
 */
public class Event {
    private String eventId;              // Document ID from Firestore
    private String OrganiserDeviceId;    // Matches Firebase field exactly
    private int capacity;
    private String date;                 // e.g., "Sunday 13 November"
    private String description;
    private String imageURL;             // Note: matches Firebase casing
    private String location;
    private int price;
    private String time;                 // e.g., "6:00 PM"
    private String title;

    // Empty constructor required for Firestore
    public Event() {}

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
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOrganiserDeviceId() { return OrganiserDeviceId; }
    public void setOrganiserDeviceId(String organiserDeviceId) {
        this.OrganiserDeviceId = organiserDeviceId;
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTitle() { return title; }
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