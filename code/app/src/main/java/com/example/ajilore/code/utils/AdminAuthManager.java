package com.example.ajilore.code.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Manages device-based authentication to grant administrative privileges.
 *
 * <p>This class authorizes devices based on a set of pre-defined unique device IDs
 * (Android IDs). For production readiness, the hardcoded list must be replaced
 * with a secure, server-side authorization mechanism (e.g., fetching authorized
 * IDs from Firebase Firestore) to prevent client-side tampering and
 * unauthorized access.</p>
 *
 * <p>Note: The {@link #ADMIN_DEVICE_IDS} set is hardcoded for initial development/testing
 * and should be considered highly sensitive.</p> implements aspects of the Strategy pattern for different authentication methods.</p>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminAuthManager {
    private static final String TAG = "AdminAuthManager";

    // TODO: In production, fetch this from Firebase Firestore in an "adminDevices" collection
    // For now, hardcoded list of admin device IDs
    private static final Set<String> ADMIN_DEVICE_IDS = new HashSet<>(Arrays.asList(
            "your_device_id_here",           // Replace with actual device ID
            "another_admin_device_id",       // Add more as needed
            "test_admin_device"              // For testing
    ));

    /**
     * Get the unique device ID for this Android device
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.d(TAG, "Device ID: " + deviceId);
        return deviceId;
    }

    /**
     * Check if the current device has admin privileges
     */
    public static boolean isAdmin(Context context) {
        String deviceId = getDeviceId(context);
        boolean isAdmin = ADMIN_DEVICE_IDS.contains(deviceId);
        Log.d(TAG, "Is Admin: " + isAdmin + " (Device: " + deviceId + ")");
        return isAdmin;
    }

    /**
     * For testing: temporarily grant admin access to current device
     * Remove this method in production!
     */
    public static void addCurrentDeviceAsAdmin(Context context) {
        String deviceId = getDeviceId(context);
        ADMIN_DEVICE_IDS.add(deviceId);
        Log.d(TAG, "⚠ Added current device as admin (TESTING ONLY): " + deviceId);
    }
}