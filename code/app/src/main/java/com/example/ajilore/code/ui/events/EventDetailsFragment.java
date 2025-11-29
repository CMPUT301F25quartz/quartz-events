package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.provider.Settings;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ajilore.code.MainActivity;
import com.example.ajilore.code.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

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
//    private static final String ARG_USER_ID = "userId";

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
    private MaterialButton btnJoinLeave;
    private MaterialButton btnAcceptSpot;
    private MaterialButton btnDecline;
    private ProgressBar progressBar;
    private View layoutWaitingListInfo;
    private ListenerRegistration eventListener;
    private ListenerRegistration waitingListStatusListener;
    private ListenerRegistration waitingListCountListener;


    // US 01.01.01 & 01.01.02: Track waiting list status
    private boolean isOnWaitingList = false;
    private boolean isRegistrationOpen = false;
    private boolean isSelectedForLottery = false;
    private int waitingListCount = 0;
    private int capacity = 0;

    /**
     * Factory for creating a new instance of this fragment for a specific event and user.
     *
     * @param eventId The unique event document ID.
     * @param title   The event title (for display).
     * @return Configured EventDetailsFragment instance.
     */
    public static EventDetailsFragment newInstance(String eventId, String title) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_TITLE, title);
//        args.putString(ARG_USER_ID, userId);
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
        //TEMI ADDED (REMOVED FIREBASE AUTH AND REPLACED IT WITH DEVICE ID)
        userId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

//        // Get authenticated user's ID
//        var currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show();
//            requireActivity().onBackPressed();
//            return;
//        }
//        userId = currentUser.getUid(); // Use authenticated user's ID
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID);
            eventTitle = args.getString(ARG_EVENT_TITLE);
