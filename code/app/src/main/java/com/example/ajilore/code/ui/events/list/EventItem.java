package com.example.ajilore.code.ui.events.list;

/**
 * Lightweight UI model for one organizer event row.
 */
public class EventItem {
    public final String eventId, title, dateText, subtitle;
    public final int posterRes;
    public final String posterUrl;



    /**
     * @param eventId   Firestore document id
     * @param title     event title to display
     * @param dateText  human-readable date/time
     * @param posterRes drawable resource for the list image
     */


    public EventItem(String eventId, String title, String dateText, int posterRes, String subtitle, String posterUrl) {
        this.eventId = eventId;
        this.title = title;
        this.dateText = dateText;
        this.posterRes = posterRes;
        this.subtitle = subtitle;
        this.posterUrl = posterUrl;
    }
}
