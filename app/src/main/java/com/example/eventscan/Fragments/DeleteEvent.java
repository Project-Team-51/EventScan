package com.example.eventscan.Fragments;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.DeviceID;
import com.bumptech.glide.Glide;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Helpers.GeolocationHandler;
import com.example.eventscan.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.osmdroid.api.IGeoPoint;

import java.util.ArrayList;
import java.util.Objects;
/*
 * A simple dialogfragment that displays some basic info about the Event that is passed into it, and gives
 * the user the ability to delete the profile from the app.
 */
public class DeleteEvent extends DialogFragment {

    Database db;
    Attendee selfAttendee = null;
    private String userType;

    /**
     * Default constructor for the DeleteEvent DialogFragment.
     */
    public DeleteEvent() {
        // Required empty public constructor
    }

    /**
     * Interface for handling event deletion.
     */
    public interface DeleteEventListener {
        void onDeleteEvent(Event event);
    }
    private DeleteEventListener deleteEventListener;

    /**
     * Called to create the dialog view.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state.
     * @return A Dialog representing the event deletion dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        Event selectedEvent = (Event) getArguments().getSerializable("selectedEvent");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_delete_event_admin, null);

        TextView eventNameText = view.findViewById(R.id.event_Name);
        eventNameText.setText(selectedEvent.getName());

        TextView eventDetailsTextView = view.findViewById(R.id.EventDescription);
        eventDetailsTextView.setText(selectedEvent.getDesc());

        Dialog dialog = new Dialog(requireContext());
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);

        Button delEvent = view.findViewById(R.id.confirmEvent);
        Button returnAdmin = view.findViewById(R.id.return2);
        Button signupButton = view.findViewById(R.id.signup_button);
        Button showMap = view.findViewById(R.id.showmap_button);

        db = Database.getInstance();

        userType = DeviceID.getUserType(requireContext());
        if (userType.equals("Organizer")) {
            signupButton.setVisibility(View.GONE);
        }

        Fragment parentFragment = getParentFragment();
        EventFragment eventFragment = (EventFragment) parentFragment;

        ImageView posterView = view.findViewById(R.id.posterView);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        db = Database.getInstance();

        StorageReference storageRef = storage.getReference().child("poster_pics");
        StorageReference posterRef = storageRef.child(selectedEvent.getEventID());

        posterRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Load the profile picture using an image loading library
                    Glide.with(this)
                            .load(uri)
                            .into(posterView);
                });
        delEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteEventListener != null) {
                    deleteEventListener.onDeleteEvent(selectedEvent);}
                dismiss();
            }
        });

        returnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        showMap.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // ENTER ARRAY LIST HERE
                openMapView(points);
            }
        });
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String deviceID = DeviceID.getDeviceID(requireContext());
                db.attendees.get(deviceID)  // Needs to be changed to reflect admins
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
                                                            Toast.makeText(getContext(), "Sign up Successful", Toast.LENGTH_SHORT).show();
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

    private void openMapView(){
        ViewMap viewMapFragment = new ViewMap();
        // Create a Bundle and put the selected Event information
        Bundle bundle = new Bundle();
        viewMapFragment.setArguments(bundle);
        // Show the ViewMap fragment
        viewMapFragment.show(getParentFragmentManager(), "ViewMapFragment");
    }

    /**
     * Sets the event listener for event deletion.
     *
     * @param listener The listener to be set for event deletion.
     */

    public void setDeleteEventListener(DeleteEventListener listener) {
        this.deleteEventListener = listener;
    }
}