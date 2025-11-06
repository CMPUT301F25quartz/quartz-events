
package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.ajilore.code.ui.events.GeneralEventsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * GeneralEventsFragment – emulator-free core tests.
 *
 * Tests:
 * 1) coreUi_renders_noFirestore — fragment shows its list container.
 * 2) recycler_hasAdapter_attached — RecyclerView has an adapter (regardless of data).
 *
 */
@RunWith(AndroidJUnit4.class)
public class GeneralEventsFragmentTests {

    /** 1) Smoke: the RecyclerView is visible (no assumptions about data). */
    @Test
    public void coreUi_renders_noFirestore() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new GeneralEventsFragment())
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.rvEvents)).check(matches(withEffectiveVisibility(
                    androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
        }
    }

    /** 2) RecyclerView has an adapter attached (works whether list is empty or not). */
    @Test
    public void recycler_hasAdapter_attached() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new GeneralEventsFragment())
                    .commit());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            final boolean[] hasAdapter = { false };
            final int[] count = { -1 };
            sc.onActivity(a -> {
                RecyclerView rv = a.findViewById(R.id.rvEvents);
                hasAdapter[0] = rv != null && rv.getAdapter() != null;
                count[0] = hasAdapter[0] ? rv.getAdapter().getItemCount() : -1;
            });

            org.junit.Assert.assertTrue("RecyclerView should have an adapter", hasAdapter[0]);
            org.junit.Assert.assertTrue("Adapter itemCount should be >= 0", count[0] >= 0);
        }
    }
}
