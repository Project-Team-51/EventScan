package com.example.eventscan.Fragments;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.R;

import java.util.ArrayList;

public class SignupsListFragment extends DialogFragment {
    Database db = Database.getInstance();
    private static final String ARG_EVENT_ID = "event_id";

    public static SignupsListFragment newInstance(String eventId) {
        SignupsListFragment fragment = new SignupsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.signups_list, null);

        // Retrieve the event ID from arguments
        String eventId = getArguments().getString(ARG_EVENT_ID);

        ListView attendeesListView = view.findViewById(R.id.attendeesList);
        fillSignedUpAttendeesList(eventId, attendeesListView);

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.dimAmount = 0.7f; // Adjust this value as needed
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button returnButton = view.findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> dismiss());

        return dialog;
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

