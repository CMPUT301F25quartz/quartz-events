package com.example.ajilore.code.utils;

import android.content.Context;
import android.provider.Settings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminAuthManager.
 *
 * Tests admin authentication and authorization logic.
 *
 * @author Dinma (Team Quartz)
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class AdminAuthManagerTest {

    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
    }

    /**
     * Test that getDeviceId returns a non-null value
     */
    @Test
    public void testGetDeviceIdReturnsValue() {
        String deviceId = AdminAuthManager.getDeviceId(context);

        assertNotNull("Device ID should not be null", deviceId);
        assertFalse("Device ID should not be empty", deviceId.isEmpty());
    }

    /**
     * Test isAdmin returns false for unknown device by default
     */
    @Test
    public void testIsAdminReturnsFalseByDefault() {
        boolean isAdmin = AdminAuthManager.isAdmin(context);

        // By default, test device is not in the admin list
        assertFalse("Unknown device should not be admin", isAdmin);
    }

    /**
     * Test addCurrentDeviceAsAdmin grants admin access
     */
    @Test
    public void testAddCurrentDeviceAsAdminGrantsAccess() {
        // Initially not admin
        boolean wasAdmin = AdminAuthManager.isAdmin(context);
        assertFalse("Device should not be admin initially", wasAdmin);

        // Grant admin access
        AdminAuthManager.addCurrentDeviceAsAdmin(context);

        // Now should be admin
        boolean isAdminNow = AdminAuthManager.isAdmin(context);
        assertTrue("Device should be admin after being added", isAdminNow);
    }

    /**
     * Test device ID is consistent across calls
     */
    @Test
    public void testDeviceIdConsistency() {
        String deviceId1 = AdminAuthManager.getDeviceId(context);
        String deviceId2 = AdminAuthManager.getDeviceId(context);

        assertEquals("Device ID should be consistent", deviceId1, deviceId2);
    }

    /**
     * Test isAdmin with valid context
     */
    @Test
    public void testIsAdminWithValidContext() {
        // Should not crash with valid context
        try {
            boolean result = AdminAuthManager.isAdmin(context);
            // Result can be true or false, just shouldn't crash
            assertNotNull("Result should not be null", (Boolean) result);
        } catch (Exception e) {
            fail("isAdmin should not throw exception: " + e.getMessage());
        }
    }

    /**
     * Test getDeviceId with valid context
     */
    @Test
    public void testGetDeviceIdWithValidContext() {
        try {
            String deviceId = AdminAuthManager.getDeviceId(context);
            assertNotNull("Device ID should not be null", deviceId);
        } catch (Exception e) {
            fail("getDeviceId should not throw exception: " + e.getMessage());
        }
    }

    /**
     * Test that admin status can be verified multiple times
     */
    @Test
    public void testMultipleAdminChecks() {
        // Add device as admin
        AdminAuthManager.addCurrentDeviceAsAdmin(context);

        // Check multiple times
        assertTrue("First check should return true", AdminAuthManager.isAdmin(context));
        assertTrue("Second check should return true", AdminAuthManager.isAdmin(context));
        assertTrue("Third check should return true", AdminAuthManager.isAdmin(context));
    }
}