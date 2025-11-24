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
                                    event.id = document.getId();  // Use public 'id' field
                                    events.add(event);
                                    Log.d(TAG, "Loaded event: " + event.title);  // Use public 'title' field
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing event: " + document.getId(), e);
                                }
                            }
                            callback.onSuccess(events);
                            Log.d(TAG, "Successfully fetched " + events.size() + " events");
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
                                    users.add(user);
                                    Log.d(TAG, "Loaded user: " + user.getName());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing user: " + document.getId(), e);
                                }
                            }
                            callback.onSuccess(users);
                            Log.d(TAG, " Successfully fetched " + users.size() + " users");
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

        // First, fetch the event to get the imageURL
        db.collection(EVENTS_COLLECTION).document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        String posterUrl = event != null ? event.posterUrl : null;  // Use posterUrl field

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
                    Log.e(TAG, "Error removing user", e);
                    callback.onError(e);
                });
    }

    /**
     * Helper to remove an image from Firebase Storage by URL.
     * Does not propagate errors to caller; logs internally.
     *
     * @param imageURL Fully qualified Firebase Storage URL for image.
     */
    private void deleteImageFromStorage(String imageURL) {
        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageURL);
            imageRef.delete()
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "Image deleted from storage"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "⚠ Error deleting image (may not exist)", e));
        } catch (Exception e) {
            Log.e(TAG, "⚠ Invalid storage URL: " + imageURL, e);
        }
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