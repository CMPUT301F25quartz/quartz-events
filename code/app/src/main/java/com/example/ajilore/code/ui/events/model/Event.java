package com.example.ajilore.code.ui.events.model;
import com.google.firebase.Timestamp;
import java.util.Date;

/*
*
* */
public class Event {

    public String   id;            // Firestore doc id (set after read)
    public String   title;
    public String   type;          // “Workshop”, “Concert”, …
    public String   location;         // location
    public int      capacity;
    public Timestamp startsAt;
    public Timestamp regOpens;
    public Timestamp regCloses;
    public String   posterKey;     // optional: key to Storage path later
    public String   status;        // “draft” | “published”
    public String   createdByUid;  // fill once Auth is in
    public Timestamp createdAt;    // serverTimestamp() on write





}
