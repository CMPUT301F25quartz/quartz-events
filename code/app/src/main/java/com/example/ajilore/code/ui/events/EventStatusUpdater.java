package com.example.ajilore.code.ui.events;

import android.util.Log;
import com.google.firebase.functions.FirebaseFunctions;

public class EventStatusUpdater {

    private static final String TAG = "EventStatusUpdater";
    private final FirebaseFunctions functions;

    public EventStatusUpdater() {
        this.functions = FirebaseFunctions.getInstance();
    }

    public void updateAllEventStatuses(OnUpdateCompleteListener listener) {
        functions
                .getHttpsCallable("manualUpdateStatuses")
                .call()
                .addOnSuccessListener(httpsCallableResult -> {
                    Log.d(TAG, "Status update successful");
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Status update failed", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public interface OnUpdateCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}