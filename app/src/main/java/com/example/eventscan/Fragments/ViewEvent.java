package com.example.eventscan.Fragments;


import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import com.example.eventscan.Database.Database;
import com.example.eventscan.Database.DatabaseHelper;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;


public class ViewEvent extends DialogFragment {

    Database db;
    Attendee selfAttendee;

    /**
     * Default constructor for the ViewEvent DialogFragment.
     */
    public ViewEvent() {
        // Required empty public constructor
        this.selfAttendee = selfAttendee;
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

        Button returnView = view.findViewById(R.id.return_view);
        Button enrollEvent = view.findViewById(R.id.enroll_event);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("poster_pictures");
        StorageReference fileRef = storageRef.child(selectedEvent.getEventID());

        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            // Decode the byte array into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            // Set the Bitmap to the ImageView
            ImageView posterView = view.findViewById(R.id.poster_view);
            posterView.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.e(TAG, "Failed to download image", exception);
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
                if (selfAttendee != null) {
                    String eventID = selectedEvent.getEventID();
                    db = Database.getInstance();
                    //db.attendees.addInterestedAttendee(eventID, selfAttendee)
                    Database.EventOperations eventOperations = db.events;
                    eventOperations.addInterestedAttendee(eventID, selfAttendee);
                            .addOnSuccessListener(aVoid -> {
                                //Toast.makeText(context, "Enrolled successfully", Toast.LENGTH_SHORT).show();
                                dismiss(); // Dismiss the dialog after successful enrollment
                            })
                            .addOnFailureListener(e -> {
                                //Log.e(TAG, "Failed to enroll: " + e.getMessage());
                                //Toast.makeText(context, "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.e(TAG, "Self attendee is null");
                    //Toast.makeText(context, "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show();
                    dismiss(); // Dismiss the dialog if self attendee is null
                }
            }
        });

        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

}