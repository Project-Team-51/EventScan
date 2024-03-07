package com.example.eventscan.Fragments;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventscan.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEvent extends DialogFragment {

    public AddEvent() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_event, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button returnToEventsButton = view.findViewById(R.id.return_to_event);
        Button generateQRCodeButton = view.findViewById(R.id.generate_QRCode);



        returnToEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Close the dialog when the button is clicked
                // You can perform other actions here if needed
            }
        });

        generateQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate QR code bitmap
                String eventInfo = "placeholdereventID:123";
                Bitmap qrCodeBitmap = generateQRCode(eventInfo);

                // Inflate the dialog layout
                View dialogView = getLayoutInflater().inflate(R.layout.qr_code_dialog, null);

                // Find views in the dialog layout
                ImageView imageViewDialog = dialogView.findViewById(R.id.imageView);
                Button buttonSaveToCamera = dialogView.findViewById(R.id.buttonSave);

                // Set QR code bitmap to ImageView
                imageViewDialog.setImageBitmap(qrCodeBitmap);

                // Create and show the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.show();

                // Set click listener for the "Save to Camera Roll" button
                buttonSaveToCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveQRCodeToCameraRoll(qrCodeBitmap);
                        dialog.dismiss(); // Dismiss the dialog after saving
                    }
                });
            }
        });
    }

    private Bitmap generateQRCode(String eventInfo) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(eventInfo, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    private void showQRCodeDialog(Bitmap qrCodeBitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.qr_code_dialog, null);
        ImageView imageViewQrCode = dialogView.findViewById(R.id.imageView);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);

        imageViewQrCode.setImageBitmap(qrCodeBitmap);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement code to save the QR code to the camera roll
                saveQRCodeToCameraRoll(qrCodeBitmap);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveQRCodeToCameraRoll(Bitmap qrCodeBitmap) {
        // Get the current timestamp to generate a unique file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Define the file name
        String imageFileName = "QRCode_" + timeStamp + ".jpg";

        // Get the directory for storing images in the external storage
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Create a new file in the storage directory
        File imageFile = new File(storageDirectory, imageFileName);

        // Try to save the bitmap to the file
        try {
            // Create an output stream to write the bitmap to the file
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            // Compress the bitmap and write it to the output stream as a JPEG file
            qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // Close the output stream
            outputStream.flush();
            outputStream.close();

            // Notify the MediaScanner about the new image so that it appears in the gallery app
            MediaScannerConnection.scanFile(
                    getActivity(),
                    new String[]{imageFile.getAbsolutePath()},
                    new String[]{"image/jpeg"},
                    null
            );

            // Show a toast message indicating successful saving
            Toast.makeText(getActivity(), "QR code saved to Gallery", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // Log the error if saving fails
            Log.e(TAG, "Error saving QR code to Gallery", e);

            // Show a toast message indicating saving failure
            Toast.makeText(getActivity(), "Failed to save QR code", Toast.LENGTH_SHORT).show();
        }
    }



}