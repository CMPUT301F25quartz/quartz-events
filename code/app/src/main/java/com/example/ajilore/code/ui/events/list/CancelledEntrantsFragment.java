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
 * Fragment that displays a real-time list of entrants whose status is "cancelled".
 *
 * <p>This screen is typically used by organizers or admins to review everyone who has
 * cancelled or been removed from events. The data is sourced from the top-level
 * {@code /entrants} Firestore collection.</p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Realtime Firestore snapshot listener on cancelled entrants</li>
 *     <li>Sorted list by most recent cancellations (descending)</li>
 *     <li>Loading indicator + empty state handling</li>
 *     <li>Uses {@link EntrantAdapter} for displaying each entrant row</li>
 * </ul>
 */
public class CancelledEntrantsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private List<Entrant> entrants;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FirebaseFirestore db;

    /**
     * Initializes Firestore and creates the in-memory list used by the adapter.
     *
     * @param savedInstanceState Previously saved state, if any.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        entrants = new ArrayList<>();
    }

    /**
     * Inflates the layout, wires UI components, initializes the RecyclerView, and
     * begins loading cancelled entrants from Firestore in real-time.
     *
     * @param inflater LayoutInflater used to inflate layout XML.
     * @param container Optional parent view.
     * @param savedInstanceState Saved instance state, if any.
     * @return The root inflated view for this fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cancelled_entrants, container, false);

        initViews(view);
        setupRecyclerView();
        loadCancelledEntrants();

        return view;
    }

    /**
     * Binds all view references from the inflated layout, including:
     * <ul>
     *     <li>RecyclerView for displaying cancelled entrants</li>
     *     <li>ProgressBar for loading state</li>
     *     <li>Empty view for no results</li>
     * </ul>
     *
     * @param view The fragment's root view.
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_cancelled_entrants);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.text_empty);
    }

    /**
     * Sets up the RecyclerView with a {@link LinearLayoutManager} and an
     * {@link EntrantAdapter} configured for "cancelled" entrants.
     *
     * <p>This adapter mode may change how rows are displayed (e.g., badge color).</p>
     */
    private void setupRecyclerView() {
        adapter = new EntrantAdapter("cancelled");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnEntrantClickListener(entrant -> {
            // Handle entrant click - you can show details or other actions
        });
    }

    /**
     * Attaches a Firestore snapshot listener that retrieves all entrants
     * where {@code status = "cancelled"}, ordered by the most recent
     * cancellation date.
     *
     * <p>Realtime behavior:</p>
     * <ul>
     *     <li>Updates the RecyclerView whenever the collection changes</li>
     *     <li>Converts each Firestore document into an {@link Entrant} model</li>
     *     <li>Shows/hides loading and empty state indicators</li>
     * </ul>
     */
    private void loadCancelledEntrants() {
        showLoading(true);

        db.collection("entrants")
                .whereEqualTo("status", "cancelled")
                .orderBy("cancelledDate", Query.Direction.DESCENDING)
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
     * Toggles visibility between the empty state text and the RecyclerView
     * based on whether any cancelled entrants were loaded.
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
     * Shows or hides the ProgressBar depending on loading state.
     *
     * @param show {@code true} to display the loader, {@code false} to hide it.
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}