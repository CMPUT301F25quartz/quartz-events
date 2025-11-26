package com.example.ajilore.code.controllers;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.ajilore.code.ui.events.model.Event;
import com.example.ajilore.code.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminController handles all administrative operations.
 * Provides methods for browsing and removing events, users, and images.
 * Implements US 03.04.01 (Browse Events), US 03.05.01 (Browse Users),
 * and US 03.01.01 (Remove Events).
 */
public class AdminController {
    private static final String TAG = "AdminController";
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Collection names matching your Firebase
    private static final String EVENTS_COLLECTION = "org_events";
    private static final String USERS_COLLECTION = "users";
    private static final String IMAGES_COLLECTION = "images";

    /**
     * Constructs an AdminController and connects to Firestore and Storage.
     */
    public AdminController() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Fetches all event documents from Firestore.
     * <p>User Story:</p>
     * <ul><li>US 03.04.01 - Browse Events</li></ul>
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
                            Log.d(TAG, " Successfully fetched " + events.size() + " events");
                        } else {
                            Exception e = task.getException();
                            callback.onError(e != null ? e : new Exception("Unknown error"));
                            Log.e(TAG, " Error fetching events", task.getException());
                        }
                    }
                });
    }

    /**
     * Fetches all user profiles from Firestore.
     * <p>User Story:</p>
     * <ul><li>US 03.05.01 - Browse Users/Profiles</li></ul>
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
                                    User user = document.toObject(User.class);
                                    user.setUserId(document.getId());

                                    // MANUAL FIX: Map profilepicture → profileImageUrl
                                    String profilePic = document.getString("profilepicture");
                                    if (profilePic != null && !profilePic.isEmpty()) {
                                        user.setProfileImageUrl(profilePic);
                                        Log.d(TAG, "Loaded profile pic for: " + user.getName());
                                    }

                                    users.add(user);
                                    Log.d(TAG, "Loaded user: " + user.getName());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing user: " + document.getId(), e);
                                }
                            }
                            callback.onSuccess(users);
                            Log.d(TAG, "Successfully fetched " + users.size() + " users");
                        } else {
                            Exception e = task.getException();
                            callback.onError(e != null ? e : new Exception("Unknown error"));
                            Log.e(TAG, " Error fetching users", task.getException());
                        }
                    }
                });
    }

    /**
     * Removes an event and corresponding poster image from Firestore and Storage.
     * <p>User Story:</p>
     * <ul><li>US 03.01.01 - Remove Events</li></ul>
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
                                    Log.d(TAG, " Event removed: " + eventId);

                                    // If there's an image, delete it from Storage
                                    if (posterUrl != null && !posterUrl.isEmpty() && !posterUrl.equals("\"\"")) {
                                        deleteImageFromStorage(posterUrl);
                                    }

                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, " Error removing event", e);
                                    callback.onError(e);
                                });
                    } else {
                        Log.e(TAG, "Event not found: " + eventId);
                        callback.onError(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Error fetching event", e);
                    callback.onError(e);
                });
    }

    /**
     * Removes a user profile from Firestore.
     * Extend this method to remove user's events, waiting list entries, or extra cleanup as needed.
     *
     * @param userId User's document ID.
     * @param callback OperationCallback for success/error.
     */
    public void removeUser(String userId, final OperationCallback callback) {
        Log.d(TAG, "Removing user: " + userId);

        db.collection(USERS_COLLECTION).document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, " User removed: " + userId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Error removing user", e);
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
                                Log.e(TAG, "⚠ Error deleting image (may not exist)", e));
            } else {
                Log.d(TAG, "⚠ External image URL (not in Firebase Storage): " + imageURL);
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠ Invalid storage URL: " + imageURL, e);
        }
    }

    /**
     * Fetches all images (event posters AND profile pictures) from Firestore.
     * <p>User Story:</p>
     * <ul><li>US 03.06.01 - Browse Images</li></ul>
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
                                    // CRITICAL: Use "profilepicture" to match your Firebase field name!
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
                            Log.d(TAG, " Successfully fetched " + eventImages.size() + " images total");
                        } else {
                            // If profile fetch fails, still return event images
                            callback.onSuccess(eventImages);
                            Log.w(TAG, "⚠ Error fetching profile images, returning event images only");
                        }
                    }
                });
    }

    /**
     * Removes an image (event poster OR profile picture) from Storage and Firestore.
     * <p>User Story:</p>
     * <ul><li>US 03.03.01 - Remove Images</li></ul>
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
            // Profile picture - Use "profilepicture" to match Firebase!
            collection = USERS_COLLECTION;
            field = "profilepicture";  // ← Matches your Firebase field name!
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
                    Log.d(TAG, " Image removed from " + collection + ": " + imageItem.eventId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Error removing image reference from " + collection, e);
                    callback.onError(e);
                });
    }

    // Callback interfaces
    /**
     * Generic data fetch callback with type and error reporting.
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
     * Generic operation callback for CRUD/remove operations.
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