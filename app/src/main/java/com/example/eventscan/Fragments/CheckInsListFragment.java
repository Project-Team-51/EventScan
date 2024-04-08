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
import com.example.eventscan.Database.EventDatabaseRepresentation;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * a DialogFragment subclass for displaying the list of signed-up attendees.
 */
public class CheckInsListFragment extends DialogFragment {
    Database db = Database.getInstance();
    private static final String ARG_EVENT_ID = "event_id";

    public static CheckInsListFragment newInstance(String eventId) {
        CheckInsListFragment fragment = new CheckInsListFragment();
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
                    EventDatabaseRepresentation eventDatabaseRepresentation = new EventDatabaseRepresentation(event);

                    // Retrieve checked-in attendee IDs and their check-in counts
                    HashMap<String, Integer> checkedInAttendeeIDs = eventDatabaseRepresentation.getCheckedInAttendeeIDs();

                    if (!checkedInAttendeeIDs.isEmpty()) {
                        // Create a list to hold attendee names with check-in counts
                        ArrayList<String> attendeeInfoList = new ArrayList<>();

                        // Retrieve names and check-in counts for each attendee ID
                        for (Map.Entry<String, Integer> entry : checkedInAttendeeIDs.entrySet()) {
                            String attendeeID = entry.getKey();
                            int checkInCount = entry.getValue();

                            db.attendees.get(attendeeID)
                                    .addOnSuccessListener(attendee -> {
                                        // Construct attendee information string with name and check-in count
                                        String attendeeInfo = attendee.getName() + " : Checked In #: " + checkInCount;
                                        // Add attendee information to the list
                                        attendeeInfoList.add(attendeeInfo);

                                        // If info for all attendees are retrieved, update the list view
                                        if (attendeeInfoList.size() == checkedInAttendeeIDs.size()) {
                                            // Create an adapter with attendee info
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.signups_list_item, attendeeInfoList);
                                            attendeesListView.setAdapter(adapter);
                                            Toast.makeText(requireContext(), "Checked-in attendees retrieved successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure for retrieving attendee info
                                        Log.e("Database", "Error getting attendee: " + e.getMessage());
                                        Toast.makeText(requireContext(), "Failed to retrieve attendee info", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(requireContext(), "No checked-in attendees found for this event", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors that occur while retrieving the event
                    Log.e("Database", "Error getting event: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to retrieve event information", Toast.LENGTH_SHORT).show();
                });
    }

}

