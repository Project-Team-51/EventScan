package com.example.eventscan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class OrganizerViewAttendee extends Fragment {

    private FirebaseFirestore db;
    private ListView attendeesListView;
    private ArrayList<String> attendeesList;
    private ArrayAdapter<String> attendeesAdapter;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.attendee_list_content, container, false);

        // Initialize Firebase Firestore instance
        db = FirebaseFirestore.getInstance();

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
                CollectionReference attendeesRef = db.collection("events").document(eventId).collection("attendees");
                attendeesRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        attendeesList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Attendee attendee = document.toObject(Attendee.class);
                            attendeesList.add(attendee.getName());
                        }
                        attendeesAdapter.notifyDataSetChanged(); // Update list view
                    } else {
                        Toast.makeText(requireContext(), "Failed to retrieve attendees", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        return view;
    }
}
