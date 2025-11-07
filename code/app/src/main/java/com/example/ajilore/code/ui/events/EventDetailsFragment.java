package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ajilore.code.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment for displaying event details and managing waiting list membership:
 * <ul>
 *   <li>Join/leave waiting list</li>
 *   <li>View total entrants count</li>
 *   <li>Sign up from event details</li>
 *   <li>View lottery selection process</li>
 * </ul>
 */
public class EventDetailsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";
    private static final String ARG_USER_ID = "userId";

    private String eventId;
    private String eventTitle;
    private String userId;

    private FirebaseFirestore db;

    // Views
    private ImageView ivPoster;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvDate;
    private TextView tvLocation;
    private TextView tvPrice;
    private TextView tvCapacity;
    private TextView tvWaitingListCount;
    private TextView tvRegistrationWindow;
    private TextView tvLotteryInfo;
    private Button btnJoinLeave;
    private ProgressBar progressBar;
    private View layoutWaitingListInfo;

    private boolean isOnWaitingList = false;
    private boolean isRegistrationOpen = false;
    private int waitingListCount = 0;
    private int capacity = 0;

    /**
     * Factory for creating a new instance of this fragment for a specific event and user.
     *
     * @param eventId   The unique event document ID.
     * @param title     The event title (for display).
     * @param userId    The user ID to check waiting list status for.
     * @return Configured EventDetailsFragment instance.
     */
    public static EventDetailsFragment newInstance(String eventId, String title, String userId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_TITLE, title);
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }
    /**
     * Inflate the event details layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    /**
     * Initializes fields, fetches arguments, sets up click handlers and loads event data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID);
            eventTitle = args.getString(ARG_EVENT_TITLE);
            userId = args.getString(ARG_USER_ID);
        }

        // Initialize views
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ivPoster = view.findViewById(R.id.ivEventPoster);
        tvTitle = view.findViewById(R.id.tvEventTitle);
        tvDescription = view.findViewById(R.id.tvEventDescription);
        tvDate = view.findViewById(R.id.tvEventDate);
        tvLocation = view.findViewById(R.id.tvEventLocation);
        tvPrice = view.findViewById(R.id.tvEventPrice);
        tvCapacity = view.findViewById(R.id.tvEventCapacity);
        tvWaitingListCount = view.findViewById(R.id.tvWaitingListCount);
        tvRegistrationWindow = view.findViewById(R.id.tvRegistrationWindow);
        tvLotteryInfo = view.findViewById(R.id.tvLotteryInfo);
        btnJoinLeave = view.findViewById(R.id.btnJoinLeaveWaitingList);
        progressBar = view.findViewById(R.id.progressBar);
        layoutWaitingListInfo = view.findViewById(R.id.layoutWaitingListInfo);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Load event details
        loadEventDetails();

        // Check waiting list status
        checkWaitingListStatus();

        // Setup button click
        btnJoinLeave.setOnClickListener(v -> handleWaitingListAction());
    }

    /**
     * Handles toggling user waiting list membership, calling the join/leave method as needed.
     */
    private void loadEventDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("org_events")
                .document(eventId)
                .addSnapshotListener((DocumentSnapshot doc, FirebaseFirestoreException e) -> {
                    progressBar.setVisibility(View.GONE);

                    if (e != null) {
                        Toast.makeText(requireContext(),
                                "Failed to load event: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        // Basic info
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String location = doc.getString("location");
                        Double price = doc.getDouble("price");
                        Long capacityLong = doc.getLong("capacity");
                        String posterKey = doc.getString("posterUrl");

                        // Timestamps
                        Timestamp startsAt = doc.getTimestamp("startsAt");
                        Timestamp regStartTime = doc.getTimestamp("registrationStartTime");
                        Timestamp regEndTime = doc.getTimestamp("registrationEndTime");

                        // Update UI
                        tvTitle.setText(title != null ? title : "Untitled Event");
                        tvDescription.setText(description != null ? description : "No description available");
                        tvLocation.setText(location != null ? location : "Location TBA");

                        if (price != null) {
                            tvPrice.setText(String.format("$%.2f", price));
                        } else {
                            tvPrice.setText("Free");
                        }

                        if (capacityLong != null) {
                            capacity = capacityLong.intValue();
                            tvCapacity.setText("Capacity: " + capacity + " participants");
                        } else {
                            tvCapacity.setText("Capacity: Not specified");
                        }

                        if (startsAt != null) {
                            String dateStr = DateFormat.getDateTimeInstance(
                                            DateFormat.LONG, DateFormat.SHORT)
                                    .format(startsAt.toDate());
                            tvDate.setText(dateStr);
                        } else {
                            tvDate.setText("Date TBA");
                        }

                        // Registration window
                        Date now = new Date();
                        isRegistrationOpen = isRegistrationOpen(regStartTime, regEndTime, now);
                        updateRegistrationWindow(regStartTime, regEndTime, now);

                        // Set poster
                        //ivPoster.setImageResource(mapPoster(posterKey));
                        if (posterKey != null && posterKey.startsWith("http")){
                            //This means that its a cloudinary image
                            Glide.with(this).load(posterKey).into(ivPoster);
                        } else {
                            ivPoster.setImageResource(mapPoster(posterKey));
                        }
                        // US 01.05.05: Display lottery selection criteria
                        displayLotteryInfo();

                        // Load waiting list count
                        loadWaitingListCount();
                    }
                });
    }

    /**
     * Checks if the current user is on the waiting list and updates the button.
     */
    private void checkWaitingListStatus() {
        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId)
                .addSnapshotListener((DocumentSnapshot doc, FirebaseFirestoreException e) -> {
                    if (e != null) {
                        return;
                    }

                    isOnWaitingList = (doc != null && doc.exists());
                    updateJoinLeaveButton();
                });
    }

    /**
     * Loads and updates the displayed waiting list count.
     */
    private void loadWaitingListCount() {
        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .addSnapshotListener((QuerySnapshot snapshot, FirebaseFirestoreException e) -> {
                    if (e != null) {
                        return;
                    }

                    if (snapshot != null) {
                        waitingListCount = snapshot.size();
                        tvWaitingListCount.setText("Total Entrants: " + waitingListCount);
                        layoutWaitingListInfo.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * Handles toggling user waiting list membership, calling the join/leave method as needed.
     */
    private void handleWaitingListAction() {
        if (!isRegistrationOpen) {
            Toast.makeText(requireContext(),
                    "Registration is not currently open",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnJoinLeave.setEnabled(false);

        if (isOnWaitingList) {
            // Leave waiting list
            leaveWaitingList();
        } else {
            // Join waiting list
            joinWaitingList();
        }
    }

    /**
     * Writes the current user to the event's waiting list in Firestore.
     */
    private void joinWaitingList() {
        DocumentReference waitingListRef = db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId);

        Map<String, Object> entrant = new HashMap<>();
        entrant.put("userId", userId);
        entrant.put("joinedAt", FieldValue.serverTimestamp());
        entrant.put("status", "waiting");
        entrant.put("responded", null);

        waitingListRef.set(entrant)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(),
                            "Successfully joined waiting list!",
                            Toast.LENGTH_SHORT).show();
                    btnJoinLeave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to join: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnJoinLeave.setEnabled(true);
                });
    }

    /**
     * Removes the current user from the event's waiting list in Firestore.
     */
    private void leaveWaitingList() {
        db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(),
                            "Successfully left waiting list",
                            Toast.LENGTH_SHORT).show();
                    btnJoinLeave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to leave: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnJoinLeave.setEnabled(true);
                });
    }

    /**
     * Updates the join/leave button text and enabled state based on user registration and waiting list status.
     */
    private void updateJoinLeaveButton() {
        if (isOnWaitingList) {
            btnJoinLeave.setText("Leave Waiting List");
            btnJoinLeave.setBackgroundColor(0xFFFF6B6B); // Red
        } else {
            btnJoinLeave.setText("Join Waiting List");
            btnJoinLeave.setBackgroundColor(0xFF17C172); // Green
        }

        btnJoinLeave.setEnabled(isRegistrationOpen);
    }

    /**
     * Updates registration window message and color based on status and timing.
     */
    private void updateRegistrationWindow(Timestamp regStart, Timestamp regEnd, Date now) {
        if (regStart == null || regEnd == null) {
            tvRegistrationWindow.setText("Registration: Always open");
            return;
        }

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        String startStr = df.format(regStart.toDate());
        String endStr = df.format(regEnd.toDate());

        if (isRegistrationOpen) {
            tvRegistrationWindow.setText("Registration closes: " + endStr);
            tvRegistrationWindow.setTextColor(0xFF17C172); // Green
        } else if (now.before(regStart.toDate())) {
            tvRegistrationWindow.setText("Registration opens: " + startStr);
            tvRegistrationWindow.setTextColor(0xFFFFA500); // Orange
        } else {
            tvRegistrationWindow.setText("Registration closed on: " + endStr);
            tvRegistrationWindow.setTextColor(0xFFFF6B6B); // Red
        }
    }

    /**
     * Sets the UI text for the lottery information section.
     */
    private void displayLotteryInfo() {
        String lotteryText = "Lottery Selection Process:\n\n" +
                "• After registration closes, participants will be randomly selected\n" +
                "• Selected participants will receive a notification\n" +
                "• You must accept the invitation within the given timeframe\n" +
                "• If someone declines, another participant will be drawn\n" +
                "• Everyone on the waiting list has an equal chance of being selected";

        tvLotteryInfo.setText(lotteryText);
    }

    /**
     * Checks if event registration is currently open according to window.
     *
     * @param regStart Registration window start timestamp.
     * @param regEnd   Registration window end timestamp.
     * @param now      Current date/time.
     * @return true if now falls within [regStart, regEnd], or always open if inputs are null.
     */
    private boolean isRegistrationOpen(Timestamp regStart, Timestamp regEnd, Date now) {
        if (regStart == null || regEnd == null) {
            return true;
        }
        Date start = regStart.toDate();
        Date end = regEnd.toDate();
        return now.after(start) && now.before(end);
    }

    /**
     * Maps a poster key to a drawable resource for the event graphic.
     *
     * @param key Poster key string
     * @return Drawable resource ID
     */
    private int mapPoster(String key) {
        if (key == null) return R.drawable.jazz;
        switch (key) {
            case "jazz": return R.drawable.jazz;
            case "band": return R.drawable.jazz;
            case "jimi": return R.drawable.jazz;
            case "gala": return R.drawable.jazz;
            default: return R.drawable.jazz;
        }
    }
}