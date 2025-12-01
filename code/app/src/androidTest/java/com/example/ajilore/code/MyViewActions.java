package com.example.ajilore.code;

import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

public class MyViewActions {

    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " millis.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // REMOVED: uiController.loopMainThreadUntilIdle();
                // This was causing the NullPointerException because Firestore keeps the thread busy.

                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                while (System.currentTimeMillis() < endTime) {
                    // This safely waits without crashing on background tasks
                    uiController.loopMainThreadForAtLeast(50);
                }
            }
        };
    }

    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isEnabled(); // Only check if view is enabled
            }

            @Override
            public String getDescription() {
                return "force click";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        };
    }

    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }
}