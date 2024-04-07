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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.example.eventscan.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.eventscan.Helpers.UserArrayAdapter;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/*
 * A simple dialogfragment that displays some basic info about the Event that is passed into it, and gives
 * the user the ability to delete the profile from the app.
 */
public class DeleteEvent extends DialogFragment {

    Database db;
    Attendee selfAttendee = null;
    private String userType;
    private ListView attendeesListView;
    private UserArrayAdapter adapter;
    private List<Attendee> attendeesList;

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
/**
        attendeesListView = view.findViewById(R.id.attendeesList);
        attendeesList = new ArrayList<>();
        adapter = new UserArrayAdapter(requireContext(), attendeesList);
        attendeesListView.setAdapter(adapter);
**/
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
        Button signupsButton = view.findViewById(R.id.signups_button);

        db = Database.getInstance();

        userType = DeviceID.getUserType(requireContext());
        if (userType.equals("Admin")) {
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

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String deviceID = DeviceID.getDeviceID(requireContext());
                db.attendees.get(deviceID)  // Needs to be changed to reflect admins
                        .addOnSuccessListener(attendee -> {
                            selfAttendee = attendee;
                            // Check if selfAttendee is retrieved successfully
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

        signupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate signups_list.xml layout
                View signupsListView = getLayoutInflater().inflate(R.layout.signups_list, null);

                // Find the ListView inside signups_list.xml
                ListView attendeesListView = signupsListView.findViewById(R.id.attendeesList);

                // Fetch signed-up attendees and populate the ListView here...
                fillSignedUpAttendeesList(selectedEvent.getEventID(), attendeesListView); // Pass selectedEvent.getEventID() here

                // Create and show AlertDialog containing signupsListView
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setView(signupsListView);
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });






        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    /**
     * Sets the event listener for event deletion.
     *
     * @param listener The listener to be set for event deletion.
     */

    public void setDeleteEventListener(DeleteEventListener listener) {
        this.deleteEventListener = listener;
    }

    private void fillSignedUpAttendeesList(String eventId, ListView attendeesListView) {
        db.events.get(eventId)
                .addOnSuccessListener(event -> {
                    ArrayList<Attendee> attendeesList = event.getInterestedAttendees();
                    if (attendeesList != null && !attendeesList.isEmpty()) {
                        // Create a list to hold attendee names
                        ArrayList<String> attendeeNames = new ArrayList<>();
                        // Retrieve names for each attendee ID
                        for (Attendee attendee : attendeesList) {
                            db.attendees.get(attendee.getDeviceID())
                                    .addOnSuccessListener(att -> {
                                        // Add attendee name to the list
                                        attendeeNames.add(att.getName());
                                        // If names for all attendees are retrieved, update the list view
                                        if (attendeeNames.size() == attendeesList.size()) {
                                            // Create an adapter with attendee names
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.signups_list_item, attendeeNames);
                                            attendeesListView.setAdapter(adapter);
                                            Toast.makeText(requireContext(), "Signed-up attendees retrieved successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure for retrieving attendee name
                                        Log.e("Database", "Error getting attendee: " + e.getMessage());
                                        Toast.makeText(requireContext(), "Failed to retrieve attendee name", Toast.LENGTH_SHORT).show();
                                        // If retrieving name fails, remove this attendee from the list
                                        attendeesList.remove(attendee);
                                        // If names for all attendees are retrieved or failed, update the list view
                                        if (attendeeNames.size() + attendeesList.size() == attendeesList.size()) {
                                            // Create an adapter with attendee names
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.signups_list_item, attendeeNames);
                                            attendeesListView.setAdapter(adapter);
                                            Toast.makeText(requireContext(), "Failed to retrieve names for some attendees", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(requireContext(), "No signed-up attendees found for this event", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors that occur while retrieving the event
                    Log.e("Database", "Error getting event: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to retrieve signed-up attendees", Toast.LENGTH_SHORT).show();
                });
    }





}