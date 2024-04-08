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
import com.example.eventscan.Database.EventDatabaseRepresentation;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.DeviceID;
import com.bumptech.glide.Glide;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Helpers.GeolocationHandler;
import com.example.eventscan.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.eventscan.Helpers.UserArrayAdapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/**
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
        Button showMap = view.findViewById(R.id.showmap_button);
        Button signupsButton = view.findViewById(R.id.signups_button);
        Button viewAttendeesButton = view.findViewById(R.id.checked_in_attendees);

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
                openMapView(selectedEvent);
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
                String eventId = selectedEvent.getEventID();
                SignupsListFragment fragment = SignupsListFragment.newInstance(eventId);
                fragment.show(getParentFragmentManager(), "SignupsListFragment");
            }
        });



        viewAttendeesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                // Create the view for displaying the checked-in attendees
//                View viewCheckedInAttendees = getLayoutInflater().inflate(R.layout.checkedin_list, null);
//                ListView checkedInAttendeesListView = viewCheckedInAttendees.findViewById(R.id.checkedInList);
//
//                // Call the method to fill the list of checked-in attendees
//                fillCheckedInAttendeesList(selectedEvent.getEventID(), checkedInAttendeesListView);
//
//                // Create and display the AlertDialog containing the populated list of checked-in attendees
//                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//                builder.setView(viewCheckedInAttendees);
//                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
                String eventId = selectedEvent.getEventID();
                CheckInsListFragment fragment = CheckInsListFragment.newInstance(eventId);
                fragment.show(getParentFragmentManager(), "CheckedInListFragment");
            }
        });


        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    private void openMapView(Event selectedEvent){
        ViewMap viewMapFragment = new ViewMap();
        // Create a Bundle and put the selected Event information
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedEvent", selectedEvent);
        viewMapFragment.setArguments(bundle);
        // Show the ViewMap fragment
        viewMapFragment.show(getParentFragmentManager(), "ViewMapFragment");
    }

    private void fillCheckedInAttendeesList(String eventId, ListView checkedInListListView) {
        EventDatabaseRepresentation eventRepresentation = new EventDatabaseRepresentation();
        //List<String> attendeeInfoList = new ArrayList<>();
        // Get the checked-in attendee IDs for the specified event ID
        final HashMap<String, Integer> checkedInAttendeeIDs = new HashMap<>();
        // checkedInAttendeeIDs = eventRepresentation.getCheckedInAttendeeIDs();
        // Iterate over the attendee IDs and fetch attendee information from the database
        for (Map.Entry<String, Integer> entry : checkedInAttendeeIDs.entrySet()) {
            List<String> attendeeInfoList = new ArrayList<>();
            String attendeeID = entry.getKey();
            int userAmount = entry.getValue();

            // Fetch attendee information from the database using attendeeID
            db.attendees.get(attendeeID)
                    .addOnSuccessListener(attendee -> {
                        // Display the fetched attendee information in the ListView
                        // Not sure about the how to get the integer value here
                        String attendeeInfo = attendee.getName() + " - Checked in ";
                        // Add the attendeeInfo to the ListView
                        attendeeInfoList.add(attendeeInfo);

                        // Check if all attendees have been processed
                        if (attendeeInfoList.size() == checkedInAttendeeIDs.size()) {
                            // Create an adapter to populate the ListView with the attendee information
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, attendeeInfoList);
                            // Set the adapter to the ListView
                            checkedInListListView.setAdapter(adapter);
                        }
                        // For demonstration purposes, let's just display attendeeInfo in a Toast
                        Toast.makeText(getContext(), attendeeInfo, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to fetch attendee information
                        Log.e("DeleteEvent", "Failed to fetch attendee information: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to fetch attendee information", Toast.LENGTH_SHORT).show();
                    });
        }





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