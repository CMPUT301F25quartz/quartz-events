package com.example.ajilore.code.models;


import org.junit.Test;

import static org.junit.Assert.*;

import com.example.ajilore.code.ui.inbox.NotificationModel;

/**
 * Unit tests for NotificationModel.
 */
public class NotificationModelTest {

    @Test
    public void equals_returnsTrue_whenFirestoreDocIdMatches() {
        NotificationModel first = new NotificationModel();
        first.setFirestoreDocId("doc-123");

        NotificationModel second = new NotificationModel();
        second.setFirestoreDocId("doc-123");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void equals_returnsFalse_whenFirestoreDocIdDiffers() {
        NotificationModel first = new NotificationModel();
        first.setFirestoreDocId("doc-123");

        NotificationModel second = new NotificationModel();
        second.setFirestoreDocId("doc-999");

        assertFalse(first.equals(second));
    }

    @Test
    public void equals_returnsFalse_whenOtherIsNotNotificationModel() {
        NotificationModel model = new NotificationModel();
        model.setFirestoreDocId("doc-123");

        assertFalse(model.equals("not a notification"));
    }

    @Test
    public void equals_returnsFalse_whenThisFirestoreDocIdIsNull() {
        NotificationModel first = new NotificationModel();
        first.setFirestoreDocId(null);

        NotificationModel second = new NotificationModel();
        second.setFirestoreDocId("doc-123");

        assertFalse(first.equals(second));
    }

    @Test
    public void hashCode_isZero_whenFirestoreDocIdIsNull() {
        NotificationModel model = new NotificationModel();
        model.setFirestoreDocId(null);

        assertEquals(0, model.hashCode());
    }

    @Test
    public void settersAndGetters_workAsExpected() {
        NotificationModel model = new NotificationModel();

        model.setEventId("evt-1");
        model.setFirestoreDocId("doc-1");
        model.setMessage("Hello");
        model.setTime("10:00 AM");
        model.setImageUrl("http://image");
        model.setRead(true);
        model.setActionText("Open");
        model.setType("info");

        assertEquals("evt-1", model.getEventId());
        assertEquals("doc-1", model.getFirestoreDocId());
        assertEquals("Hello", model.getMessage());
        assertEquals("10:00 AM", model.getTime());
        assertEquals("http://image", model.getImageUrl());
        assertTrue(model.isRead());
        assertEquals("Open", model.getActionText());
        assertEquals("info", model.getType());
    }
}