//            userId = args.getString(ARG_USER_ID);
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
//to handle accept and decline buttons
        btnAcceptSpot = view.findViewById(R.id.btnAcceptSpot);
        btnDecline    = view.findViewById(R.id.btnDecline);
        btnAcceptSpot.setOnClickListener(v -> handleRespondToInvite(true));
        btnDecline.setOnClickListener(v -> handleRespondToInvite(false));


        //Adding this to see if i can fix the bug by using an initial default state
        isRegistrationOpen = false;
        isOnWaitingList = false;
        updateJoinLeaveButton();

        // Back button navigation
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Load event details
        loadEventDetails();
        btnJoinLeave.setVisibility(View.VISIBLE);
        // US 01.01.01 & 01.01.02: Check waiting list status
        checkWaitingListStatus();

        // Setup button click
        btnJoinLeave.setOnClickListener(v -> handleWaitingListAction());
    }

    /**
     * Handles toggling user waiting list membership, calling the join/leave method as needed.
     */
    private void loadEventDetails() {
        progressBar.setVisibility(View.VISIBLE);

        eventListener = db.collection("org_events")
                .document(eventId)
                .addSnapshotListener((DocumentSnapshot doc, FirebaseFirestoreException e) -> {
                    progressBar.setVisibility(View.GONE);

                    if (e != null) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Failed to load event: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        // Basic info
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String location = doc.getString("location");
                        Long capacityLong = doc.getLong("capacity");
                        String posterKey = doc.getString("posterUrl");

                        // Timestamps
                        Timestamp startsAt = doc.getTimestamp("startsAt");
                        Timestamp regStartTime = doc.getTimestamp("regOpens");
                        Timestamp regEndTime = doc.getTimestamp("regCloses");

                        //geolocation
                        Boolean geoRequired = doc.getBoolean("geolocationRequired");

                        Button btnViewMap = getView().findViewById(R.id.btnMap);

                        if (btnViewMap != null) {
                            if (geoRequired != null && !geoRequired) {
                                // Geolocation disabled → hide button
                                btnViewMap.setVisibility(View.GONE);
                            } else {
                                // Geolocation enabled → show button
                                btnViewMap.setVisibility(View.VISIBLE);
                            }
                        }


                        // Update UI
                        tvTitle.setText(title != null ? title : "Untitled Event");
                        tvDescription.setText(description != null ? description : "No description available");
                        tvLocation.setText(location != null ? location : "Location TBA");


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

                        updateJoinLeaveButton();
                        // Set poster
                        if (posterKey != null && !posterKey.isEmpty()) {
                            Glide.with(this)
                                    .load(posterKey)
                                    .placeholder(R.drawable.jazz)  // While loading
                                    .error(R.drawable.jazz)        // If load fails
                                    .into(ivPoster);
                        } else {
                            ivPoster.setImageResource(R.drawable.jazz);  // Default placeholder
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
        waitingListStatusListener = db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId)
                .addSnapshotListener((DocumentSnapshot doc, FirebaseFirestoreException e) -> {
                    if (e != null) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Error checking waiting list status",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        String status    = doc.getString("status");
                        String responded = doc.getString("responded");

                        // Treat cancelled / declined as NOT on the waiting list
                        boolean cancelled = "cancelled".equalsIgnoreCase(status)
                                || "declined".equalsIgnoreCase(responded);

                        isOnWaitingList = !cancelled;

                        // 🔹 chosen + not yet accepted/declined → show Accept/Decline
                        boolean chosenPending =
                                "chosen".equalsIgnoreCase(status)
                                        && !"accepted".equalsIgnoreCase(responded)
                                        && !"declined".equalsIgnoreCase(responded);

                        isSelectedForLottery = chosenPending;
                    } else {
                        isOnWaitingList = false;
                        isSelectedForLottery = false;
                    }

                    if (eventListener != null) {
                        updateJoinLeaveButton();
                    }
                });
    }



    /**
     * Loads and updates the displayed waiting list count.
     */
    private void loadWaitingListCount() {
        waitingListCountListener = db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .addSnapshotListener((QuerySnapshot snapshot, FirebaseFirestoreException e) -> {
                    if (e != null) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Error loading waiting list count",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (snapshot != null) {
                        int count = 0;
                        for (DocumentSnapshot d : snapshot.getDocuments()) {
                            String status    = d.getString("status");
                            String responded = d.getString("responded");

                            boolean cancelled = "cancelled".equalsIgnoreCase(status)
                                    || "declined".equalsIgnoreCase(responded);

                            if (!cancelled) {
                                count++;
                            }
                        }

                        waitingListCount = count;
                        tvWaitingListCount.setText("Waiting List: " + waitingListCount + "/" + capacity);
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
            // US 01.01.02: Leave waiting list - show confirmation
            showLeaveConfirmationDialog();
        } else {
            // US 01.01.01: Join waiting list
            joinWaitingList();
        }
    }

    /**
     * US 01.01.02: Show confirmation dialog before leaving waiting list
     */
    private void showLeaveConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Leave the waiting list?")
                .setMessage("By clicking the leave button, you confirm that you will forfeit your spot on the waiting list and will no longer be considered for the lottery.")
                .setPositiveButton("Leave", (dialog, which) -> leaveWaitingList())
                .setNegativeButton("Cancel", (dialog, which) -> btnJoinLeave.setEnabled(true))
                .setOnCancelListener(dialog -> btnJoinLeave.setEnabled(true))
                .show();
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

        // Log the registration to the history collection
        logRegistrationToHistory(userId, eventId, eventTitle);


        waitingListRef.set(entrant)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(),
                                "Successfully joined waiting list!",
                                Toast.LENGTH_SHORT).show();
                    }
                    btnJoinLeave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(),
                                "Failed to join: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                    btnJoinLeave.setEnabled(true);
                });
    }

    /**
     * Removes the current user from the event's waiting list in Firestore.
     */
    private void leaveWaitingList() {
        DocumentReference ref = db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId);

        // First see if this user currently has a spot (chosen/selected)
        ref.get().addOnSuccessListener(doc -> {
            String status    = doc != null ? doc.getString("status") : null;
            String responded = doc != null ? doc.getString("responded") : null;

            boolean hadSpot = "chosen".equalsIgnoreCase(status)
                    || ("selected".equalsIgnoreCase(status)
                    && "accepted".equalsIgnoreCase(responded));

            ref.delete()
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Successfully left waiting list",
                                    Toast.LENGTH_SHORT).show();
                        }

                        //  Only draw a replacement if they actually had a spot
                        if (hadSpot) {
                            SelectEntrantsFragment.drawReplacementsFromWaitingList(
                                    db,
                                    requireContext(),
                                    eventId,
                                    eventTitle,
                                    1
                            );
                        }

                        btnJoinLeave.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Failed to leave: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        btnJoinLeave.setEnabled(true);
                    });

        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(requireContext(),
                        "Failed to check waiting list: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            btnJoinLeave.setEnabled(true);
        });
    }


    /**
     * Updates the join/leave button text and enabled state based on user registration and waiting list status.
     */
    private void updateJoinLeaveButton() {

        // Default: hide accept/decline, enable them
        btnAcceptSpot.setVisibility(View.GONE);
        btnDecline.setVisibility(View.GONE);
        btnAcceptSpot.setEnabled(true);
        btnDecline.setEnabled(true);

        // If this user has been chosen and hasn't responded yet,
        // hide Join/Leave and show Accept/Decline instead.
        if (isSelectedForLottery) {
            btnJoinLeave.setVisibility(View.GONE);
            btnAcceptSpot.setVisibility(View.VISIBLE);
            btnDecline.setVisibility(View.VISIBLE);
            return; // don't let the rest of the logic override this state
        }

        // Normal behaviour: show Join/Leave, hide Accept/Decline
        btnJoinLeave.setVisibility(View.VISIBLE);

        if (!isRegistrationOpen) {
            btnJoinLeave.setText("Registration Closed");
            btnJoinLeave.setEnabled(false);
            btnJoinLeave.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.grey));
            return;
        }

        if (isOnWaitingList) {
            btnJoinLeave.setText("Leave ->");
            btnJoinLeave.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red));
        } else {
            btnJoinLeave.setText("Join Waiting List");
            btnJoinLeave.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue));
        }

        btnJoinLeave.setEnabled(true);
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
        String endStr = df.format(regEnd.toDate());

        if (isRegistrationOpen) {
            tvRegistrationWindow.setText("Registration closes: " + endStr);
            tvRegistrationWindow.setTextColor(0xFF17C172); // Green
        } else if (now.before(regStart.toDate())) {
            String startStr = df.format(regStart.toDate());
            tvRegistrationWindow.setText("Registration opens: " + startStr);
            tvRegistrationWindow.setTextColor(0xFFFFA500); // Orange
        } else {
            tvRegistrationWindow.setText("Registration closed on: " + endStr);
            tvRegistrationWindow.setTextColor(0xFFFF6B6B); // Red
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNav();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNav();
        }

        // Remove all Firestore listeners to prevent memory leaks
        if (eventListener != null) {
            eventListener.remove();
            eventListener = null;
        }
        if (waitingListStatusListener != null) {
            waitingListStatusListener.remove();
            waitingListStatusListener = null;
        }
        if (waitingListCountListener != null) {
            waitingListCountListener.remove();
            waitingListCountListener = null;
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


    // FUTURE IMPLEMENTATION - NOT PART OF CURRENT USER STORIES
    // Commented out for later sprints

    /*
    // US 01.04.01: Accept invitation (future implementation)
    private Button btnAcceptSpot;
    private Button btnDecline;

    private void handleAcceptSpot() {
        // Will be implemented when lottery system is ready
    }

    private void handleDeclineSpot() {
        // Will be implemented when lottery system is ready
    }

    */

    // response to invite
    private void handleRespondToInvite(boolean accepted) {
        if (eventId == null || userId == null) return;

        btnAcceptSpot.setEnabled(false);
        btnDecline.setEnabled(false);

        DocumentReference ref = db.collection("org_events")
                .document(eventId)
                .collection("waiting_list")
                .document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("responded", accepted ? "accepted" : "declined");
        updates.put("responseAt", FieldValue.serverTimestamp());
        updates.put("status", accepted ? "selected" : "cancelled");

        ref.set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;

                    Toast.makeText(
                            requireContext(),
                            accepted ? "You accepted your spot 🎉" : "You declined this spot",
                            Toast.LENGTH_SHORT
                    ).show();

                    // If they declined, free the spot and draw 1 replacement
                    if (!accepted) {
                        SelectEntrantsFragment.drawReplacementsFromWaitingList(
                                db,
                                requireContext(),
                                eventId,
                                eventTitle,
                                1
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;

                    btnAcceptSpot.setEnabled(true);
                    btnDecline.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "Failed to update response: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }



    //method to log the history of the events that the user has registered for in registrations collection
    private void logRegistrationToHistory(@NonNull String userId,
                                          @NonNull String eventId,
                                          @NonNull String eventTitle) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("org_events").document(eventId).get().addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) return;

                    Map<String, Object> reg = new HashMap<>();
                    reg.put("userId", userId);
                    reg.put("eventId", eventId);
                    reg.put("eventTitle", eventDoc.getString("title"));
                    reg.put("posterUrl", eventDoc.getString("posterUrl"));
                    reg.put("location", eventDoc.getString("location"));
                    reg.put("startsAt", eventDoc.getTimestamp("startsAt"));
                    reg.put("registeredAt", FieldValue.serverTimestamp());


                    db.collection("users")
                            .document(userId)
                            .collection("registrations")
                            .document(eventId)
                            .set(reg, SetOptions.merge()) // merge ensures no duplicate errors
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(),
                                            "Failed to save registration history: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to fetch event info: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}

