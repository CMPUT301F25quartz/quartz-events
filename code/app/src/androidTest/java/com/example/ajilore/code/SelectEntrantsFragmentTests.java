/**
 * SelectEntrantsFragment Tests (androidTest, no direct reference to inner Entrant class).
 *
 * Tests:
 * 1) Core UI smoke test (no Firestore).
 *


 */

package com.example.ajilore.code;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.ui.events.SelectEntrantsFragment;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import androidx.fragment.app.Fragment;



@RunWith(AndroidJUnit4.class)
public class SelectEntrantsFragmentTests {

    private static final String EVENT_ID = "evt_run_draw_test";
    private static FirebaseFirestore db;

    @BeforeClass
    public static void initDb() {
        db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8080); // Android emulator loopback
    }


    // 1) Core UI smoke test (no Firestore)
    @Test
    public void coreUi_renders_withArgs_noFirestore() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            // Show fragment with safe dummy args
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, SelectEntrantsFragment.newInstance("evt_test", "Test Event"))
                    .commitNow());

            // Scroll to make views visible before asserting (NestedScrollView)
            onView(withId(R.id.tvEventTitle))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                    .check(matches(isDisplayed()));

            onView(withId(R.id.etCount))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                    .check(matches(isDisplayed()));

            onView(withId(R.id.btnRunDraw))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                    .check(matches(isDisplayed()));

            // RecyclerView sometimes resists scrollTo(); if it complains, use effective visibility:
            try {
                onView(withId(R.id.rvSelected))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                        .check(matches(isDisplayed()));
            } catch (Throwable ignore) {
                onView(withId(R.id.rvSelected))
                        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
            }
        }
    }

    @Test
    public void runDraw_withBlankCount_keepsButtonEnabled() {
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            // Put fragment on screen with safe args
            sc.onActivity(a -> a.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment,
                            SelectEntrantsFragment.newInstance("evt_test", "Test Event"))
                    .commitNow());

            // Scroll so button is in view, click with blank input
            onView(withId(R.id.btnRunDraw))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                    .perform(click());

            // Button should still be enabled (no draw executed)
            onView(withId(R.id.btnRunDraw))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }




}
