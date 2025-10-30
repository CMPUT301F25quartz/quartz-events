package com.example.ajilore.code.ui.events;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralizes organizer notifications:
 *  - writes an audit under org_events/{eventId}/broadcasts
 *  - fans out messages to entrants under org_events/{eventId}/entrants/{uid}/inbox
 */
public final class EventNotifier {
    private EventNotifier() {}

    public interface Callback {
        void onSuccess(int deliveredCount, @NonNull String broadcastIdOrEmpty);
        void onError(@NonNull Exception e);
    }

    /** Broadcast to a whole audience by status (e.g., "chosen", "selected", "waiting", "cancelled"). */
    public static void notifyAudience(@NonNull FirebaseFirestore db,
                                      @NonNull String eventId,
                                      @NonNull String eventTitle,
                                      @NonNull String message,
                                      boolean includePoster,
                                      @Nullable String linkUrl,
                                      @NonNull String targetStatus,   // "chosen" | "selected" | "waiting" | "cancelled"
                                      @NonNull Callback cb) {

        // 0) Write a broadcast audit record
        Map<String, Object> payload = new HashMap<>();
        payload.put("audience", targetStatus);
        payload.put("message", message);
        payload.put("includePoster", includePoster);
        payload.put("linkUrl", linkUrl);
        payload.put("createdAt", FieldValue.serverTimestamp());
        payload.put("eventId", eventId);

        db.collection("org_events").document(eventId)
                .collection("broadcasts")
                .add(payload)
                .addOnSuccessListener(bRef -> {
                    final String broadcastId = bRef.getId();

                    // 1) Load all entrants for this event (small demo scale → simple)
                    db.collection("org_events").document(eventId)
                            .collection("entrants")
                            .get()
                            .addOnSuccessListener((QuerySnapshot snaps) -> {
                                if (snaps == null || snaps.isEmpty()) {
                                    cb.onSuccess(0, broadcastId);
                                    return;
                                }

                                List<String> uids = new ArrayList<>();
                                for (QueryDocumentSnapshot d : snaps) {
                                    String status = d.getString("status");        // waiting | chosen | selected | cancelled
                                    String responded = d.getString("responded");  // pending | accepted | declined | null

                                    boolean include = false;
                                    switch (targetStatus) {
                                        case "waiting":
                                            include = "waiting".equals(status);
                                            break;

                                        case "chosen":
                                            // Invite message to people chosen but not yet accepted/declined
                                            include = "chosen".equals(status)
                                                    && !"accepted".equals(responded)
                                                    && !"declined".equals(responded);
                                            break;

                                        case "selected":
                                            // Your schema today: chosen + accepted
                                            include = ("chosen".equals(status) && "accepted".equals(responded))
                                                    // Future-proof if you later flip status
                                                    || "selected".equals(status);
                                            break;

                                        case "cancelled":
                                            // Your schema today: chosen + declined
                                            include = ("chosen".equals(status) && "declined".equals(responded))
                                                    // Future-proof if you later flip status
                                                    || "cancelled".equals(status);
                                            break;
                                    }

                                    if (include) uids.add(d.getId());
                                }

                                if (uids.isEmpty()) {
                                    cb.onSuccess(0, broadcastId);
                                    return;
                                }

                                // 2) Fan out to each recipient’s inbox (under org_events/{eventId}/entrants/{uid}/inbox)
                                writeInboxInChunks(db, uids, eventId, eventTitle, message,
                                        includePoster, linkUrl, targetStatus, broadcastId, cb);
                            })
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }


    /** Send to a single entrant (used for accept/decline follow-ups). */
    public static void notifySingle(@NonNull FirebaseFirestore db,
                                    @NonNull String eventId,
                                    @NonNull String eventTitle,
                                    @NonNull String uid,
                                    @NonNull String targetStatus,   // "selected" or "cancelled" (or others)
                                    @NonNull String message,
                                    boolean includePoster,
                                    @Nullable String linkUrl,
                                    @NonNull Callback cb) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("audience", targetStatus);
        payload.put("message", message);
        payload.put("includePoster", includePoster);
        payload.put("linkUrl", linkUrl);
        payload.put("createdAt", FieldValue.serverTimestamp());
        payload.put("eventId", eventId);

        db.collection("org_events").document(eventId)
                .collection("broadcasts")
                .add(payload)
                .addOnSuccessListener(bRef -> {
                    String broadcastId = bRef.getId();

                    DocumentReference inboxRef = db.collection("org_events").document(eventId)
                            .collection("entrants").document(uid)
                            .collection("inbox").document();

                    Map<String, Object> inbox = new HashMap<>();
                    inbox.put("type", targetStatus.equals("selected") ? "selected_notice"
                            : targetStatus.equals("cancelled") ? "cancelled_notice" : "broadcast");
                    inbox.put("audience", targetStatus);
                    inbox.put("eventId", eventId);
                    inbox.put("eventTitle", eventTitle);
                    inbox.put("message", message);
                    inbox.put("includePoster", includePoster);
                    inbox.put("linkUrl", linkUrl);
                    inbox.put("read", false);
                    inbox.put("broadcastId", broadcastId);
                    inbox.put("createdAt", FieldValue.serverTimestamp());

                    inboxRef.set(inbox)
                            .addOnSuccessListener(v -> cb.onSuccess(1, broadcastId))
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }

    // Internal batching helper
// collects per-batch sizes so delivered count is accurate
    private static void writeInboxInChunks(@NonNull FirebaseFirestore db,
                                           @NonNull List<String> uids,
                                           @NonNull String eventId,
                                           @NonNull String eventTitle,
                                           @NonNull String message,
                                           boolean includePoster,
                                           @Nullable String linkUrl,
                                           @NonNull String targetStatus,
                                           @NonNull String broadcastId,
                                           @NonNull Callback cb) {
        final int LIMIT = 450;

        List<WriteBatch> batches = new ArrayList<>();
        List<Integer> batchSizes = new ArrayList<>();

        int from = 0;
        while (from < uids.size()) {
            int to = Math.min(from + LIMIT, uids.size());
            List<String> chunk = uids.subList(from, to);

            WriteBatch batch = db.batch();
            int thisBatchCount = 0;

            for (String uid : chunk) {
                DocumentReference inboxRef = db.collection("org_events").document(eventId)
                        .collection("entrants").document(uid)
                        .collection("inbox").document();

                Map<String, Object> inbox = new HashMap<>();
                inbox.put("type",
                        targetStatus.equals("chosen")    ? "invite" :
                                targetStatus.equals("selected")  ? "selected_notice" :
                                        targetStatus.equals("cancelled") ? "cancelled_notice" : "broadcast");
                inbox.put("audience", targetStatus);
                inbox.put("eventId", eventId);
                inbox.put("eventTitle", eventTitle);
                inbox.put("message", message);
                inbox.put("includePoster", includePoster);
                inbox.put("linkUrl", linkUrl);
                inbox.put("read", false);
                inbox.put("broadcastId", broadcastId);
                inbox.put("createdAt", FieldValue.serverTimestamp());

                batch.set(inboxRef, inbox);
                thisBatchCount++;
            }

            batches.add(batch);
            batchSizes.add(thisBatchCount);
            from = to;
        }

        if (batches.isEmpty()) {
            cb.onSuccess(0, broadcastId);
            return;
        }

        commitChain(batches, batchSizes, 0, 0, broadcastId, cb);
    }

    private static void commitChain(@NonNull List<WriteBatch> batches,
                                    @NonNull List<Integer> batchSizes,
                                    int index,
                                    int deliveredSoFar,
                                    @NonNull String broadcastId,
                                    @NonNull Callback cb) {
        if (index >= batches.size()) {
            cb.onSuccess(deliveredSoFar, broadcastId);
            return;
        }

        WriteBatch batch = batches.get(index);
        int thisBatchSize = batchSizes.get(index);

        batch.commit()
                .addOnSuccessListener(v ->
                        commitChain(batches, batchSizes, index + 1, deliveredSoFar + thisBatchSize, broadcastId, cb))
                .addOnFailureListener(cb::onError);
    }

}

