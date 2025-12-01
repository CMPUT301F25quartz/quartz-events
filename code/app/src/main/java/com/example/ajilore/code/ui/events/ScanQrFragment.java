package com.example.ajilore.code.ui.events;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ajilore.code.R;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;


/**
 * {@code ScanQrFragment} launches the ZXing QR scanner and handles QR-based
 * navigation inside the Quartz Events app.
 *
 * <p>The fragment performs the following:</p>
 * <ul>
 *     <li>Registers a ZXing {@link ScanContract} using the Activity Result API</li>
 *     <li>Launches a QR code scanning UI as soon as the fragment appears</li>
 *     <li>Parses scanned text to extract a Quartz deep link:
 *         {@code https://quartz-events.page.link/event/{eventId}}</li>
 *     <li>Navigates to {@link EventDetailsFragment} when a valid event QR is scanned</li>
 *     <li>Handles cancellation and invalid QR codes gracefully with Toast feedback</li>
 * </ul>
 *
 * <p>This fragment is opened from the Events list using the “Scan QR” button.
 * It occupies a blank screen with only a Back button, as the scanner UI is full-screen.</p>
 */

public class ScanQrFragment extends Fragment {

    private ActivityResultLauncher<ScanOptions> scanLauncher;

    public ScanQrFragment() { }

    /**
     * Registers a QR scanner launcher using ZXing's {@link ScanContract}.
     *
     * <p>Behavior:</p>
     * <ul>
     *     <li>If the user cancels the scan → a Toast is shown and the fragment closes</li>
     *     <li>If content is scanned → the text is passed to {@link #handleScannedText(String)}</li>
     * </ul>
     *
     * @param savedInstanceState Previously saved state (unused)
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scanLauncher = registerForActivityResult(new ScanContract(), (ScanIntentResult result) -> {
            if (result.getContents() == null) {
                Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                String contents = result.getContents();
                handleScannedText(contents);
            }
        });
    }
    /**
     * Inflates the layout for the Scan QR screen.
     * Note: The visible UI is minimal because the ZXing scanner overlays the screen.
     *
     * @param inflater LayoutInflater used to inflate the XML
     * @param container Optional parent container
     * @param savedInstanceState Previously saved instance state, if any
     * @return The root view for the fragment
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_qr, container, false);
    }

    /**
     * Sets up the Back button and immediately launches the ZXing QR scanner.
     *
     * <p>The scanner is configured with:</p>
     * <ul>
     *     <li>QR-only format</li>
     *     <li>Prompt text</li>
     *     <li>Beep enabled</li>
     *     <li>Orientation locked</li>
     * </ul>
     *
     * @param view Root view of the fragment
     * @param s Saved instance state bundle, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);

        ImageButton btnBack = view.findViewById(R.id.btnQRBack);
        btnBack.setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan event QR");
        options.setBeepEnabled(true);
        options.setCameraId(0);
        options.setOrientationLocked(true);
        options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class);

        scanLauncher.launch(options);
    }

    /**
     * Handles decoded QR text from the scanner.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Validates the scanned text</li>
     *     <li>Attempts to extract an event ID via {@link #extractEventIdFromLink(String)}</li>
     *     <li>Shows an error Toast if the QR is not a Quartz deep link</li>
     *     <li>Navigates to {@link EventDetailsFragment} when valid</li>
     * </ul>
     *
     * @param contents The raw scanned QR string
     */
    private void handleScannedText(String contents) {
        if (contents == null || contents.isEmpty()) return;

        String eventId = extractEventIdFromLink(contents);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Not a Quartz event QR", Toast.LENGTH_LONG).show();
            return;
        }
        openEventDetails(eventId);
    }

    /**
     * Navigates to the Event Details screen for the scanned event.
     *
     * @param eventId Firestore document ID under /org_events
     */
    private void openEventDetails(@NonNull String eventId) {
        Fragment fragment = EventDetailsFragment.newInstance(eventId, "");
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Parses a Quartz deep link of the form:
     * <pre>
     * https://quartz-events.page.link/event/{eventId}
     * </pre>
     *
     * @param url Scanned QR contents
     * @return The extracted eventId, or {@code null} if not a valid Quartz link
     */
    @Nullable
    private String extractEventIdFromLink(@NonNull String url) {
        String prefix = "https://quartz-events.page.link/event/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }
}