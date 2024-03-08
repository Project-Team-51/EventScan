package com.example.eventscan.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.eventscan.Entities.Administrator;
import com.example.eventscan.Entities.Event;

import com.example.eventscan.Entities.User;

import com.example.eventscan.Helpers.EventArrayAdapter;
import com.example.eventscan.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import java.util.List;
/*
 * The activity that an Organizer see's when they click on the navigation button to see all events. Allows them to see all events.
 * Utilizes very similar code to the EventFragment. This will be consolidated into the EventFragment in a future build, as this is unneeded as a standalone activity.
 */
public class OrganizerViewAllEvents extends Fragment implements View.OnClickListener {
    private ListView allEventsListView;
    private ArrayList<Event> allEvents;
    private EventArrayAdapter allEventsAdapter;

    private FirebaseFirestore db;
    private CollectionReference eventsCollection;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_view_all, container, false);

        allEvents = new ArrayList<>();

        allEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, allEvents);

        allEventsListView = view.findViewById(R.id.allEvents);

        allEventsListView.setAdapter(allEventsAdapter);

        // initialize firestore
        db = FirebaseFirestore.getInstance();
        eventsCollection = db.collection("events");

        // update events in real time
        eventsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) { // if there is an update then..
                    allEvents.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                        Event event = doc.toObject(Event.class);
                        allEventsAdapter.add(event);
                    }
                    allEventsAdapter.notifyDataSetChanged(); // update listviews
                }
            }
        });

        allEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedBook = allEvents.get(position);
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {

    }
}


