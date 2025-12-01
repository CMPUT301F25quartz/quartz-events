package com.example.ajilore.code.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times; // Added this import
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ajilore.code.ui.events.model.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

public class AdminControllerTest {

    @Mock private FirebaseFirestore mockDb;
    @Mock private FirebaseStorage mockStorage;

    // Firestore Structure Mocks
    @Mock private CollectionReference mockCollection;
    @Mock private DocumentReference mockDoc;
    @Mock private WriteBatch mockBatch;
    @Mock private Query mockQuery;

    // Task Mocks
    @Mock private Task<Void> mockVoidTask;
    @Mock private Task<DocumentSnapshot> mockDocTask;
    @Mock private Task<QuerySnapshot> mockQueryTask;
    @Mock private Task<DocumentReference> mockRefTask;

    // Data Mocks
    @Mock private DocumentSnapshot mockDocSnapshot;
    @Mock private QuerySnapshot mockQuerySnapshot;
    @Mock private QueryDocumentSnapshot mockQueryDocSnapshot;
    @Mock private DocumentReference mockNewDocRef;

    private AdminController adminController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // 1. Initialize Controller with Mocks
        adminController = new AdminController(mockDb, mockStorage);

        // 2. Mock basic Firestore chain
        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDoc);

        // 3. Mock Batch operations
        when(mockDb.batch()).thenReturn(mockBatch);
        when(mockBatch.commit()).thenReturn(mockVoidTask);

        // 4. Mock Query operations
        when(mockCollection.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);

        // 5. Mock Document retrieval
        when(mockDoc.get()).thenReturn(mockDocTask);

        // 6. Mock collection.add()
        when(mockCollection.add(any())).thenReturn(mockRefTask);

        // 7. Setup Tasks to trigger "Success" immediately
        setupTaskSuccess(mockVoidTask, null);
        setupTaskSuccess(mockDocTask, mockDocSnapshot);
        setupTaskSuccess(mockQueryTask, mockQuerySnapshot);
        setupTaskSuccess(mockRefTask, mockNewDocRef);
    }

    private <T> void setupTaskSuccess(Task<T> task, T result) {
        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<T> listener = invocation.getArgument(0);
            listener.onSuccess(result);
            return task;
        });
    }

    @Test
    public void testRemoveEvent_DeletesDocument() {
        // Arrange
        String eventId = "event123";
        Event mockEvent = new Event();
        mockEvent.posterUrl = "http://fake.url/image.png";
        mockEvent.title = "Test Event";

        when(mockDocSnapshot.exists()).thenReturn(true);
        when(mockDocSnapshot.toObject(Event.class)).thenReturn(mockEvent);
        when(mockDoc.delete()).thenReturn(mockVoidTask);

        // Act
        adminController.removeEvent(eventId, mock(AdminController.OperationCallback.class));

        // Assert
        verify(mockDoc).delete();
        verify(mockCollection).add(any());
    }

    @Test
    public void testRemoveOrganizer_AtomicOperations() {
        // Arrange
        String organizerId = "org123";

        // Mock User Fetch
        when(mockDocSnapshot.exists()).thenReturn(true);
        when(mockDocSnapshot.getString("name")).thenReturn("Test Organizer");

        // Mock Events Fetch
        when(mockQuerySnapshot.iterator()).thenReturn(Collections.singletonList(mockQueryDocSnapshot).iterator());
        when(mockQueryDocSnapshot.getReference()).thenReturn(mockDoc);

        // Act
        adminController.removeOrganizer(organizerId, mock(AdminController.OperationCallback.class));

        // Assert
        // Verify .document() was called 3 times (User fetch, Ban add, User delete)
        verify(mockCollection, times(3)).document(organizerId);

        // Verify we looked up their events
        verify(mockCollection).whereEqualTo("createdByUid", organizerId);

        // Verify Batch Operations
        verify(mockDb).batch();
        verify(mockBatch).update(eq(mockDoc), eq("status"), eq("flagged"));
        verify(mockBatch).set(any(DocumentReference.class), any(java.util.Map.class));
        verify(mockBatch).delete(any(DocumentReference.class));
        verify(mockBatch).commit();

        // Verify Audit Log
        verify(mockCollection).add(any());
    }
}