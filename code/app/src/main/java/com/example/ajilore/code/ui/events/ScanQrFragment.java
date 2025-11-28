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
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ScanQrFragment extends Fragment {

    private ActivityResultLauncher<ScanOptions> scanLauncher;

    public ScanQrFragment() { }

    // Register ZXing scanner
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_qr, container, false);
    }

    // Set back button and launch scanner
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

    // Handle decoded QR text → eventId → navigate
    private void handleScannedText(String contents) {
        if (contents == null || contents.isEmpty()) return;

        String eventId = extractEventIdFromLink(contents);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Not a Quartz event QR", Toast.LENGTH_LONG).show();
            return;
        }
        openEventDetails(eventId);
    }

    private void openEventDetails(@NonNull String eventId) {
        Fragment fragment = EventDetailsFragment.newInstance(eventId, "");
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Nullable
    private String extractEventIdFromLink(@NonNull String url) {
        String prefix = "https://quartz-events.page.link/event/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }
}