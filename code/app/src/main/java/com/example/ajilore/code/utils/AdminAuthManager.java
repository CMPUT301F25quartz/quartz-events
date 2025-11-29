package com.example.ajilore.code.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages device-based authentication to grant administrative privileges.
 *
 * <p>This class authorizes devices based on a strictly defined allowlist of unique
 * Android Device IDs. It serves as the primary access control mechanism for
 * the Administrator Dashboard, ensuring only designated hardware can access
 * sensitive administrative features.</p>
 *
 * <p><b>Implementation Note:</b> The list of authorized devices is statically
 * defined within this class. This approach ensures that administrative access
 * is restricted to specific, known devices associated with the project team
 * without requiring an external authentication service for this prototype phase.</p>
 *
 * @author Dinma (Team Quartz)
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminAuthManager {
    private static final String TAG = "AdminAuthManager";

    /**
     * A set of hardcoded Android Device IDs authorized for administrative access.
     * <p>
     * Implementation Note: These IDs correspond to the specific devices owned by
     * the development team and authorized stakeholders.
     * </p>
     */
    private static final Set<String> AUTHORIZED_DEVICE_IDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "0da496796da0ac39",
            "0b5f61e7b8ce395e",
            "282bb7ff4beb39dc",
            "d46fd6b7513064fc"
    )));

    /**
     * Retrieves the unique ANDROID_ID for the current device.
     *
     * <p>The ANDROID_ID is a 64-bit hex string that is unique to each combination
     * of app-signing key, user, and device.</p>
     *
     * @param context The application context required to access system settings.
     * @return The unique Android Device ID string.
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.d(TAG, "Device ID Retrieval: " + deviceId);
        return deviceId;
    }

    /**
     * Verifies if the current device is authorized to access Admin features.
     *
     * <p>Checks the current device's ID against the {@link #AUTHORIZED_DEVICE_IDS} allowlist.</p>
     *
     * @param context The application context.
     * @return {@code true} if the device is authorized; {@code false} otherwise.
     */
    public static boolean isAdmin(Context context) {
        String deviceId = getDeviceId(context);
        boolean isAuthorized = AUTHORIZED_DEVICE_IDS.contains(deviceId);

        if (isAuthorized) {
            Log.i(TAG, "Administrative access granted for device: " + deviceId);
        } else {
            Log.i(TAG, "Administrative access denied for device: " + deviceId);
        }

        return isAuthorized;
    }
}