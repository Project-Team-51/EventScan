package com.example.eventscan.Fragments;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventscan.Activities.UserSelection;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Helpers.QrCodec;
import com.example.eventscan.Entities.Organizer;
import com.example.eventscan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
import java.util.Random;
import java.util.UUID;

public class AddEvent extends DialogFragment {

    private Event event;
    private Organizer organizer;
    private ImageView imageView;
    private Uri posterUri;
    private String eventID;
    private FirebaseFirestore db;
    private String deviceID;

    public AddEvent() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            deviceID = getArguments().getString("DEVICE_ID");
        }
        event = new Event();
        View view = inflater.inflate(R.layout.add_event, container, false);
        EditText eventName = view.findViewById(R.id.add_edit_event_Name);
        EditText eventDesc = view.findViewById(R.id.addEventDescription);
        String name = eventName.getText().toString();
        String description = eventDesc.getText().toString();
        eventID = getRandomNumberString();

        event.setEventID(eventID);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        Button returnToEventsButton = view.findViewById(R.id.return_to_event);
        Button generateQRCodeButton = view.findViewById(R.id.generate_QRCode);
        Button confirmEventButton = view.findViewById(R.id.confirmEvent);
        Button uploadPoster = view.findViewById(R.id.upload_poster);
        imageView = view.findViewById(R.id.posterView);

        uploadPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the image selection activity
                pickImageLauncher.launch("image/*");
            }
        });
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

                Bitmap qrCodeBitmap = generateQRCode(QrCodec.encodeQRString(event.getEventID()));

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
        confirmEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve event name and description
                EditText eventNameEditText = view.findViewById(R.id.add_edit_event_Name);
                EditText eventDescEditText = view.findViewById(R.id.addEventDescription);
                String eventName = eventNameEditText.getText().toString();
                String eventDesc = eventDescEditText.getText().toString();

                event.setDesc(eventDesc);
                event.setName(eventName);
                event.setPoster(posterUri);
                organizer = new Organizer(deviceID);
                event.setOrganizer(organizer);

                // Poster
                // Generate a unique filename for the poster image
                String uniqueId = UUID.randomUUID().toString(); // Generate a random UUID
                String posterFileName = "poster_" + eventName + "_" + uniqueId + ".jpg";

                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference posterRef = storageRef.child("poster_pictures").child(eventID);
                posterRef.putFile(posterUri);

                Event event = new Event(eventName, eventDesc, organizer, posterUri, eventID);
                // Call the addEvent function with the retrieved information
                db.collection("events").document(event.getEventID()).set(event)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Event successfully added to Firestore");
                                getActivity().finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding event to Firestore", e);

                            }
                        });

            }
        });
    }

    private Bitmap generateQRCode(String eventID) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(eventID, BarcodeFormat.QR_CODE, 500, 500);
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

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    private void saveQRCodeToCameraRoll(Bitmap qrCodeBitmap) {
        // Get the current timestamp to generate a unique file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        //  file name
        String imageFileName = "QRCode_" + timeStamp + ".jpg";

        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File imageFile = new File(storageDirectory, imageFileName);

        // Try to save the bitmap to the file
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            // Compress the bitmap and write it to the output stream as a JPEG file
            qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

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



    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        posterUri = result;
                        imageView.setImageURI(result);
                    }

                }
            });

}