package com.example.eventscan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;


import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrganizerViewAttendee extends Fragment {

    private Database db;
    private ListView attendeesListView;
    private ArrayList<String> attendeesList;
    private ArrayAdapter<String> attendeesAdapter;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.attendee_list_content, container, false);

        // Initialize Firebase Firestore instance
        db = Database.getInstance();

        // Initialize UI components
        attendeesListView = view.findViewById(R.id.allUserList);
        attendeesList = new ArrayList<>();
        attendeesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, attendeesList);
        attendeesListView.setAdapter(attendeesAdapter);

        // Retrieve event ID from arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            String eventId = args.getString("eventId");
            if (eventId != null) {
                // Query database to retrieve attendees for the specified event ID
                db.events.get(eventId).addOnSuccessListener(event -> {
                    for(Attendee attendee: event.getCheckedInAttendeesList()){
                        attendeesList.add(attendee.getName());
                    }
                    attendeesAdapter.notifyDataSetChanged();
                });
            }
        }

        return view;
    }
}
