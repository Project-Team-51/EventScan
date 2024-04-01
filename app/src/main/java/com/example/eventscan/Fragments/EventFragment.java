package com.example.eventscan.Fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;

import com.example.eventscan.Entities.User;

import com.example.eventscan.Helpers.EventArrayAdapter;
import com.example.eventscan.Helpers.UserArrayAdapter;
import com.example.eventscan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/*
 * A fragment subclass that handles the displaying of events in two listviews. As of writing, it displays both attending classes
 * and owned classes as the same thing, which is all the events on the firestore. Clicking on an item in the ownedEvents list
 * brings up a delete dialog.
 */
public class EventFragment extends Fragment implements DeleteEvent.DeleteEventListener{
    private ListView ownedEventsListView;
    private ListView inEventsListView;
    private ArrayList<Event> ownedEvents;
    private String userType;
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
                Event selectedEvent = ownedEvents.get(position);
                if(userType.equals("organizer")){
                    openEventView(selectedEvent);
                }
                else{
                    openDeleteEventFragment(selectedEvent);
                }
            }
        });

        // Grab user type, organizer or Attendee
        String deviceID = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("attendees")
                .document(deviceID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        Attendee attendee = documentSnapshot.toObject(Attendee.class);
                        // TEMPORARY FIX
                        // Must revamp how attendees are stored
                        if (attendee != null) {
                            userType = attendee.getType();
                            customizeLayout(userType, view);
                        }
                    } else {
                        Log.e("elephant", "Error getting document: ", task.getException());
                    }
                });

        return view;

    }

    /**
     * Changes layout based on user type.
     *
     * @param userType The user's selected role.
     * @param view The view to be changed.
     */
    @SuppressLint("SetTextI18n")
    private void customizeLayout(String userType, View view) {
        TextView yourEventsText = view.findViewById(R.id.yourEventsText);
        yourEventsText.setText("All Events");
        if ("attendee".equals(userType)) {
            yourEventsText.setText("All Events");
            Log.d("elephant", "customizeLayout: success");
        }
    }

    private void openEventView(Event selectedEvent){
        ViewEvent ViewEventFragment = new ViewEvent();
        // Create a Bundle and put the selected Event information
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedEvent", selectedEvent);
        ViewEventFragment.setArguments(bundle);
        // Show the DeleteEvent fragment
        ViewEventFragment.show(getParentFragmentManager(), "ViewEventFragment");
    }

    /**
     * Opens the DeleteEvent fragment for the selected event.
     *
     * @param selectedEvent The event to be deleted.
     */
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

    /**
     * Handles the deletion of an event.
     *
     * @param event The event to be deleted.
     */
    public void onDeleteEvent(Event event) {
        deleteEvent(event);
    }

    /**
     * Deletes the specified event from the list and Firestore.
     *
     * @param event The event to be deleted.
     */

    public void deleteEvent(Event event) {
        ownedEventsAdapter.remove(event);
        ownedEventsAdapter.notifyDataSetChanged();
        db.collection("events").document(event.getEventID()).delete();
    }

    private void fetchUsersForEvent(String eventId) {
        // Assuming you have a CollectionReference for users
        CollectionReference eventsCollection = db.collection("events");

        // Query users collection for users associated with the given event ID
        eventsCollection.document(eventId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        if(task.isSuccessful()) {
                            Event event = document.toObject(Event.class);
                            // arraylist of type attendee
                            ArrayList<Attendee> attendeesList = event.getCheckedInAttendeesList();
                        } else {
                            Log.d("NAMEPOP", "Error getting documents: ", task.getException());
                        }

                    }
                });
        }
    // changed
    private void displayAttendees(ArrayList<User> attendeesList) {
        // Create an adapter for the list of attendees
        UserArrayAdapter attendeeAdapter = new UserArrayAdapter(getActivity(), R.layout.attendee_list_content, attendeesList);

        // Assuming you have a ListView in your layout with the id 'attendeesListView'
        ListView attendeesListView = getView().findViewById(R.id.allUserList);

        // Set the adapter to the ListView
        attendeesListView.setAdapter(attendeeAdapter);
    }

}