package com.example.ajilore.code.models;


import com.example.ajilore.code.ui.inbox.NotificationModel;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for NotificationModel focusing on the short constructor
 * and read/unread behaviour.
 */
public class NotificationOtherTest {

    @Test
    public void shortConstructor_setsCoreFields_andDefaultExtras() {
        // eventId, firestoreDocId, message, isRead, type
        NotificationModel model = new NotificationModel(
                "evt-123",
                "doc-456",
                "You have been selected for an event",
                false,
                "selection"
        );

        // Core values from constructor
        assertEquals("evt-123", model.getEventId());
        assertEquals("doc-456", model.getFirestoreDocId());
        assertEquals("You have been selected for an event", model.getMessage());
        assertFalse(model.isRead());
        assertEquals("selection", model.getType());

        // Defaults defined in your short constructor
        assertEquals("", model.getTime());
        assertEquals("", model.getImageUrl());
        assertEquals("See Details", model.getActionText());
    }

    @Test
    public void setRead_togglesReadState() {
        NotificationModel model = new NotificationModel(
                "evt-123",
                "doc-456",
                "New notification",
                false,
                "info"
        );

        // Initially false (from constructor)
        assertFalse(model.isRead());

        // Mark as read
        model.setRead(true);
        assertTrue(model.isRead());

        // Mark unread again just to be extra sure
        model.setRead(false);
        assertFalse(model.isRead());
    }
}
