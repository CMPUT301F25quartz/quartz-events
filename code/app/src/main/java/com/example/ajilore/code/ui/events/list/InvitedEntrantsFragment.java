package com.example.ajilore.code.ui.events.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ajilore.code.R;
import com.example.ajilore.code.adapters.EntrantAdapter;
import com.example.ajilore.code.models.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
/**
 * InvitedEntrantsFragment
 *
 * <p>Displays a realtime list of entrants whose status is <b>"invited"</b>.
 * These users have been invited to an event but have not yet responded.</p>
 *
 * <p>This fragment reads from the top-level Firestore collection:</p>
 * <pre>
 * entrants (collection)
 *    ├── {uid} (document)
 *    │     ├── status = "invited"
 *    │     ├── invitedDate : Timestamp
 * </pre>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Realtime Firestore listener for invited entrants</li>
 *     <li>Sorted by {@code invitedDate} in descending order</li>
 *     <li>Displays results using {@link EntrantAdapter} in "invited" mode</li>
 *     <li>Handles empty state messaging and loading spinner visibility</li>
 *     <li>Supports future expansion for detailed entrant actions</li>
 * </ul>
 *
 * <p>This screen is typically used by event organizers reviewing who has been
 * formally invited but has not yet accepted or declined their participation.</p>
 */
public class InvitedEntrantsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private List<Entrant> entrants;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FirebaseFirestore db;

    /**
     * Initializes Firestore and the in-memory entrant list.
     *
     * @param savedInstanceState Previously saved instance state, if any.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        entrants = new ArrayList<>();
    }

    /**
     * Inflates the fragment layout, binds all view references, sets up the
     * RecyclerView, and starts the realtime "invited" entrant listener.
     *
     * @param inflater LayoutInflater for inflating the XML.
     * @param container Optional parent container.
     * @param savedInstanceState Saved instance state bundle, if any.
     * @return The root inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invited_entrants, container, false);

        initViews(view);
        setupRecyclerView();
        loadInvitedEntrants();

        return view;
    }

    /**
     * Binds UI components from the layout:
     * <ul>
     *     <li>RecyclerView for displaying invited entrants</li>
     *     <li>ProgressBar for loading state</li>
     *     <li>TextView for empty list messaging</li>
     * </ul>
     *
     * @param view Root fragment view.
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_invited_entrants);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.text_empty);
    }

    /**
     * Configures the RecyclerView with a {@link LinearLayoutManager} and an
     * {@link EntrantAdapter} in "invited" mode. This mode may adjust how the
     * adapter displays status indicators or row content.
     *
     * <p>A click listener may later be used to support organizer actions such as
     * viewing entrant details or sending reminders.</p>
     */
    private void setupRecyclerView() {
        adapter = new EntrantAdapter("invited");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnEntrantClickListener(entrant -> {
            // Handle entrant click - you can show details or other actions
        });
    }

    /**
     * Loads all entrants whose Firestore status is exactly <b>"invited"</b>.
     *
     * <p>Attaches a realtime Firestore snapshot listener that:</p>
     * <ul>
     *     <li>Automatically updates the list when new people are invited</li>
     *     <li>Maps each document into an {@link Entrant} model</li>
     *     <li>Sorts by {@code invitedDate} (most recent first)</li>
     *     <li>Toggles loading and empty-state views</li>
     * </ul>
     *
     * <p>Because this uses {@code addSnapshotListener}, the UI stays synced with
     * Firestore in real time.</p>
     */
    private void loadInvitedEntrants() {
        showLoading(true);

        db.collection("entrants")
                .whereEqualTo("status", "invited")
                .orderBy("invitedDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        showLoading(false);
                        return;
                    }

                    if (snapshot != null) {
                        entrants.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                entrant.setId(doc.getId());
                                entrants.add(entrant);
                            }
                        }

                        adapter.setEntrants(entrants);
                        updateEmptyView();
                        showLoading(false);
                    }
                });
    }

    /**
     * Shows or hides the "empty" placeholder text depending on whether any
     * invited entrants exist. Also toggles the RecyclerView accordingly.
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
     * Shows or hides the ProgressBar that indicates the list is loading.
     *
     * @param show {@code true} to display the spinner, {@code false} to hide it.
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}