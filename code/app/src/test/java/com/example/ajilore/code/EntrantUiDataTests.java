package com.example.ajilore.code;


import static org.junit.Assert.*;

import com.example.ajilore.code.ui.events.data.Entrant;

import org.junit.Test;

/**
 * Unit tests for the lightweight UI Entrant model
 * (com.example.ajilore.code.ui.events.data.Entrant).
 */
public class EntrantUiDataTests {

    @Test
    public void constructor_setsAllFields_whenNameAndStatusProvided() {
        Entrant entrant = new Entrant(
                "uid_123",
                "Jane Doe",
                "Accepted",
                "http://example.com/pic.jpg"
        );

        assertEquals("uid_123", entrant.uid);
        assertEquals("Jane Doe", entrant.nameOrUid);
        assertEquals("Accepted", entrant.displayStatus);
        assertEquals("http://example.com/pic.jpg", entrant.profilePictureUrl);
    }

    @Test
    public void constructor_fallsBackToUid_whenNameIsNullOrEmpty() {
        Entrant withNullName = new Entrant(
                "uid_abc",
                null,
                "Pending",
                null
        );
        assertEquals("uid_abc", withNullName.nameOrUid);

        Entrant withEmptyName = new Entrant(
                "uid_xyz",
                "",
                "Pending",
                null
        );
        assertEquals("uid_xyz", withEmptyName.nameOrUid);
    }

    @Test
    public void constructor_defaultsStatusToPending_whenStatusNullOrEmpty() {
        Entrant nullStatus = new Entrant(
                "uid_1",
                "User 1",
                null,
                null
        );
        assertEquals("Pending", nullStatus.displayStatus);

        Entrant emptyStatus = new Entrant(
                "uid_2",
                "User 2",
                "",
                null
        );
        assertEquals("Pending", emptyStatus.displayStatus);
    }
}
