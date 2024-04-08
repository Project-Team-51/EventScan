package com.example.eventscan.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.DeviceID;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Helpers.EventArrayAdapter;
import com.example.eventscan.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class organizerAllEvent extends DialogFragment {
    private ArrayList<Event> allEvents;
    private EventArrayAdapter alLEventsAdapter;
    private Database db;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.all_event_org, null);
        Button returnButton = view.findViewById(R.id.returnButt);
        Dialog dialog = new Dialog(requireContext());

        Bundle bundle = this.getArguments();
        String myDeviceID = DeviceID.getDeviceID(requireContext());
        Log.d("DeviceID", "Device ID: " + myDeviceID);
        allEvents = new ArrayList<>();
        ListView allEventsView = view.findViewById(R.id.allUserList);
        alLEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, allEvents); // Initialize the adapter
        allEventsView.setAdapter(alLEventsAdapter); // Set the adapter
        db = Database.getInstance();

        // Retrieve events from Firestore
        retrieveEventsFromFirestore();

        dialog.setContentView(view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.dimAmount = 0.7f; // Adjust this value as needed
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        returnButton.setOnClickListener(v -> dismiss());

        allEventsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = allEvents.get(position);
                    openEventView(selectedEvent);

            }
        });
        return dialog;
    }

    private void retrieveEventsFromFirestore() {
        db.getEventsCollection().get().addOnSuccessListener(queryDocumentSnapshots -> {
            allEvents.clear(); // Clear the list before adding new events
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Event event = documentSnapshot.toObject(Event.class); // Convert document to Event object
                allEvents.add(event); // Add event to the list
            }
            alLEventsAdapter.notifyDataSetChanged(); // Notify adapter of data change
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error retrieving events: " + e.getMessage());
        });
    }

    private void openEventView(Event selectedEvent){
        ViewEvent viewEventFragment = new ViewEvent();
        // Create a Bundle and put the selected Event information
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedEvent", selectedEvent);
        viewEventFragment.setArguments(bundle);
        // Show the DeleteEvent fragment
        viewEventFragment.show(getParentFragmentManager(), "ViewEventFragment");
    }
}
