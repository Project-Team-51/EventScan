package com.example.eventscan.Fragments;


import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Dialog;


import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;

import com.example.eventscan.Database.Database;

import com.bumptech.glide.request.RequestOptions;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Database.DatabaseHelper;


import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.DeviceID;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;


public class ViewEvent extends DialogFragment {

    Database db;
    Attendee selfAttendee = null;

    /**
     * Default constructor for the ViewEvent DialogFragment.
     */
    public ViewEvent() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        Event selectedEvent = (Event) getArguments().getSerializable("selectedEvent");
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.event_information_view, null);

        TextView eventNameText = view.findViewById(R.id.stored_event_name);
        eventNameText.setText(selectedEvent.getName());

        TextView eventDetailsTextView = view.findViewById(R.id.stored_event_desc);
        eventDetailsTextView.setText(selectedEvent.getDesc());

        Dialog dialog = new Dialog(requireContext());
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);

        ImageView posterView = view.findViewById(R.id.poster_view);
        Button returnView = view.findViewById(R.id.return_view);
        Button enrollEvent = view.findViewById(R.id.signup_event);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        db = Database.getInstance();


        StorageReference storageRef = storage.getReference().child("poster_pics");
        StorageReference posterRef = storageRef.child(selectedEvent.getEventID());
        Log.d("monkey", selectedEvent.getEventID());

        posterRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                            // Load the profile picture using an image loading library
                            Glide.with(this)
                                    .load(uri)
                                    .error(R.drawable.profile_icon) // Image to display in case of error
                                    .into(posterView);
                });

        Fragment parentFragment = getParentFragment();
        EventFragment eventFragment = (EventFragment) parentFragment;

        returnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        enrollEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String deviceID = DeviceID.getDeviceID(requireContext());
                db.attendees.get(deviceID)
                        .addOnSuccessListener(attendee -> {
                            selfAttendee = attendee;
                            // Check if selfAttendee is fetched successfully
                            if (selfAttendee != null) {
                                String eventID = selectedEvent.getEventID();
                                db.events.get(eventID)
                                        .addOnSuccessListener(event -> {
                                            if (event != null) {
                                                // Add interested attendee to the event
                                                db.events.addInterestedAttendee(event, selfAttendee)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d(TAG, "Signed up Successfully: ");
                                                            dismiss(); // Dismiss the dialog after successful enrollment
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Failed to Sign up: " + e.getMessage());
                                                            // Display a toast or error message to the user
                                                            Toast.makeText(getContext(), "Failed to sign up for the event", Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                Log.e(TAG, "Event not found for ID: " + eventID);
                                                // Display a toast or error message to the user
                                                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to retrieve event: " + e.getMessage());
                                            // Display a toast or error message to the user
                                            Toast.makeText(getContext(), "Failed to retrieve event", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.e(TAG, "Self attendee is null");
                                // Display a toast or error message to the user
                                Toast.makeText(getContext(), "Failed to fetch attendee information", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("QR SCAN", "couldn't fetch selfAttendee: " + e.toString());
                            // Display a toast or error message to the user
                            Toast.makeText(getContext(), "Failed to fetch attendee information", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

}