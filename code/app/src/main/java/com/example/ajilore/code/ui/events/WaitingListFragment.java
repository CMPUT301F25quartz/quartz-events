package com.example.ajilore.code.ui.events;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ajilore.code.R;
import com.example.ajilore.code.ui.events.data.Entrant;
import com.example.ajilore.code.ui.events.list.WaitingListAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the waiting list for an event.
 * Use the {@link #newInstance(String)} factory method to create an instance with the given event ID.
 */
public class WaitingListFragment extends Fragment {
    private static final String ARG_EVENT_ID = "eventId";

    private RecyclerView rvEntrants;
    private TextView tvTotal, tvAccepted, tvDeclined, tvPending, tvEmpty;
    private EditText etSearch;
    private WaitingListAdapter adapter;
    private List<Entrant> entrantList = new ArrayList<>();

    // NEW: Store original list for filtering (added by Kulnoor)
    private List<Entrant> originalEntrantList = new ArrayList<>(); // Store original list for filtering
    private FirebaseFirestore db;
    private String eventId;

    private ImageButton btnBack;

    // // NEW: Adding filter button and filter state (added by Kulnoor)
    private ImageButton btnFilter;
    private String currentFilter = "ALL"; // NEW: Current filter state: "ALL", "ACCEPTED", "DECLINED", "PENDING"

    private ImageButton btnExportCsv;

    /**
     * Default public constructor for {@link WaitingListFragment}.
     * Required by the system for fragment instantiation.
     */
    public WaitingListFragment() {}


    /**
     * Factory method to create a new instance of {@link WaitingListFragment}
     * using the provided event ID parameter.
     *
     * @param eventId The unique identifier for the event.
     * @return A new instance of fragment {@link WaitingListFragment}.
     */
    public static WaitingListFragment newInstance(String eventId) {
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        WaitingListFragment fragment = new WaitingListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to inflate the fragment's view hierarchy.
     *
     * @param inflater The LayoutInflater object for inflating views.
     * @param container The parent view (if any).
     * @param savedInstanceState Previous saved state (if any).
     * @return The root view of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_list, container, false);
    }

    /**
     * Called when the fragment's view has been created.
     * Initializes UI components, sets up listeners, and populates the waiting list.
     *
     * @param view The root view returned by {@link #onCreateView}.
     * @param savedInstanceState Previous saved state (if any).
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;
        if (eventId == null) {
            Toast.makeText(requireContext(), "Missing event ID", Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed();
            return;
        }

        db = FirebaseFirestore.getInstance();

        rvEntrants = view.findViewById(R.id.rvEntrants);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvAccepted = view.findViewById(R.id.tvAccepted);
        tvDeclined = view.findViewById(R.id.tvDeclined);
        tvPending = view.findViewById(R.id.tvPending);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        etSearch = view.findViewById(R.id.etSearch);
        btnExportCsv = view.findViewById(R.id.btn_export_csv);
        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(v -> exportAcceptedToCsv());
        }


        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(x -> requireActivity().onBackPressed());

        // NEW: Initialize filter button as image button
        btnFilter = view.findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterOptions());
        }

        rvEntrants.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WaitingListAdapter(requireContext(), entrantList);
        rvEntrants.setAdapter(adapter);

        // NEW: Load and listen for real-time updates

        loadAndListenForUpdates();

        etSearch.addTextChangedListener(new TextWatcher() {
            /**
             * Called after the text is changed in the search EditText.
             * Filters the entrants list based on the query.
             *
             * @param s The text after it has changed.
             */

            @Override
            public void afterTextChanged(Editable s) {
                // CHANGED: Use applyFilters instead of direct filter (added by Kulnoor)
                applyFilters();
            }
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        });
    }

    // NEW: Method to load data and set up real-time listener
    /**
     * Subscribes to real-time updates on the waiting list for this event and
     * rebuilds the entrant list whenever changes occur.
     * <p>
     * For each document in the {@code waiting_list} subcollection, this method
     * derives a display status, fetches the corresponding user document to get
     * their name and profile picture, updates the entrant list, and refreshes
     * the summary statistics.
     */
    private void loadAndListenForUpdates() {
        db.collection("org_events").document(eventId)
                .collection("waiting_list")
                .addSnapshotListener((snap, err) -> {
                    entrantList.clear();
                    int total = 0, accepted = 0, declined = 0, pending = 0;

                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String uid = d.getId();
                            //String name = d.getString("name");
                            String status = d.getString("status");
                            String responded = d.getString("responded");

                            String displayStatus;
                            if ("chosen".equalsIgnoreCase(status) && "pending".equalsIgnoreCase(responded)) {
                                displayStatus = "Pending";
                            } else if ("selected".equalsIgnoreCase(status) && "accepted".equalsIgnoreCase(responded)) {
                                displayStatus = "Accepted";
                            } else if ("waiting".equalsIgnoreCase(status) && "declined".equalsIgnoreCase(responded)) {
                                displayStatus = "Declined";
                            } else if ("waiting".equalsIgnoreCase(status)) {
                                displayStatus = "Waiting";
                            } else {
                                displayStatus = responded != null ? responded : "Pending";
                            }

                            String finalDisplayStatus = displayStatus;

                            // NEW: Fetch user's name and update entrant in real-time (added by Kulnoor)
                            db.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        String userName = userDoc.getString("name");
                                        if (userName == null || userName.isEmpty()) {
                                            userName = uid; // just use the device id
                                        }
                                        String photoUrl = userDoc.getString("profilepicture");
                                        Entrant newEntrant = new Entrant(uid, userName, finalDisplayStatus, photoUrl);

                                        // NEW: Update existing entrant or add new one (added by Kulnoor)
                                        updateEntrantList(newEntrant);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle case where user doc doesn't exist
                                        Entrant newEntrant = new Entrant(uid, uid, finalDisplayStatus,null);
                                        updateEntrantList(newEntrant);
                                    });
                        }

                        // Update statistics in real-time
                        updateStatsFromFirestore(snap);
                    }
                });
    }

    // Method to update the entrant lists in real-time

    /**
     * Updates the in-memory entrant lists with a new or modified entrant and reapplies filters.
     * <p>
     * Any existing entries with the same UID are removed from both the visible and
     * original lists, then the new entrant is added to the original list before
     * calling {@link #applyFilters()}.
     *
     * @param newEntrant the entrant to insert or update
     */
    private void updateEntrantList(Entrant newEntrant) {
        // NEW: Remove existing entrant with same UID if it exists (added by Kulnoor)
        entrantList.removeIf(entrant -> entrant.uid.equals(newEntrant.uid));
        originalEntrantList.removeIf(entrant -> entrant.uid.equals(newEntrant.uid));

        // NEW: Add the updated entrant (added by Kulnoor)
        originalEntrantList.add(newEntrant);

        //  Apply current filter to update visible list
        applyFilters();
    }

    //  Method to update statistics from Firestore snapshot

    /**
     * Recomputes and displays aggregate statistics for the waiting list.
     * <p>
     * Iterates over the snapshot of {@code waiting_list} documents, interprets
     * the logical status (accepted, declined, pending, etc.), counts each category,
     * and updates the summary TextViews and CSV export button visibility.
     *
     * @param snap the Firestore snapshot of the event's waiting list
     */

    private void updateStatsFromFirestore(com.google.firebase.firestore.QuerySnapshot snap) {
        int total = 0, accepted = 0, declined = 0, pending = 0;

        for (DocumentSnapshot d : snap.getDocuments()) {
            String status = d.getString("status");
            String responded = d.getString("responded");

            String displayStatus;
            if ("chosen".equalsIgnoreCase(status) && "pending".equalsIgnoreCase(responded)) {
                displayStatus = "Pending";
            } else if ("selected".equalsIgnoreCase(status) && "accepted".equalsIgnoreCase(responded)) {
                displayStatus = "Accepted";
            } else if ("waiting".equalsIgnoreCase(status) && "declined".equalsIgnoreCase(responded)) {
                displayStatus = "Declined";
            } else if ("waiting".equalsIgnoreCase(status)) {
                displayStatus = "Waiting";
            } else {
                displayStatus = responded != null ? responded : "Pending";
            }

            String safeResponded = responded != null ? responded.toLowerCase() : "pending";

            switch (safeResponded) {
                case "accepted": accepted++; break;
                case "declined": declined++; break;
                case "waiting" :
                    pending++; break;
                default: pending++; break;
            }
        }

        total = snap.size();

        // Update UI with new statistics
        tvTotal.setText(total + " Total");
        tvAccepted.setText(accepted + " Accepted");
        tvDeclined.setText(declined + " Declined");
        tvPending.setText(pending + " Pending");

        if (btnExportCsv != null) {
            btnExportCsv.setVisibility(accepted > 0 ? View.VISIBLE : View.GONE);
        }
    }

    //  Separate method to update adapter
    /**
     * Refreshes the adapter using the current search and filter settings.
     * <p>
     * This is a thin wrapper that simply delegates to {@link #applyFilters()}.
     */
    private void updateAdapter() {
        applyFilters();
    }

    // Method to show filter options dialog

    /**
     * Displays a dialog allowing the user to filter entrants by status.
     * <p>
     * Updates the {@code currentFilter} based on the selected option
     * (All, Accepted, Declined, Pending) and reapplies filters to the list.
     */
    private void showFilterOptions() {
        String[] filters = {"All", "Accepted/Enrolled", "Declined/Cancelled", "Pending/Invited"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter By Status")
                .setItems(filters, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // CHANGED: Set current filter to ALL (added by Kulnoor)
                            currentFilter = "ALL";
                            break;
                        case 1:
                            // CHANGED: Set current filter to ACCEPTED (added by Kulnoor)
                            currentFilter = "ACCEPTED";

                            break;
                        case 2:
                            // CHANGED: Set current filter to DECLINED (added by Kulnoor)
                            currentFilter = "DECLINED";
                            break;
                        case 3:
                            // CHANGED: Set current filter to PENDING (added by Kulnoor)
                            currentFilter = "PENDING";
                            break;
                    }
                    applyFilters();
                })
                .show();
    }

    //  Method to apply both search and filter
    /**
     * Applies the current text search and status filter to the entrant list,
     * then updates the adapter and empty-state UI.
     * <p>
     * This method uses {@link #originalEntrantList} as the source of truth,
     * and filters by name/UID and by logical status (accepted, declined, pending, etc.).
     */
    private void applyFilters() {
        List<Entrant> filtered = new ArrayList<>();
        String searchTerm = etSearch.getText().toString().toLowerCase();

        // NEW: Loop through original list and apply both search and filter (added by Kulnoor)
        for (Entrant entrant : originalEntrantList) {
            //Check if search matches
            boolean matchesSearch = searchTerm.isEmpty() ||
                    entrant.nameOrUid.toLowerCase().contains(searchTerm);

            //Check if filter matches (added by Kulnoor)
            boolean matchesFilter = true;
            switch (currentFilter) {
                case "ACCEPTED":
                    matchesFilter = "Accepted".equalsIgnoreCase(entrant.displayStatus);
                    break;
                case "DECLINED":
                    matchesFilter = "Declined".equalsIgnoreCase(entrant.displayStatus);
                    break;
                case "PENDING":
                    matchesFilter = "Pending".equalsIgnoreCase(entrant.displayStatus) ||
                            "Waiting".equalsIgnoreCase(entrant.displayStatus);
                    break;
                case "ALL":
                default:
                    matchesFilter = true;
                    break;
            }

            //  Only add if both search and filter match
            if (matchesSearch && matchesFilter) {
                filtered.add(entrant);
            }
        }

        adapter.updateList(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Filters {@link #entrantList} based on the search query and updates the adapter.
     *
     * @param query The search string to filter entrants by.
     */
    private void filterEntrants(String query) {
        // CHANGED: Now calls applyFilters instead of direct filter (added by Kulnoor)
        applyFilters();
    }


    /**
     * Exports the list of accepted entrants for this event as a CSV file.
     * <p>
     * On Android Q and above, the file is written directly to the public
     * Downloads directory via {@link android.provider.MediaStore}. On earlier
     * versions, the CSV is written to the app cache and shared via an
     * {@link android.content.Intent#ACTION_SEND} intent.
     */
    private void exportAcceptedToCsv() {
        // 1) collect all accepted entrants for this event from the source list
        List<Entrant> accepted = new ArrayList<>();
        for (Entrant e : originalEntrantList) {
            if ("Accepted".equalsIgnoreCase(e.displayStatus)) {
                accepted.add(e);
            }
        }

        if (accepted.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No accepted entrants to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2) build CSV content
        StringBuilder csv = new StringBuilder();
        csv.append("Name,UID\n");
        for (Entrant entrant : accepted) {
            String name = safeCsv(entrant.nameOrUid);
            String uid  = safeCsv(entrant.uid);
            csv.append("\"").append(name).append("\",")
                    .append("\"").append(uid).append("\"\n");
        }
        String csvContent = csv.toString();

        String fileName = "final_list_" + (eventId != null ? eventId : "entrants") + ".csv";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // 3a) API 29+ : save into public Downloads via MediaStore
            try {
                android.content.ContentResolver resolver = requireContext().getContentResolver();
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOWNLOADS);

                android.net.Uri collection =
                        android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI;

                android.net.Uri item = resolver.insert(collection, values);
                if (item == null) {
                    Toast.makeText(requireContext(),
                            "Failed to create download file.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try (java.io.OutputStream os = resolver.openOutputStream(item)) {
                    os.write(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                Toast.makeText(requireContext(),
                        "Saved to Downloads as " + fileName,
                        Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(getContext(),
                        "Error saving CSV: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            // 3b) API 24–28 : fall back to cache + share
            try {
                java.io.File dir = new java.io.File(requireContext().getCacheDir(), "qr");
                if (!dir.exists()) dir.mkdirs();

                java.io.File file = new java.io.File(dir, fileName);
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    fos.write(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        file
                );

                android.content.Intent share = new android.content.Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/csv");
                share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
                share.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(android.content.Intent.createChooser(
                        share, "Export final list of entrants CSV"));

            } catch (Exception e) {
                Toast.makeText(getContext(),
                        "Error exporting CSV: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Escapes a string so it can be safely embedded in a CSV field.
     * <p>
     * Currently this replaces double quotes with doubled double quotes,
     * which is the standard CSV escaping rule.
     *
     * @param value the raw string value to escape; may be {@code null}
     * @return a non-null, escaped string safe for CSV output
     */
    private String safeCsv(String value) {
        if(value == null) return "";
        return value.replace("\"", "\"\"");
    }

}