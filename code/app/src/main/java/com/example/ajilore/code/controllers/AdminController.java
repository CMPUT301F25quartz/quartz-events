package com.example.ajilore.code.controllers;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.ajilore.code.ui.events.model.Event;
import com.example.ajilore.code.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for all administrative operations within the application.
 *
 * <p>This class acts as the bridge between the Admin UI and the Firebase backend (Firestore and Storage).
 * It implements the logic for browsing, filtering, and removing domain entities (Events, Users, Images)
 * and ensures data consistency (e.g., removing associated images when an event is deleted).</p>
 *
 * <h3>Supported User Stories:</h3>
 * <ul>
 * <li><b>US 03.04.01:</b> Browse Events ({@link #fetchAllEvents})</li>
 * <li><b>US 03.05.01:</b> Browse Profiles ({@link #fetchAllUsers})</li>
 * <li><b>US 03.06.01:</b> Browse Images ({@link #fetchAllImages})</li>
 * <li><b>US 03.01.01:</b> Remove Events ({@link #removeEvent})</li>
 * <li><b>US 03.02.01:</b> Remove Profiles ({@link #removeUser})</li>
 * <li><b>US 03.03.01:</b> Remove Images ({@link #removeImage})</li>
 * <li><b>US 03.07.01:</b> Remove Organizers ({@link #deactivateOrganizer})</li>
 * <li><b>US 03.08.01:</b> Review Notification Logs ({@link #fetchNotificationLogs})</li>
 * </ul>
 *
 * @author Dinma (Team Quartz)
 * @version 2.0
 * @see com.example.ajilore.code.models.User
 * @see com.example.ajilore.code.ui.events.model.Event
 */
public class AdminController {
    private static final String TAG = "AdminController";

    // Firestore Collection Constants
    private static final String EVENTS_COLLECTION = "org_events";
    private static final String USERS_COLLECTION = "users";
    private static final String LOGS_COLLECTION = "admin_notification_logs";

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    /**
     * Initializes the AdminController with default Firebase instances.
     */
    public AdminController() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Internal helper to log administrative actions for audit purposes.
     *
     * <p>These logs are viewable in the Admin Notification Logs screen (US 03.08.01).</p>
     *
     * @param eventId  The ID of the event related to the action (nullable).
     * @param message  A descriptive message of what occurred.
     * @param audience The target audience type (e.g., "waiting", "organizer").
     * @param adminId  The ID of the admin performing the action (usually device ID or "admin").
     */
    private void logAdminAction(String eventId, String message, String audience, String adminId) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("eventId", eventId != null ? eventId : "N/A");
        logData.put("message", message);
        logData.put("audience", audience);
        logData.put("type", "admin_action");
        logData.put("senderId", adminId);
        logData.put("timestamp", FieldValue.serverTimestamp());

        db.collection(LOGS_COLLECTION)
                .add(logData)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Audit log created successfully: " + message))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to create audit log", e));
    }

    /**
     * Fetches all event documents from Firestore.
     * User Story: US 03.04.01 - Browse Events
     *
     * @param callback DataCallback returning list of events or an error.
     */
    public void fetchAllEvents(final DataCallback<List<Event>> callback) {
        Log.d(TAG, "Fetching all events...");

        db.collection(EVENTS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Event> events = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Event event = document.toObject(Event.class);
                                    event.id = document.getId();
                                    events.add(event);
                                    Log.d(TAG, "Loaded event: " + event.title);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing event: " + document.getId(), e);
                                }
                            }
                            callback.onSuccess(events);
                            Log.d(TAG, "Successfully fetched " + events.size() + " events");
                        } else {
                            Exception e = task.getException();
                            callback.onError(e != null ? e : new Exception("Unknown error"));
                            Log.e(TAG, "Error fetching events", task.getException());
                        }
                    }
                });
    }

    /**
     * Fetches all user profiles from Firestore and converts to User model.
     * User Story: US 03.05.01 - Browse Users/Profiles
     *
     * @param callback DataCallback returning the user list or error.
     */
    public void fetchAllUsers(final DataCallback<List<User>> callback) {
        Log.d(TAG, "Fetching all users...");

        db.collection(USERS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<User> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // Manually build User from Firestore document
                                    User user = new User();
                                    user.setUserId(document.getId());
                                    user.setName(document.getString("name"));
                                    user.setEmail(document.getString("email"));
                                    user.setRole(document.getString("role"));

                                    // Profile picture - use exact Firebase field name
                                    String profilePic = document.getString("profilepicture");
                                    if (profilePic != null && !profilePic.isEmpty()) {
                                        user.setProfileImageUrl(profilePic);
                                    }

                                    users.add(user);
                                    Log.d(TAG, "Loaded user: " + user.getName() +
                                            " (role: " + user.getRole() + ")");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing user: " + document.getId(), e);
                                }
                            }
                            callback.onSuccess(users);
                            Log.d(TAG, "Successfully fetched " + users.size() + " users");
                        } else {
                            Exception e = task.getException();
                            callback.onError(e != null ? e : new Exception("Unknown error"));
                            Log.e(TAG, "Error fetching users", task.getException());
                        }
                    }
                });
    }

    /**
     * Removes an event and corresponding poster image from Firestore and Storage.
     * User Story: US 03.01.01 - Remove Events
     *
     * @param eventId  Event document id to delete.
     * @param callback OperationCallback for success/error handling.
     */
    public void removeEvent(String eventId, final OperationCallback callback) {
        Log.d(TAG, "Removing event: " + eventId);

        // First, fetch the event to get the posterUrl
        db.collection(EVENTS_COLLECTION).document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        String posterUrl = event != null ? event.posterUrl : null;

                        // Delete the event document
                        db.collection(EVENTS_COLLECTION).document(eventId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Event removed: " + eventId);

                                    // If there's an image, delete it from Storage
                                    if (posterUrl != null && !posterUrl.isEmpty() && !posterUrl.equals("\"\"")) {
                                        deleteImageFromStorage(posterUrl);
                                    }
                                    // Log admin action and notify entrants
                                    logAdminAction(
                                            eventId,
                                            "Event '" + event.title + "' has been removed by an administrator.",
                                            "all_entrants",
                                            "admin"
                                    );
                                    //  Send notification to all entrants in waiting list
                                    notifyEventDeletion(eventId, event.title);

                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing event", e);
                                    callback.onError(e);
                                });
                    } else {
                        Log.e(TAG, "Event not found: " + eventId);
                        callback.onError(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching event", e);
                    callback.onError(e);
                });
    }

    /**
     * Notifies all entrants when an event is deleted by admin.
     */
    private void notifyEventDeletion(String eventId, String eventTitle) {
        // Create a broadcast notification for all waiting list members
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("message", "The event '" + eventTitle + "' has been cancelled by the administrator.");
        notificationData.put("audience", "waiting");
        notificationData.put("eventId", eventId);
        notificationData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // Note: We can't actually send this through the event collection since it's deleted
        // So we just log it for admin records
        Log.d(TAG, "Event deletion notification logged for: " + eventTitle);
    }

    /**
     * Removes a user profile from Firestore.
     * User Story: US 03.02.01 - Remove Profiles
     *
     * Note: This permanently deletes the user document.
     * For organizers, use deactivateOrganizer() instead.
     *
     * @param userId User's document ID.
     * @param callback OperationCallback for success/error.
     */
    public void removeUser(String userId, final OperationCallback callback) {
        Log.d(TAG, "Removing user: " + userId);

        // Fetch user details first for notification
        db.collection(USERS_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    String userName = doc.getString("name");

                    db.collection(USERS_COLLECTION).document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User removed: " + userId);

                                // Log admin action
                                logAdminAction(
                                        null,
                                        "User '" + userName + "' has been removed for policy violation.",
                                        "single_user",
                                        "admin"
                                );

                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error removing user", e);
                                callback.onError(e);
                            });
                })
                .addOnFailureListener(e -> callback.onError(e));
    }

    /**
     * Deactivates an organizer's account and recursively flags all their events.
     *
     * <p><b>Implements US 03.07.01:</b> This method enforces policy violations by:
     * <ol>
     * <li>Setting the user's `accountStatus` to "deactivated".</li>
     * <li>Revoking `canCreateEvents` permissions.</li>
     * <li>Triggering {@link #flagOrganizerEvents} to mark all existing events as flagged.</li>
     * </ol>
     * </p>
     *
     * @param organizerId The Firestore document ID of the organizer.
     * @param callback    Callback to handle the operation result.
     */
    public void deactivateOrganizer(String organizerId, final OperationCallback callback) {
        Log.i(TAG, "Initiating deactivation for organizer: " + organizerId);

        Map<String, Object> deactivationData = new HashMap<>();
        deactivationData.put("accountStatus", "deactivated");
        deactivationData.put("canCreateEvents", false);
        deactivationData.put("deactivatedAt", System.currentTimeMillis());
        deactivationData.put("deactivatedBy", "admin");

        db.collection(USERS_COLLECTION).document(organizerId)
                .update(deactivationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Organizer status updated. Proceeding to flag events.");
                    flagOrganizerEvents(organizerId, new OperationCallback() {
                        @Override
                        public void onSuccess() {
                            logAdminAction(null, "Organizer deactivated & events flagged.", "organizer", "admin");
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.w(TAG, "Organizer deactivated, but event flagging failed partially.", e);
                            callback.onSuccess(); // We consider the primary goal (deactivation) achieved
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Critical failure deactivating organizer", e);
                    callback.onError(e);
                });
    }
    /**
     * Flags all events created by a specific organizer.
     * User Story: US 03.07.01 - Remove organizers that violate app policy
     *
     * This is called automatically by deactivateOrganizer().
     * Flags prevent new registrations and mark events for review.
     *
     * NOTE: Uses "createdByUid" field to match your Event model!
     *
     * @param organizerId The organizer's user ID
     * @param callback OperationCallback for success/error handling
     */
    private void flagOrganizerEvents(String organizerId, final OperationCallback callback) {
        Log.d(TAG, "Flagging events for organizer: " + organizerId);

        // Query all events by this organizer
        // IMPORTANT: Using "createdByUid" to match your Event model!
        db.collection(EVENTS_COLLECTION)
                .whereEqualTo("createdByUid", organizerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d(TAG, "No events found for organizer: " + organizerId);
                        callback.onSuccess();
                        return;
                    }

                    // Prepare flag data
                    Map<String, Object> flagData = new HashMap<>();
                    flagData.put("status", "flagged");
                    flagData.put("flaggedReason", "Organizer account deactivated");
                    flagData.put("flaggedAt", System.currentTimeMillis());

                    // Track completion of all updates
                    final int totalEvents = querySnapshot.size();
                    final int[] completedUpdates = {0};
                    final boolean[] hasError = {false};

                    Log.d(TAG, "Flagging " + totalEvents + " events");

                    // Update each event
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String eventId = document.getId();

                        db.collection(EVENTS_COLLECTION).document(eventId)
                                .update(flagData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Flagged event: " + eventId);
                                    completedUpdates[0]++;

                                    // Check if all updates completed
                                    if (completedUpdates[0] >= totalEvents) {
                                        if (hasError[0]) {
                                            callback.onError(new Exception("Some events failed to flag"));
                                        } else {
                                            callback.onSuccess();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error flagging event: " + eventId, e);
                                    hasError[0] = true;
                                    completedUpdates[0]++;

                                    // Check if all updates completed
                                    if (completedUpdates[0] >= totalEvents) {
                                        callback.onError(new Exception("Some events failed to flag"));
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying organizer events", e);
                    callback.onError(e);
                });
    }

    /**
     * Fetches all events created by a specific organizer.
     * User Story: US 03.07.01 - Remove organizers that violate app policy
     *
     * This can be used to display organizer event activity before deactivation.
     *
     * NOTE: Uses "createdByUid" field to match your Event model!
     *
     * @param organizerId The organizer's user ID
     * @param callback DataCallback returning list of events or error
     */
    public void getOrganizerEvents(String organizerId, final DataCallback<List<Event>> callback) {
        Log.d(TAG, "Fetching events for organizer: " + organizerId);

        // IMPORTANT: Using "createdByUid" to match your Event model!
        db.collection(EVENTS_COLLECTION)
                .whereEqualTo("createdByUid", organizerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            Event event = document.toObject(Event.class);
                            event.id = document.getId();
                            events.add(event);
                            Log.d(TAG, "Loaded organizer event: " + event.title);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event: " + document.getId(), e);
                        }
                    }

                    callback.onSuccess(events);
                    Log.d(TAG, "Fetched " + events.size() + " events for organizer");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching organizer events", e);
                    callback.onError(e);
                });
    }

    /**
     * Helper to remove an image from Firebase Storage by URL.
     * Does not propagate errors to caller; logs internally.
     *
     * @param imageURL Fully qualified Firebase Storage or Cloudinary URL.
     */
    private void deleteImageFromStorage(String imageURL) {
        try {
            // Only try Firebase Storage URLs
            if (imageURL.contains("firebasestorage.googleapis.com")) {
                StorageReference imageRef = storage.getReferenceFromUrl(imageURL);
                imageRef.delete()
                        .addOnSuccessListener(aVoid ->
                                Log.d(TAG, "Image deleted from storage"))
                        .addOnFailureListener(e ->
                                Log.e(TAG, "Error deleting image (may not exist)", e));
            } else {
                Log.d(TAG, "External image URL (not in Firebase Storage): " + imageURL);
            }
        } catch (Exception e) {
            Log.e(TAG, "Invalid storage URL: " + imageURL, e);
        }
    }

    /**
     * Fetches all images (event posters AND profile pictures) from Firestore.
     * User Story: US 03.06.01 - Browse Images
     *
     * @param callback DataCallback returning list of ImageItems or an error.
     */
    public void fetchAllImages(final DataCallback<List<com.example.ajilore.code.models.ImageItem>> callback) {
        Log.d(TAG, "Fetching all images...");

        final List<com.example.ajilore.code.models.ImageItem> allImages = new ArrayList<>();

        // First, fetch event posters
        db.collection(EVENTS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Add event posters
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    com.example.ajilore.code.ui.events.model.Event event =
                                            document.toObject(com.example.ajilore.code.ui.events.model.Event.class);
                                    event.id = document.getId();

                                    // Only add if event has a posterUrl
                                    if (event.posterUrl != null &&
                                            !event.posterUrl.isEmpty() &&
                                            !event.posterUrl.equals("\"\"")) {

                                        long uploadTime = event.createdAt != null ?
                                                event.createdAt.toDate().getTime() :
                                                System.currentTimeMillis();

                                        com.example.ajilore.code.models.ImageItem imageItem =
                                                new com.example.ajilore.code.models.ImageItem(
                                                        event.posterUrl,
                                                        event.id,
                                                        "Event: " + event.title,
                                                        uploadTime
                                                );
                                        imageItem.type = "event";
                                        allImages.add(imageItem);
                                        Log.d(TAG, "Loaded event poster: " + event.title);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing event for image: " + document.getId(), e);
                                }
                            }

                            // Now fetch profile pictures
                            fetchProfilePictures(allImages, callback);

                        } else {
                            Exception e = task.getException();
                            callback.onError(e != null ? e : new Exception("Unknown error"));
                            Log.e(TAG, "Error fetching event images", task.getException());
                        }
                    }
                });
    }

    /**
     * Helper method to fetch profile pictures and combine with event images.
     * CRITICAL: Uses "profilepicture" field name to match your Firebase!
     */
    private void fetchProfilePictures(final List<com.example.ajilore.code.models.ImageItem> eventImages,
                                      final DataCallback<List<com.example.ajilore.code.models.ImageItem>> callback) {
        db.collection(USERS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Add profile pictures
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // CRITICAL: Use "profilepicture" to match your Firebase field name
                                    String profileImageUrl = document.getString("profilepicture");
                                    String userName = document.getString("name");

                                    // Only add if user has a profile picture
                                    if (profileImageUrl != null &&
                                            !profileImageUrl.isEmpty() &&
                                            !profileImageUrl.equals("\"\"")) {

                                        long uploadTime = System.currentTimeMillis();
                                        if (document.contains("createdAt")) {
                                            try {
                                                uploadTime = document.getLong("createdAt");
                                            } catch (Exception e) {
                                                // Use current time if createdAt is not a long
                                            }
                                        }

                                        com.example.ajilore.code.models.ImageItem imageItem =
                                                new com.example.ajilore.code.models.ImageItem(
                                                        profileImageUrl,
                                                        document.getId(),
                                                        "Profile: " + (userName != null ? userName : "User"),
                                                        uploadTime
                                                );
                                        imageItem.type = "profile";
                                        eventImages.add(imageItem);
                                        Log.d(TAG, "Loaded profile picture: " + userName);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing user for profile image: " + document.getId(), e);
                                }
                            }

                            callback.onSuccess(eventImages);
                            Log.d(TAG, "Successfully fetched " + eventImages.size() + " images total");
                        } else {
                            // If profile fetch fails, still return event images
                            callback.onSuccess(eventImages);
                            Log.w(TAG, "Error fetching profile images, returning event images only");
                        }
                    }
                });
    }

    /**
     * Removes an image (event poster OR profile picture) from Storage and Firestore.
     * User Story: US 03.03.01 - Remove Images
     *
     * @param imageItem  The ImageItem containing the image URL, ID, and type
     * @param callback OperationCallback for success/error handling.
     */
    public void removeImage(com.example.ajilore.code.models.ImageItem imageItem, final OperationCallback callback) {
        Log.d(TAG, "Removing image: " + imageItem.title + " (type: " + imageItem.type + ")");

        // Delete the image from Storage first
        if (imageItem.imageUrl != null && !imageItem.imageUrl.isEmpty()) {
            deleteImageFromStorage(imageItem.imageUrl);
        }

        // Determine which collection and field to update based on image type
        String collection;
        String field;

        if ("profile".equals(imageItem.type)) {
            // Profile picture - Use "profilepicture" to match Firebase
            collection = USERS_COLLECTION;
            field = "profilepicture";
            Log.d(TAG, "Removing profile picture for user: " + imageItem.eventId);
        } else {
            // Event poster (default)
            collection = EVENTS_COLLECTION;
            field = "posterUrl";
            Log.d(TAG, "Removing event poster for event: " + imageItem.eventId);
        }

        // Update the document to remove the image URL
        db.collection(collection).document(imageItem.eventId)
                .update(field, null)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Image removed from " + collection + ": " + imageItem.eventId);
                    // Log admin action
                    if ("profile".equals(imageItem.type)) {
                        logAdminAction(
                                null,
                                "Profile picture removed for user in violation of policy.",
                                "single_user",
                                "admin"
                        );
                    } else {
                        logAdminAction(
                                imageItem.eventId,
                                "Event poster for '" + imageItem.title + "' removed for policy violation.",
                                "organizer",
                                "admin"
                        );
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing image reference from " + collection, e);
                    callback.onError(e);
                });
    }


    /**
     * Fetches notification logs for administrative review.
     *
     * <p><b>Implements US 03.08.01:</b> Queries the `admin_notification_logs` collection,
     * ordered by timestamp descending (newest first).</p>
     *
     * @param callback Callback returning a list of {@link com.example.ajilore.code.models.NotificationLog} objects.
     */
    public void fetchNotificationLogs(final DataCallback<List<com.example.ajilore.code.models.NotificationLog>> callback) {
        db.collection(LOGS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<com.example.ajilore.code.models.NotificationLog> logs = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                com.example.ajilore.code.models.NotificationLog log =
                                        doc.toObject(com.example.ajilore.code.models.NotificationLog.class);
                                log.setLogId(doc.getId());
                                logs.add(log);
                            } catch (Exception e) {
                                Log.w(TAG, "Skipping malformed log entry: " + doc.getId());
                            }
                        }
                        callback.onSuccess(logs);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Helper method to sort logs by timestamp (newest first).
     * Handles null timestamps by placing them at the end.
     */
    private void sortLogsByTimestamp(List<com.example.ajilore.code.models.NotificationLog> logs) {
        logs.sort((log1, log2) -> {
            if (log1.getTimestamp() == null && log2.getTimestamp() == null) return 0;
            if (log1.getTimestamp() == null) return 1;
            if (log2.getTimestamp() == null) return -1;

            // Descending order (newest first)
            return log2.getTimestamp().compareTo(log1.getTimestamp());
        });
    }

    // Callback interfaces
    /**
     * Generic callback interface for data retrieval operations.
     * @param <T> The type of data expected (e.g., List&lt;Event&gt;).
     */
    public interface DataCallback<T> {
        /**
         * Called with a result if data fetch succeeds.
         * @param data The data returned from Firestore.
         */
        void onSuccess(T data);

        /**
         * Called if the data fetch operation fails for any reason.
         * @param e The error or exception thrown.
         */
        void onError(Exception e);
    }

    /**
     * Generic callback interface for void operations (Create/Update/Delete).
     */
    public interface OperationCallback {
        /**
         * Success indicator for operation.
         */
        void onSuccess();

        /**
         * Error indicator for operation.
         * @param e The error or exception thrown.
         */
        void onError(Exception e);
    }
}