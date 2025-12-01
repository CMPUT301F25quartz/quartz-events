package com.example.ajilore.code.models;

import org.junit.Test;
import static org.junit.Assert.*;
import com.example.ajilore.code.ui.events.model.Event;
import com.google.firebase.Timestamp;
import java.util.Date;

/**
 * Unit tests for Admin-related models.
 * Covers: User, Event, ImageItem, NotificationLog
 */
public class AdminModelsTest {

    @Test
    public void testUserCreation() {
        User user = new User("user123", "John Doe", "john@example.com", "1234567890", "entrant");

        assertEquals("user123", user.getUserId());
        assertEquals("John Doe", user.getName());
        assertEquals("entrant", user.getRole());
        assertTrue("Notifications should be enabled by default", user.isNotificationsEnabled());

        user.setRole("organiser");
        assertEquals("organiser", user.getRole());
    }

    @Test
    public void testImageItem() {
        long now = System.currentTimeMillis();
        ImageItem item = new ImageItem("http://url.com/img.png", "evt1", "Concert", now);

        assertEquals("event", item.type);
        assertEquals("Concert", item.title);
        assertEquals("evt1", item.eventId);
        assertEquals(now, item.uploadedAt);
    }

    @Test
    public void testNotificationLog() {
        NotificationLog log = new NotificationLog();
        log.setMessage("Test Message");
        log.setAudience("waiting");
        log.setEventTitle("Piano Class");

        assertEquals("Test Message", log.getMessage());
        assertEquals("waiting", log.getAudience());
        assertEquals("Piano Class", log.getEventTitle());
    }
}