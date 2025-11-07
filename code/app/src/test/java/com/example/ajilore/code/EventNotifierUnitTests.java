package com.example.ajilore.code;

import com.example.ajilore.code.ui.events.EventNotifier;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pure JVM tests for EventNotifier using Mockito to mock Firestore.
 * No emulator or Android device required.
 */
public class EventNotifierUnitTests {

    // Firestore graph
    private FirebaseFirestore db;
    private CollectionReference orgEventsCol;
    private DocumentReference eventDoc;
    private CollectionReference broadcastsCol;
    private CollectionReference waitingListCol;
    private CollectionReference inboxCol;
    private DocumentReference inboxDoc; // for notifySingle path (document())

    // Tasks
    private Task<DocumentReference> taskAddBroadcast;
    private Task<QuerySnapshot> taskGetWaiting;
    private Task<Void> taskBatchCommit;
    private Task<Void> taskInboxSet;

    // WriteBatch (we’ll return the same instance; commit will call success)
    private WriteBatch batch;

    // Helpers to immediately fire success listeners
    private <T> Task<T> succeed(Task<T> task, T value) {
        // Make addOnSuccessListener immediately call listener
        when(task.addOnSuccessListener(any())).thenAnswer(inv -> {
            OnSuccessListener<T> l = inv.getArgument(0);
            l.onSuccess(value);
            return task;
        });
        // Pass through addOnFailureListener
        when(task.addOnFailureListener(any())).thenReturn(task);
        return task;
    }

    @Before
    public void setUp() {
        // Mocks
        db = mock(FirebaseFirestore.class);
        orgEventsCol = mock(CollectionReference.class);
        eventDoc = mock(DocumentReference.class);
        broadcastsCol = mock(CollectionReference.class);
        waitingListCol = mock(CollectionReference.class);
        inboxCol = mock(CollectionReference.class);
        inboxDoc = mock(DocumentReference.class);

        taskAddBroadcast = mock(Task.class);
        taskGetWaiting = mock(Task.class);
        taskBatchCommit = mock(Task.class);
        taskInboxSet = mock(Task.class);

        batch = mock(WriteBatch.class);

        // Firestore path stubbing
        when(db.collection("org_events")).thenReturn(orgEventsCol);
        when(orgEventsCol.document(anyString())).thenReturn(eventDoc);
        when(eventDoc.collection(eq("broadcasts"))).thenReturn(broadcastsCol);
        when(eventDoc.collection(eq("waiting_list"))).thenReturn(waitingListCol);

        // For inbox fanout path .../waiting_list/{uid}/inbox
        // We’ll let writeInboxInChunks call: ...document(uid).collection("inbox").document()
        // Use generic answers to avoid per-uid wiring
        when(waitingListCol.document(anyString())).thenAnswer(a -> {
            String uid = a.getArgument(0);
            DocumentReference userDoc = mock(DocumentReference.class);
            when(userDoc.collection(eq("inbox"))).thenReturn(inboxCol);
            when(inboxCol.document()).thenReturn(inboxDoc);
            return userDoc;
        });

        // add(payload) returns Task<DocumentReference> that immediately succeeds
        when(broadcastsCol.add(anyMap())).thenReturn(taskAddBroadcast);
        DocumentReference fakeBroadcastRef = mock(DocumentReference.class);
        when(fakeBroadcastRef.getId()).thenReturn("bcast_123");
        succeed(taskAddBroadcast, fakeBroadcastRef);

        // waiting_list.get() returns Task<QuerySnapshot> — we control per-test
        when(waitingListCol.get()).thenReturn(taskGetWaiting);

        // Batch wiring
        when(db.batch()).thenReturn(batch);
        when(batch.set(any(DocumentReference.class), anyMap())).thenReturn(batch);
        when(batch.commit()).thenReturn(taskBatchCommit);
        succeed(taskBatchCommit, null);

        // notifySingle: inboxRef.set(...) returns taskInboxSet that succeeds
        when(inboxDoc.set(anyMap())).thenReturn(taskInboxSet);
        succeed(taskInboxSet, null);
    }

