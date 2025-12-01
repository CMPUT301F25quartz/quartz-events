package com.example.ajilore.code.models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the User model.
 */
public class UserTest {

    @Test
    public void defaultConstructor_setsExpectedDefaults() {
        User user = new User();

        // From the class:
        // private boolean notificationsEnabled;
        // private String accountStatus = "active";
        // private boolean canCreateEvents = true;

        assertFalse("notificationsEnabled should default to false",
                user.isNotificationsEnabled());
        assertEquals("active", user.getAccountStatus());
        assertTrue("canCreateEvents should default to true",
                user.isCanCreateEvents());
    }

    @Test
    public void settersAndGetters_workAsExpected() {
        User user = new User();

        user.setUserId("uid-123");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPhone("555-1234");
        user.setProfileImageUrl("http://example.com/image.jpg");
        user.setNotificationsEnabled(true);
        user.setAccountStatus("deactivated");
        user.setCanCreateEvents(false);
        user.setRole("organizer");

        assertEquals("uid-123", user.getUserId());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("555-1234", user.getPhone());
        assertEquals("http://example.com/image.jpg", user.getProfileImageUrl());
        assertTrue(user.isNotificationsEnabled());
        assertEquals("deactivated", user.getAccountStatus());
        assertFalse(user.isCanCreateEvents());
        assertEquals("organizer", user.getRole());
    }
}