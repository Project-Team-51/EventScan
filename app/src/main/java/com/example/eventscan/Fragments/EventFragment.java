package com.example.eventscan.Fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.DeviceID;
import com.example.eventscan.Entities.Event;

import com.example.eventscan.Entities.User;

import com.example.eventscan.Helpers.EventArrayAdapter;
import com.example.eventscan.Helpers.UserArrayAdapter;
import com.example.eventscan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    private CollectionReference eventsCollection;
    private Database db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_observer_fragment, container, false);
        Bundle bundle = this.getArguments();
        TextView textView = (TextView) view.findViewById(R.id.yourEventsText);

        String myDeviceID = DeviceID.getDeviceID(requireContext());
        Log.d("DeviceID", "Device ID: " + myDeviceID);

        // Get reference to the TextView


        // Set the device ID text to the TextView
        userType = DeviceID.getUserType(requireContext());
        switch (userType) {
            case "Organizer":
                textView.setText("Your Events");
                break;
            case "Admin":
            case "Attendee":
                textView.setText("All Events");
                break;
        }


        ownedEvents = new ArrayList<>();
        inEvents = new ArrayList<>();

        ownedEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, ownedEvents);
        inEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, inEvents);

        ownedEventsListView = view.findViewById(R.id.ownedEvents);
        inEventsListView = view.findViewById(R.id.inEvents);

        ownedEventsListView.setAdapter(ownedEventsAdapter);
        inEventsListView.setAdapter(inEventsAdapter);

        db = Database.getInstance();

        eventsCollection = db.getEventsCollection();
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
                    ArrayList<Task<Event>> updateTasks = new ArrayList<>();
                    AtomicInteger failed_fetches_amount = new AtomicInteger(); // AtomicInteger suggested by android studio
                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                        Task<Event> fetchEventTask = db.events.get(doc);
                        fetchEventTask.addOnCompleteListener(task -> {
                            if(!task.isSuccessful()){
                                failed_fetches_amount.incrementAndGet();
                                return;
                            }
                            Event event = task.getResult();
                            ownedEventsAdapter.add(event);
                            inEventsAdapter.add(event);
                        });
                        updateTasks.add(fetchEventTask);
                    }
                    // bigTask will be complete when all "fetchEventTask"s are complete from the loop above
                    Task<List<Task<?>>> bigTask = Tasks.whenAllComplete(updateTasks);
                    bigTask.addOnCompleteListener(task -> {
                        ownedEventsAdapter.notifyDataSetChanged(); // update listviews
                        inEventsAdapter.notifyDataSetChanged();
                    });
                }
            }
        });

        ownedEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = ownedEvents.get(position);
                if(userType.equals("Organizer") || userType.equals("Admin") ){
                    openDeleteEventFragment(selectedEvent);
                }
                else{
                    openEventView(selectedEvent);
                }
            }
        });
        return view;
    }

    private void openEventView(Event selectedEvent){
        ViewEvent viewEventFragment = new ViewEvent();
        // Create a Bundle and put the selected Event information
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedEvent", selectedEvent);
        viewEventFragment.setArguments(bundle);
        // Show ViewEvent fragment
        viewEventFragment.show(getParentFragmentManager(), "ViewEventFragment");
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
        db.events.delete(event).addOnFailureListener(e -> {
            Log.e("Delete event","Failed to delete event "+event.getEventID());
        });
    }

// Commented out below code - the functions don't look complete and there are no usages - didn't update the DB calls yet because of the not-finished ambiguity
//    private void fetchUsersForEvent(String eventId) {
//        // Assuming you have a CollectionReference for users
//        CollectionReference eventsCollection = db.collection("events");
//
//        // Query users collection for users associated with the given event ID
//        eventsCollection.document(eventId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        DocumentSnapshot document = task.getResult();
//                        if(task.isSuccessful()) {
//                            Event event = document.toObject(Event.class);
//                            // arraylist of type attendee
//                            ArrayList<Attendee> attendeesList = event.getCheckedInAttendeesList();
//                        } else {
//                            Log.d("NAMEPOP", "Error getting documents: ", task.getException());
//                        }
//
//                    }
//                });
//        }
//    // changed
//    private void displayAttendees(ArrayList<User> attendeesList) {
//        // Create an adapter for the list of attendees
//        UserArrayAdapter attendeeAdapter = new UserArrayAdapter(getActivity(), R.layout.attendee_list_content, attendeesList);
//
//        // Assuming you have a ListView in your layout with the id 'attendeesListView'
//        ListView attendeesListView = getView().findViewById(R.id.allUserList);
//
//        // Set the adapter to the ListView
//        attendeesListView.setAdapter(attendeeAdapter);
//    }


    public void setText(String text){
        TextView textView = (TextView) getView().findViewById(R.id.yourEventsText);
        textView.setText(text);
    }
}
