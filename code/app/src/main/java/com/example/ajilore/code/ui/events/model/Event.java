package com.example.ajilore.code.ui.events.model;
import com.google.firebase.Timestamp;
import java.util.Date;

/**
 * Class Event
* Data model for an organizer-created event
 * @author ajilore
 * @version 1.0
 * @since 2025-11-01
*/
public class Event {

    /**
    * This is the firestore doc id
    * */
    public String   id;

    /**
     * This is the title of the event
     */
    public String   title;

    /**
     * This is the type/category of the event e.g Concert, Workshop etc
     */
    public String   type;

    /**
    * Venue or location text (optional)
    * */
    public String   location;

    /**
     * Maximum number of entrants; 0 or more.
     * */
    public int      capacity;

    /** When the event starts (date {@literal &} time). */
    public Timestamp startsAt;

    /** When registration opens (date only or date {@literal &} time). */
    public Timestamp regOpens;

    /** When registration closes (date only or date {@literal &} time). */
    public Timestamp regCloses;

    /**
     * Optional key that identifies a poster image.
     * Could be a Storage path, filename, or logical key used by the app.
     */
    public String   posterKey;     // optional: key to Storage path later

    /** Publication state; commonly "draft" or "published". */
    public String   status;        // “draft” | “published”

    /** UID of the organizer who created the event (filled once Auth is added). */

    public String   createdByUid;  // fill once Auth is in

    /** Server timestamp set at creation time in Firestore. */
    public Timestamp createdAt;    // serverTimestamp() on write





}
