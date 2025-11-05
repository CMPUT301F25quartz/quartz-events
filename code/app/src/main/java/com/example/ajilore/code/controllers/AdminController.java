package com.example.ajilore.code.controllers;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.ajilore.code.models.Event;
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

    public AdminController() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Fetches all events from Firestore.
     * US 03.04.01: Browse Events
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
                                    event.setEventId(document.getId());
                                    events.add(event);
                                    Log.d(TAG, "Loaded event: " + event.getTitle());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing event: " + document.getId(), e);
                                }
                            }
                            callback.onSuccess(events);
                            Log.d(TAG, "✅ Successfully fetched " + events.size() + " events");
                        } else {
                            Exception e = task.getException();
                            callback.onError(e != null ? e : new Exception("Unknown error"));
                            Log.e(TAG, "❌ Error fetching events", task.getException());
                        }
                    }
                });
    }

    /**
     * Fetches all users from Firestore.
     * US 03.05.01: Browse Profiles/Users
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
                            Log.d(TAG, "✅ Successfully fetched " + users.size() + " users");
                        } else {
                            Exception e = task.getException();
                            callback.onError(e != null ? e : new Exception("Unknown error"));
                            Log.e(TAG, "❌ Error fetching users", task.getException());
                        }
                    }
                });
    }

    /**
     * Removes an event from Firestore and its associated image from Storage.
     * US 03.01.01: Remove Events
     */
    public void removeEvent(String eventId, final OperationCallback callback) {
        Log.d(TAG, "Removing event: " + eventId);

        // First, fetch the event to get the imageURL
        db.collection(EVENTS_COLLECTION).document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        String imageURL = event != null ? event.getImageURL() : null;

                        // Delete the event document
                        db.collection(EVENTS_COLLECTION).document(eventId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✅ Event removed: " + eventId);

                                    // If there's an image, delete it from Storage
                                    if (imageURL != null && !imageURL.isEmpty() && !imageURL.equals("\"\"")) {
                                        deleteImageFromStorage(imageURL);
                                    }

                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Error removing event", e);
                                    callback.onError(e);
                                });
                    } else {
                        Log.e(TAG, "Event not found: " + eventId);
                        callback.onError(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error fetching event", e);
                    callback.onError(e);
                });
    }

    /**
     * Removes a user from Firestore.
     * Note: You may want to add additional logic here to handle:
     * - Removing user's events
     * - Removing user from waiting lists
     * - Cleaning up related data
     */
    public void removeUser(String userId, final OperationCallback callback) {
        Log.d(TAG, "Removing user: " + userId);

        db.collection(USERS_COLLECTION).document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User removed: " + userId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error removing user", e);
                    callback.onError(e);
                });
    }

    /**
     * Helper method to delete an image from Firebase Storage.
     */
    private void deleteImageFromStorage(String imageURL) {
        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageURL);
            imageRef.delete()
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "✅ Image deleted from storage"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "⚠️ Error deleting image (may not exist)", e));
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Invalid storage URL: " + imageURL, e);
        }
    }

    // Callback interfaces
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }
}