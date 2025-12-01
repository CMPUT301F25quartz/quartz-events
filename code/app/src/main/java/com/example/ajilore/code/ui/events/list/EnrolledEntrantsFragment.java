package com.example.ajilore.code.ui.events.list;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ajilore.code.R;
import com.example.ajilore.code.adapters.EntrantAdapter;
import com.example.ajilore.code.models.Entrant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * EnrolledEntrantsFragment
 *
 * <p>Displays a realtime list of entrants who have <b>accepted</b> their invitations
 * for a specific event. These entrants represent the final enrolled/attending group.</p>
 *
 * <p>This screen is event-scoped and reads from:</p>
 *
 * <pre>
 * org_events/{eventId}/waiting_list
 *      ├─ {uid}
 *      │    ├─ status = "chosen"
 *      │    ├─ responded = "accepted"
 *      │    ├─ joinedAt : Timestamp
 * </pre>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Realtime Firestore snapshot listener for accepted entrants</li>
 *     <li>Sorted by join time (descending)</li>
 *     <li>Displays list using {@link EntrantAdapter} in "enrolled" mode</li>
 *     <li>Supports CSV export visibility when the list is non-empty</li>
 *     <li>Handles empty states and loading indicators</li>
 * </ul>
 */
public class EnrolledEntrantsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private List<Entrant> entrants;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FirebaseFirestore db;

    private String eventId;
    
    private ImageButton btnExportCsv;

    public static EnrolledEntrantsFragment newInstance(@NonNull String eventId) {
        Bundle b = new Bundle();
        b.putString("eventId", eventId);      // store the event id in arguments
        EnrolledEntrantsFragment f = new EnrolledEntrantsFragment();
        f.setArguments(b);
        return f;
    }


    /**
     * Initializes Firestore, prepares the in-memory list, and retrieves the
     * event ID from fragment arguments.
     *
     * @param savedInstanceState Previously saved instance state, if any.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        entrants = new ArrayList<>();

        Bundle args = getArguments();
        eventId = args != null ? args.getString("eventId") : null;
    }

    /**
     * Inflates the UI layout, binds all view references, initializes the RecyclerView,
     * and begins loading accepted entrants in real time.
     *
     * @param inflater LayoutInflater used to inflate the layout.
     * @param container Optional parent container.
     * @param savedInstanceState Saved state, if any.
     * @return Root inflated view for this fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enrolled_entrants, container, false);

        initViews(view);
        setupRecyclerView();
        loadEnrolledEntrants();

        return view;
    }

    /**
     * Binds all view components from the inflated layout, including:
     * <ul>
     *     <li>RecyclerView for displaying accepted entrants</li>
     *     <li>ProgressBar for loading state</li>
     *     <li>Empty view message for when no entrants exist</li>
     *     <li>CSV export button (shown only when entrants exist)</li>
     * </ul>
     *
     * @param view The root inflated fragment view.
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_enrolled_entrants);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.text_empty);
        btnExportCsv = view.findViewById(R.id.btn_export_csv);
        //btnExportCsv.setOnClickListener(v -> exportCsv());
    }


    /**
     * Configures the RecyclerView with a {@link LinearLayoutManager} and an
     * {@link EntrantAdapter} in "enrolled" mode, which may modify how rows appear
     * (e.g., status badges or formatting).
     *
     * <p>A click listener can optionally be attached to handle entrant selection
     * for additional organizer actions.</p>
     */
    private void setupRecyclerView() {
        adapter = new EntrantAdapter("enrolled");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnEntrantClickListener(entrant -> {
            // Handle entrant click - you can show details or other actions
        });
    }

    /**
     * Loads all entrants who have fully accepted their spot:
     * <pre>
     * status    = "chosen"
     * responded = "accepted"
     * </pre>
     *
     * <p>This method attaches a realtime Firestore listener that:</p>
     * <ul>
     *     <li>Updates the list whenever an entrant accepts</li>
     *     <li>Maps each Firestore document into an {@link Entrant} model</li>
     *     <li>Shows/hides the CSV export button depending on list size</li>
     *     <li>Toggles empty/visible state appropriately</li>
     *     <li>Shows a spinner during initial load</li>
     * </ul>
     *
     * <p>Sorting is done by {@code joinedAt} in descending order, showing the most
     * recent accepted entrants first.</p>
     */
    private void loadEnrolledEntrants() {
        showLoading(true);

        db.collection("org_events").document(eventId)
                .collection("waiting_list")
                .whereEqualTo("status", "chosen")
                .whereEqualTo("responded", "accepted")
                .orderBy("joinedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        showLoading(false);
                        return;
                    }

                    if (snapshot != null) {
                        entrants.clear();
                        for (DocumentSnapshot doc : snapshot) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                entrant.setId(doc.getId());
                                entrants.add(entrant);
                            }
                        }

                        adapter.setEntrants(entrants);
                        updateEmptyView();
                        btnExportCsv.setVisibility(entrants.isEmpty() ? View.GONE: View.VISIBLE);
                        showLoading(false);
                    }
                });
    }

    /**
     * Shows the "empty" placeholder message when there are no enrolled entrants
     * and hides the RecyclerView. When entrants exist, the list becomes visible.
     */
    private void updateEmptyView() {
        if (entrants.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows or hides the loading ProgressBar.
     *
     * @param show {@code true} to reveal the loader, {@code false} to hide it.
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}