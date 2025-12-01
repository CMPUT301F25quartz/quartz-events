package com.example.ajilore.code;

import static org.junit.Assert.assertFalse;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.utils.AdminAuthManager;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test for AdminAuthManager.
 *
 * Assumes the emulator / test device is NOT one of the hardcoded admin device IDs.
 */
@RunWith(AndroidJUnit4.class)
public class AdminAuthManagerInstrumentedTest {

    @Test
    public void isAdmin_returnsFalse_onEmulatorOrUnknownDevice() {
        Context context = ApplicationProvider.getApplicationContext();

        boolean isAdmin = AdminAuthManager.isAdmin(context);

        // On the Android emulator (and most non-whitelisted phones),
        // this should be false.
        assertFalse(isAdmin);
    }
}
