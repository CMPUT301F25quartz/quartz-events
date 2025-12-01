package com.example.ajilore.code.ui.history;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.EventDetailsFragment;
import com.example.ajilore.code.ui.events.EventsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code HistoryFragment} displays a list of all events that the currently
 * signed-in user has registered for.
 * <p>
 * This feature supports the user story:
 * <b>US 01.02.03:</b> “As an entrant, I want to have a history of events
 * I have registered for, whether I was selected or not.”
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *     <li>Fetches registration history from Firestore under
 *         {@code users/{uid}/registrations}</li>
 *     <li>Displays the events in reverse chronological order</li>
 *     <li>Shows a loading indicator while fetching data</li>
 *     <li>Handles empty states and load failures gracefully</li>
 * </ul>
 *
 * <p><b>Firestore structure used:</b></p>
 * <pre>
 * users (collection)
 *  └── {uid} (document)
 *       └── registrations (subcollection)
 *            ├── {eventId} (document)
 *            │    ├── eventTitle: String
 *            │    ├── eventId: String
 *            │    ├── registeredAt: Timestamp
 * </pre>
 *
 * @author
 * Temi Akindele
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
            * Inflates the layout for the history screen.
     *
             * @param inflater  LayoutInflater used to inflate the fragment layout.
            * @param container Parent container that holds this fragment’s view.
            * @param savedInstanceState Previously saved instance state (if any).
            * @return The root view for the history fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
    /**
     * Called when the view is created. Sets up the RecyclerView, adapter,
     * and begins loading the user’s registration history from Firestore.
     *
     * @param v The root view of the fragment.
     * @param savedInstanceState Saved state bundle, if any.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        recyclerView = v.findViewById(R.id.recyclerHistory);
        progressBar = v.findViewById(R.id.progressHistory);
        ImageButton btnBack = v.findViewById(R.id.btnBack);

        //Added in a back button
        if (btnBack != null){
            btnBack.setOnClickListener(view -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, new EventsFragment())
                        .commit();
            });
        }


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
     * Loads the user's registration history from Firestore.
     * <p>
     * Documents are retrieved from the {@code users/{uid}/registrations}
     * subcollection and ordered by {@code registeredAt} in descending order.
     * Results are displayed in a RecyclerView using {@link HistoryAdapter}.
     * </p>
     *
     * <p>Displays a progress bar during data loading and shows an error
     * message if the request fails.</p>
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
