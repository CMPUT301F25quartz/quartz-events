package com.example.ajilore.code.ui.events;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ajilore.code.R;
import com.google.firebase.BuildConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that generates, displays, shares and saves a deep link QR code for an event.
 * Use the {@link #newInstance(String, String)} factory method to configure for a specific event.
 */
public class ManageEventQRFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Default empty constructor for fragment instantiation.
     */
    public ManageEventQRFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance using provided event details.
     *
     * @param eventId    The ID of the event to generate a QR for.
     * @param eventTitle The title of the event for display.
     * @return A new instance of ManageEventQRFragment with arguments set.
     */
    // TODO: Rename and change types and number of parameters
    public static ManageEventQRFragment newInstance(String eventId, String eventTitle) {
        ManageEventQRFragment fragment = new ManageEventQRFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_TITLE, eventTitle);
        fragment.setArguments(args);
        return fragment;
    }


    private ImageView ivQR;
    private Bitmap qrBitmap;
    private String eventId, eventTitle;

    private ImageButton btnBack;

    /**
     * Inflates the Manage Event QR layout.
     *
     * @param inflater  LayoutInflater for view creation.
     * @param container Parent ViewGroup, if any.
     * @param savedInstanceState Saved instance state, if any.
     * @return The fragment's root view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_event_q_r, container, false);
    }

    /**
     * Initializes event args, sets up the QR code, title, subtitle, and button listeners.
     *
     * @param view Root view after inflation.
     * @param savedInstanceState Saved instance state bundle, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Read the args to understand what event we are generating the QR for
        Bundle args = getArguments();
        eventId = args != null ? args.getString(ARG_EVENT_ID) : "";
        eventTitle = args != null ? args.getString(ARG_EVENT_TITLE) : "";

        TextView tvTitle = view.findViewById(R.id.tvEventTitle);
        TextView tvSub = view.findViewById(R.id.tvEventSubtitle);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(x -> requireActivity().onBackPressed());

        ivQR = view.findViewById(R.id.ivQR);

        tvTitle.setText(eventTitle);
        tvSub.setText("Scan to view event • opens in app or web");

        //Wait until ImageView has a real size and then draw a QR
        ivQR.post(() -> {
            int size = Math.min(ivQR.getWidth(), ivQR.getHeight());
            if (size <= 0) {
                //Fallback if the height is wrap-content before the image is set
                size = Math.max(ivQR.getWidth(), ivQR.getHeight());
            }
            renderQRInto(size);
        });

        //Buttons
        view.findViewById(R.id.btnShare).setOnClickListener(x -> sharePng());
        view.findViewById(R.id.btnDownload).setOnClickListener(x -> downloadPng());
    }

    /**
     * Generates a deep link URL for the current event.
     *
     * @return Event deep link URL for QR encoding.
     */
    private String buildDeepLink() {

        return "https://quartz-events.page.link/event/" + eventId;
    }


    /**
     * Generates a QR code image for the event and displays it in the ImageView.
     *
     * @param size Size (in pixels) for QR image.
     */
    private void renderQRInto(int size) {
        try {
            qrBitmap = generateQRBitmap(buildDeepLink(), size, size);
            ivQR.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to generate QR: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Uses ZXing to generate a QR code Bitmap from given string.
     *
     * @param text String to encode.
     * @param width Width in pixels.
     * @param height Height in pixels.
     * @return Bitmap QR code image.
     * @throws WriterException If QR generation fails.
     */
    private Bitmap generateQRBitmap(String text, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);

        BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : Color.WHITE);
            }
        }
        return bmp;
    }

    /**
     * Shares the QR code bitmap as a PNG using FileProvider and Android Sharesheet.
     */
    private void sharePng(){
        if(qrBitmap == null) return;

        try {
            //Write it into cache/qr/qr.png
            File dir = new File(requireContext().getCacheDir(), "qr");

            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "event_" + eventId + ".png");

            try (FileOutputStream fos = new FileOutputStream(out)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }

            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", out);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/png");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Share QR"));
        }catch (Exception e){
            Toast.makeText(requireContext(), "Failed to share QR: " + e.getMessage(), Toast.LENGTH_LONG);
        }

        }

    /**
     * Saves the QR code PNG to the user's Pictures/Quartz Events folder.
     */
    private void downloadPng(){
        if(qrBitmap == null) return;

        try{
            String name = "event_" + eventId + ".png";
            ContentValues cv = new ContentValues();

            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            cv.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Quartz Events");

            Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

            if (uri != null) {
                try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                }
                Toast.makeText(requireContext(), "Saved to Pictures/Quartz Events", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to save QR: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }
}
