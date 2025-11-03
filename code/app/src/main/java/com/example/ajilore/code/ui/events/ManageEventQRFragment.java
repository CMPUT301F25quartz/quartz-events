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
 * A simple {@link Fragment} subclass.
 * Use the {@link ManageEventQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageEventQRFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ManageEventQRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventId    Parameter 1.
     * @param eventTitle Parameter 2.
     * @return A new instance of fragment ManageEventQRFragment.
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_event_q_r, container, false);
    }

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
     * Build a deep link for this event and share it
     */
    private String buildDeepLink() {
        return "https://quartz-events.page.link/event/" + eventId;
    }


    /**
     * Generate and display the QR for this event
     *
     * @param size The size of the QR in pixels
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
     * Turn a string into a QR bitmap using ZXing
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
     * Share the QR as a PNG via FileProvider
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
     * Save the QR into the user's photo's
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
