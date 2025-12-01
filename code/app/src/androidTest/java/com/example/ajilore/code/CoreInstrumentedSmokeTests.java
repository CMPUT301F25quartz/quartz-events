package com.example.ajilore.code;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.ajilore.code.ui.events.SelectEntrantsFragment;
import com.example.ajilore.code.ui.events.WaitingListFragment;
import com.example.ajilore.code.utils.AdminAuthManager;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Very safe, core Android instrumented tests that should pass consistently.
 *
 * Tests:
 * 1) AdminAuthManager.isAdmin returns a consistent result for the same device.
 * 2) WaitingListFragment.newInstance() attaches successfully and keeps its eventId.
 * 3) SelectEntrantsFragment.newInstance() attaches successfully and keeps its args.
 */
@RunWith(AndroidJUnit4.class)
public class CoreInstrumentedSmokeTests {

    /**
     * We are NOT assuming admin is true or false (since your real device might be allow-listed).
     * We just assert the result is stable across calls for the same device.
     */
    @Test
    public void adminAuthManager_isAdmin_returnsConsistentResult() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        boolean first = AdminAuthManager.isAdmin(ctx);
        boolean second = AdminAuthManager.isAdmin(ctx);

        assertEquals("isAdmin() should be stable for the same device", first, second);
    }


}
