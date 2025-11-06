package com.example.ajilore.code.ui.events.list;

/**
 * Model for one row in the events feed
 */
public class EventRow {
    public final String id;
    public final String title;

    public final String location;
    public final String dateText;
    public final String status;
    public final int posterRes; //Drawable resource id
    public final String posterUrl; //Cloudinary url

    public EventRow(String id, String title, String location, String dateText, int posterRes, String posterUrl, String status){
        this.id = id;
        this.title = title;
        this.location = location;
        this.dateText = dateText;
        this.status = status;
        this.posterRes = posterRes;
        this.posterUrl = posterUrl;
    }




}
