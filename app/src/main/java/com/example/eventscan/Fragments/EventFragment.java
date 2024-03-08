package com.example.eventscan.Fragments;


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
 * A fragment subclass that handles the displaying of events in two listviews. As of writing, it displays both attending classes
 * and owned classes as the same thing, which is all the events on the firestore. Clicking on an item in the ownedEvents list
 * brings up a delete dialog.
 */
public class EventFragment extends Fragment implements DeleteEvent.DeleteEventListener{
    private ListView ownedEventsListView;
    private ListView inEventsListView;
    private ArrayList<Event> ownedEvents;
    private ArrayList<Event> inEvents;
    private EventArrayAdapter ownedEventsAdapter;
    private EventArrayAdapter inEventsAdapter;

    private FirebaseFirestore db;
    private CollectionReference eventsCollection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_observer_fragment, container, false);

        ownedEvents = new ArrayList<>();
        inEvents = new ArrayList<>();

        ownedEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, ownedEvents);
        inEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, inEvents);

        ownedEventsListView = view.findViewById(R.id.ownedEvents);
        inEventsListView = view.findViewById(R.id.inEvents);

        ownedEventsListView.setAdapter(ownedEventsAdapter);
        inEventsListView.setAdapter(inEventsAdapter);

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
                    ownedEvents.clear();
                    inEvents.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                        Event event = doc.toObject(Event.class);
                        ownedEventsAdapter.add(event);
                        inEventsAdapter.add(event);
                    }
                    ownedEventsAdapter.notifyDataSetChanged(); // update listviews
                    inEventsAdapter.notifyDataSetChanged();
                }
            }
        });

        ownedEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedBook = ownedEvents.get(position);
                openDeleteEventFragment(selectedBook);
            }
        });

        return view;
    }

    private void openDeleteEventFragment(Event selectedEvent) {
        DeleteEvent deleteEventFragment = new DeleteEvent();
        deleteEventFragment.setDeleteEventListener(this);
        // Create a Bundle and put the selected Event information
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedEvent", selectedEvent);
        deleteEventFragment.setArguments(bundle);
        // Show the DeleteEvent fragment
        deleteEventFragment.show(getParentFragmentManager(), "DeleteEventFragment");
    }

    public void onDeleteEvent(Event event) {
        deleteEvent(event);
    }

    public void deleteEvent(Event event) {
        ownedEventsAdapter.remove(event);
        ownedEventsAdapter.notifyDataSetChanged();
        db.collection("events").document(event.getEventID()).delete();
    }
}