    // Build a QuerySnapshot with a list of faux docs (uids + fields)
    private QuerySnapshot buildWaitingSnapshot(List<FauxDoc> docs) {
        QuerySnapshot qs = mock(QuerySnapshot.class);

        List<QueryDocumentSnapshot> qdocs = new ArrayList<>();
        for (FauxDoc fd : docs) {
            QueryDocumentSnapshot q = mock(QueryDocumentSnapshot.class);
            when(q.getId()).thenReturn(fd.uid);
            when(q.getString(eq("status"))).thenReturn(fd.status);
            when(q.getString(eq("responded"))).thenReturn(fd.responded);
            qdocs.add(q);
        }
        when(qs.isEmpty()).thenReturn(qdocs.isEmpty());
        when(qs.iterator()).thenReturn(qdocs.iterator());
        when(qs.getDocuments()).thenReturn(new ArrayList<>(qdocs));
        return qs;
    }

    private static class FauxDoc {
        final String uid, status, responded;
        FauxDoc(String uid, String status, String responded) {
            this.uid = uid; this.status = status; this.responded = responded;
        }
    }

    @Test
    public void notifyAudience_waiting_filtersOnlyWaiting() {
        // Seed waiting list: 2 waiting, 1 chosen
        QuerySnapshot qs = buildWaitingSnapshot(Arrays.asList(
                new FauxDoc("u_wait_1", "waiting", "pending"),
                new FauxDoc("u_wait_2", "waiting", null),
                new FauxDoc("u_chosen_1", "chosen", "pending")
        ));
        succeed(taskGetWaiting, qs);

        final int[] delivered = { -1 };
        final String[] bcastId = { null };

        EventNotifier.notifyAudience(
                db,
                "evt_X",
                "Test Event",
                "Hello waiting!",
                true,
                null,
                "waiting",
                new EventNotifier.Callback() {
                    @Override public void onSuccess(int deliveredCount, String broadcastIdOrEmpty) {
                        delivered[0] = deliveredCount;
                        bcastId[0] = broadcastIdOrEmpty;
                    }
                    @Override public void onError(Exception e) {
                        fail("Should not fail: " + e);
                    }
                });

        // Delivered should be 2 (both waiting)
        assertEquals(2, delivered[0]);
        assertNotNull(bcastId[0]);
        assertFalse(bcastId[0].isEmpty());

        // Verify we queued batch sets for 2 recipients
        verify(batch, times(2)).set(any(DocumentReference.class), anyMap());
        // And commit was called once (single batch is enough)
        verify(batch, times(1)).commit();
    }

    @Test
    public void notifyAudience_chosen_excludesAcceptedAndDeclined() {
        // Seed: chosen/pending, chosen/null (include), chosen/accepted, chosen/declined (exclude)
        QuerySnapshot qs = buildWaitingSnapshot(Arrays.asList(
                new FauxDoc("u_chosen_pending", "chosen", "pending"),
                new FauxDoc("u_chosen_empty",   "chosen", null),
                new FauxDoc("u_chosen_acc",     "chosen", "accepted"),
                new FauxDoc("u_chosen_dec",     "chosen", "declined"),
                new FauxDoc("u_waiting",        "waiting", "pending") // not chosen
        ));
        succeed(taskGetWaiting, qs);

        final int[] delivered = { -1 };

        EventNotifier.notifyAudience(
                db, "evt_Y", "Title", "Please respond", false, null, "chosen",
                new EventNotifier.Callback() {
                    @Override public void onSuccess(int deliveredCount, String id) {
                        delivered[0] = deliveredCount;
                    }
                    @Override public void onError(Exception e) { fail(e.getMessage()); }
                });

        // Only 2 chosen users should be targeted (pending/null)
        assertEquals(2, delivered[0]);
        verify(batch, times(2)).set(any(DocumentReference.class), anyMap());
        verify(batch, times(1)).commit();
    }

    @Test
    public void notifySingle_writesExactlyOneInboxAndReturnsBroadcastId() {
        // We don't need waiting_list.get() in this path; it writes directly to one inbox.
        // Ensure add(broadcast) → success, inbox.set → success are already stubbed.

        final int[] delivered = { -1 };
        final String[] bcastId = { null };

        EventNotifier.notifySingle(
                db,
                "evt_Z",
                "Solo Title",
                "uid_1",
                "selected",
                "Congrats!",
                true,
                "https://example.com",
                new EventNotifier.Callback() {
                    @Override public void onSuccess(int deliveredCount, String broadcastIdOrEmpty) {
                        delivered[0] = deliveredCount;
                        bcastId[0] = broadcastIdOrEmpty;
                    }
                    @Override public void onError(Exception e) { fail(e.getMessage()); }
                });

        assertEquals(1, delivered[0]);
        assertNotNull(bcastId[0]);
        assertFalse(bcastId[0].isEmpty());

        // One inbox set (notifySingle)
        verify(inboxDoc, times(1)).set(anyMap());
    }
}
