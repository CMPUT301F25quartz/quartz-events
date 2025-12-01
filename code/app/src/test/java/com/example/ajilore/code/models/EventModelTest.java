package com.example.ajilore.code.models;

import static org.junit.Assert.*;

import com.example.ajilore.code.ui.events.model.Event;

import org.junit.Test;

/**
 * Focused unit tests for the Event model (flag status + basic defaults).
 */
public class EventModelTest {

    @Test
    public void defaultConstructor_initializesSafeDefaults_andNotFlagged() {
        Event event = new Event();

        // Strings should start as null
        assertNull(event.id);
        assertNull(event.title);
        assertNull(event.type);
        assertNull(event.location);
        assertNull(event.posterUrl);
        assertNull(event.status);
        assertNull(event.createdByUid);
        assertNull(event.flaggedReason);

        // Timestamps should start as null
        assertNull(event.startsAt);
        assertNull(event.regOpens);
        assertNull(event.regCloses);
        assertNull(event.createdAt);

        // capacity is a primitive int, so it defaults to 0
        assertEquals(0, event.capacity);

        // With null status, event must not be flagged
        assertFalse(event.isFlagged());
    }

    @Test
    public void isFlagged_returnsTrue_whenStatusIsFlagged_caseInsensitive() {
        Event event = new Event();

        event.status = "flagged";
        assertTrue(event.isFlagged());

        event.status = "FLAGGED";
        assertTrue(event.isFlagged());

        event.status = "FlAgGeD";
        assertTrue(event.isFlagged());
    }

    @Test
    public void isFlagged_returnsFalse_whenStatusIsNullOrNotFlagged() {
        Event event = new Event();

        // null (default)
        event.status = null;
        assertFalse(event.isFlagged());

        // some other valid statuses
        event.status = "published";
        assertFalse(event.isFlagged());

        event.status = "draft";
        assertFalse(event.isFlagged());

        // random string
        event.status = "something else";
        assertFalse(event.isFlagged());
    }
}