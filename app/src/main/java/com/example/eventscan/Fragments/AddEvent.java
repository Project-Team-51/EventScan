package com.example.eventscan.Fragments;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.example.eventscan.Activities.MainActivity;

import com.example.eventscan.Activities.UserSelection;


import com.example.eventscan.Database.Database;
import com.example.eventscan.Database.QRDatabaseEventLink;
import com.example.eventscan.Entities.DeviceID;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Helpers.QrCodec;
import com.example.eventscan.Entities.Organizer;
import com.example.eventscan.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * A dialog fragment that allows us to add a new event. The organizer who makes the event is designated the event owner,
 * and the new event is pushed to the Firestore where every users all events list will promptly update and add it.
 */
public class AddEvent extends DialogFragment implements AttendeeLimitDialogFragment.AttendeeLimitListener {

    private Event event;
    private Organizer organizer;
    private ImageView imageView;
    private Uri posterUri;
    private String posterUriString;
    private String eventID;
    private Database db;
    private String deviceID;

    //private boolean isSingleUse = false;

    public interface OnEventAddedListener {
        void onEventAdded();
    }
    public void setEventAddedListener(OnEventAddedListener listener) {
        this.eventAddedListener = listener;
    }
    private OnEventAddedListener eventAddedListener;
    public AddEvent() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        if (getArguments() != null) {
//            deviceID = getArguments().getString("DEVICE_ID");
//        }
        deviceID = DeviceID.getDeviceID(requireContext());
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            setEventAddedListener(mainActivity);
        }
    }
    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle)
     * has returned, but before any saved state has been restored in to the view.
     * It is called after onCreateView(LayoutInflater, ViewGroup, Bundle) and before
     * onViewStateRestored(Bundle).
     *
     * @param view               The view returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = Database.getInstance();
        Button generateQRCodeCheckInButton = view.findViewById(R.id.generate_QRCode_check_in);
        Button generateQRCodeDetailsButton = view.findViewById(R.id.generate_QRCode_see_details);
        Button confirmEventButton = view.findViewById(R.id.confirmEvent);
        Button uploadPoster = view.findViewById(R.id.upload_poster);
        Button viewCheckedInAttendees = view.findViewById(R.id.checked_in_attendees);
        Switch attendeeLimitSwitch = view.findViewById(R.id.attendeeLimit);
        imageView = view.findViewById(R.id.posterView);


        uploadPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the image selection activity
                pickImageLauncher.launch("image/*");
            }
        });


        generateQRCodeCheckInButton.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.qr_code_dialog, null);
            ImageView imageViewDialog = dialogView.findViewById(R.id.imageView);
            Button buttonSaveToCamera = dialogView.findViewById(R.id.buttonSave);
            Button buttonShareQR = dialogView.findViewById(R.id.buttonShareQRCode);
            int linkType = QRDatabaseEventLink.DIRECT_CHECK_IN;

            // setup the task to run as early as possible
            Task<Bitmap> getQRTask = QrCodec.createOrGetQR(event, linkType, false);
            getQRTask.addOnSuccessListener(bitmap -> {
                imageViewDialog.setImageBitmap(bitmap);

                buttonShareQR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/jpeg");
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] qrImageBytes = byteArrayOutputStream.toByteArray();
                        shareIntent.putExtra(Intent.EXTRA_STREAM, qrImageBytes);
                        startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
                    }
                });
            });
            // rest of fragment setup
            // Create and show the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            // Set click listener for the "Save to Camera Roll" button
            buttonSaveToCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveQRCodeToCameraRoll(getQRTask.getResult());
                    //dialog.dismiss(); // Dismiss the dialog after saving
                }
            });
        });

        generateQRCodeDetailsButton.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.qr_code_dialog, null);
            ImageView imageViewDialog = dialogView.findViewById(R.id.imageView);
            Button buttonSaveToCamera = dialogView.findViewById(R.id.buttonSave);
            Button buttonShareQR = dialogView.findViewById(R.id.buttonShareQRCode);
            int linkType = QRDatabaseEventLink.DIRECT_SEE_DETAILS;

            // setup the task to run as early as possible
            Task<Bitmap> getQRTask = QrCodec.createOrGetQR(event, linkType, false);
            getQRTask.addOnSuccessListener(bitmap -> {
                imageViewDialog.setImageBitmap(bitmap);

                buttonShareQR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/jpeg");
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] qrImageBytes = byteArrayOutputStream.toByteArray();
                        shareIntent.putExtra(Intent.EXTRA_STREAM, qrImageBytes);
                        startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
                    }
                });
            });
            // rest of fragment setup
            // Create and show the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            // Set click listener for the "Save to Camera Roll" button
            buttonSaveToCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveQRCodeToCameraRoll(getQRTask.getResult());
                    //dialog.dismiss(); // Dismiss the dialog after saving
                }
            });
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
                event.setPoster(posterUriString);

                organizer = new Organizer();
                organizer.setDeviceID(deviceID);
                event.setOrganizer(organizer);
                //event.setAttendeeLimit(10);

                Task<Event> createEventTask = Database.getInstance().events.create(event);
                createEventTask.addOnSuccessListener(event1 -> {
                    // event1 is an event that might have an updated eventID to make it unique
                    Log.d(TAG, "Event successfully added to Firestore");
                    if(eventAddedListener != null){
                        eventAddedListener.onEventAdded();
                    }
                }).addOnFailureListener(e -> {
                            Log.w(TAG,"Error adding event to firestore", e);
                });
            }
        });
        attendeeLimitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showAttendeeLimitDialog(event);
                }
            }
        });

    }



    /**
     * Displays a dialog containing the provided QR code bitmap.
     * Allows the user to save the QR code to the device's camera roll.
     *
     * @param qrCodeBitmap The QR code bitmap to be displayed.
     */
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

    /**
     * Generates a random 6-digit number as a string.
     *
     * @return A randomly generated 6-digit number as a string.
     */
    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    /**
     * Saves the provided QR code bitmap to the device's camera roll.
     *
     * @param qrCodeBitmap The QR code bitmap to be saved.
     */
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
                        posterUriString = posterUri.toString();
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference profilePicRef = storageRef.child("poster_pics").child(eventID);
                        profilePicRef.putFile(posterUri).addOnSuccessListener(taskSnapshot -> {
                                    // Image uploaded successfully
                                    Log.d("POSTER", "Image uploaded successfully");
                                })
                                .addOnFailureListener(exception -> {
                                    // Handle unsuccessful upload
                                    Log.e("POSTER", "Failed to upload image: " + exception.getMessage());
                                });
                    }
                }
            });

    /**
     * Displays a dialog allowing the user to set the maximum number of attendees for the event.
     *
     * @param event The event for which the attendee limit is being set.
     */
    private void showAttendeeLimitDialog(Event event) {
        AttendeeLimitDialogFragment dialogFragment = new AttendeeLimitDialogFragment();
        dialogFragment.setAttendeeLimitListener(this); // Set the listener
        dialogFragment.show(getChildFragmentManager(), "attendee_limit_dialog");
    }

    /**
     * Handles the attendee limit set by the user.
     *
     * @param attendeeLimit The maximum number of attendees set by the user.
     */
    public void onAttendeeLimitSet(int attendeeLimit) {
        event.setAttendeeLimit(attendeeLimit);
    }




}