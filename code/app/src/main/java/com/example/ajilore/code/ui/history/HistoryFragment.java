package com.example.ajilore.code.ui.history;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.EventDetailsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code HistoryFragment} displays a list of all events that the current
 * device user has registered for.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *     <li>Fetch the registration history from Firestore under
 *         {@code users/{deviceId}/registrations}</li>
 *     <li>Provide navigation to an event’s details when clicked</li>
 * </ul>
 *
 * <p>The fragment uses a {@link RecyclerView} backed by a
 * {@link HistoryAdapter} to present event entries.</p>
 *
 * @author
 *     Temi Akindele
 */

public class HistoryFragment extends Fragment {

    //UI components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    //Adapter
    private HistoryAdapter adapter;
    private List<Map<String, Object>> historyList = new ArrayList<>();
    //firebase
    private FirebaseFirestore db;
    private String deviceId;
    /**
     * Inflates the layout for the History screen.
     *
     * @param inflater  LayoutInflater used to inflate the XML for this fragment
     * @param container Parent view the fragment will be attached to
     * @param savedInstanceState Previously saved state, if any
     * @return The root view for the History Fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
    /**
     * Initializes UI components, sets up the RecyclerView and adapter,
     * retrieves the device ID, and begins loading registration history.
     *
     * <p>Clicking an event item navigates the user to
     * {@link EventDetailsFragment} using the event ID and title.</p>
     *
     * @param v The root view returned by {@link #onCreateView}
     * @param savedInstanceState If the fragment is being re-created from a previous state
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        recyclerView = v.findViewById(R.id.recyclerHistory);
        progressBar = v.findViewById(R.id.progressHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        adapter = new HistoryAdapter(historyList, item -> {
            String eventId = (String) item.get("eventId");
            String title   = (String) item.get("eventTitle");

            if (eventId != null) {
                EventDetailsFragment f = EventDetailsFragment.newInstance(eventId, title);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, f)
                        .addToBackStack(null)
                        .commit();
            }
        });
        recyclerView.setAdapter(adapter);

        loadHistory();
    }
    /**
     * Loads the user's event registration history from Firestore.
     *
     * <p>Data is retrieved from the subcollection:
     * {@code users/{deviceId}/registrations}, ordered by
     * {@code registeredAt} descending so the most recent registrations appear first.</p>
     *
     * <p>Shows a progress bar while loading, updates the adapter when complete,
     * and displays an error message on failure.</p>
     */
    private void loadHistory() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .document(deviceId)
                .collection("registrations")
                .orderBy("registeredAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    historyList.clear();
                    for (DocumentSnapshot doc : query) {
                        historyList.add(doc.getData());
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
