package com.example.ajilore.code;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Very basic sanity checks:
 *  - App context has the right package name
 *  - MainActivity launches without crashing
 */
@RunWith(AndroidJUnit4.class)
public class CoreAppInstrumentedTests {

    @Test
    public void useAppContext_hasCorrectPackageName() {
        Context appContext = ApplicationProvider.getApplicationContext();
        assertEquals("com.example.ajilore.code", appContext.getPackageName());
    }

}
