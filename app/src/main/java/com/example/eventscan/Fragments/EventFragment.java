
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
import android.widget.Button;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * A fragment subclass that handles the displaying of events in two listviews. As of writing, it displays both attending classes
 * and owned classes as the same thing, which is all the events on the firestore. Clicking on an item in the ownedEvents list
 * brings up a delete dialog.
 */
public class EventFragment extends Fragment implements DeleteEvent.DeleteEventListener, SendNotificationFragment.SendNotificationListener{
    private ListView ownedEventsListView;
    private ListView inEventsListView;
    private ListView notificationsListView;
    private String userType;
    private ArrayList<Event> ownedEvents;
    private ArrayList<Event> inEvents;
    private ArrayList<Event> notifications;
    private EventArrayAdapter ownedEventsAdapter;
    private EventArrayAdapter inEventsAdapter;
    private EventArrayAdapter notificationsAdapter;

    private CollectionReference eventsCollection;
    private boolean isNotifyMode = false;
    private boolean isEventsMode = true;
    private boolean notified = false;
    private Database db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_observer_fragment, container, false);
        Bundle bundle = this.getArguments();
        TextView textView = (TextView) view.findViewById(R.id.yourEventsText);

        TextView textView2 = (TextView) view.findViewById(R.id.atEventsText);


        Button allEventButton = view.findViewById(R.id.allEventButton);

        String myDeviceID = DeviceID.getDeviceID(requireContext());
        Log.d("DeviceID", "Device ID: " + myDeviceID);
        // Get reference to the TextView


        // Set the device ID text to the TextView
        userType = DeviceID.getUserType(requireContext());
        switch (userType) {
            case "Organizer":
                if (isNotifyMode){
                    textView.setText("Select Event");
                    textView2.setText("Notification Center");
                } else {
                    textView.setText("Your Events");
                }
                break;
            case "Admin":
            case "Attendee":
                textView.setText("All Events");
                allEventButton.setVisibility(View.GONE);
                break;
        }



        ownedEvents = new ArrayList<>();
        inEvents = new ArrayList<>();
        notifications = new ArrayList<>();

        Map<String, String> eventTypeMap = new HashMap<>();
        ownedEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, ownedEvents);
        inEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, inEvents);
        notificationsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, inEvents);

        ownedEventsListView = view.findViewById(R.id.ownedEvents);
        inEventsListView = view.findViewById(R.id.inEvents);
        notificationsListView = view.findViewById(R.id.inEvents);

        ownedEventsListView.setAdapter(ownedEventsAdapter);
        inEventsListView.setAdapter(inEventsAdapter);
        notificationsListView.setAdapter(inEventsAdapter);

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

                    db.attendees.get(myDeviceID).continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(task.getException());
                        }
                        return db.attendees.getInterestedEvents(task.getResult());
                    }).addOnSuccessListener(eventList -> {
                        for (Event event : eventList) {
                            eventTypeMap.put(event.getEventID(), "interested");
                            inEventsAdapter.add(event);
                        }
                    });

                    db.attendees.get(myDeviceID).continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(task.getException());
                        }
                        return db.attendees.getCheckedInEvents(task.getResult());
                    }).addOnSuccessListener(eventList -> {
                        for (Event event : eventList) {
                            eventTypeMap.put(event.getEventID(), "checked in");
                            inEventsAdapter.add(event);
                        }
                    });

                    if (Objects.equals(userType,"Organizer")){
                        db.attendees.get(myDeviceID).continueWithTask(task -> {
                            if(!task.isSuccessful()){
                                return Tasks.forException(task.getException());
                            }
                            return db.attendees.getOwnedEvents(task.getResult());
                        }).addOnSuccessListener(eventList -> {
                            for (Event event : eventList) {
                                ownedEventsAdapter.add(event);
                            }
                        });
                    }

                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                        Task<Event> fetchEventTask = db.events.get(doc);
                        fetchEventTask.addOnCompleteListener(task -> {
                            if(!task.isSuccessful()){
                                failed_fetches_amount.incrementAndGet();
                                return;
                            }
                            Event event = task.getResult();



                            Log.d("EventLimit for " + event.getName(), "Attendee Limit: " + event.getAttendeeLimit());

                            if (!Objects.equals(userType, "Organizer")) {
                                ownedEventsAdapter.add(event);
                            }

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

                if(userType.equals("Organizer") && isNotifyMode){
                    openSendNotifcationFragment(selectedEvent);
                } else if(userType.equals("Organizer") || userType.equals("Admin") ){
                    openDeleteEventFragment(selectedEvent);
                }
                else{
                    openEventView(selectedEvent);
                }
            }
        });

        allEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                organizerAllEvent viewEventFragment = new organizerAllEvent(myDeviceID);
                viewEventFragment.show(getParentFragmentManager(), "ViewEventFragment");
            }
        });

        inEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = inEvents.get(position);
                openAttendingView(selectedEvent, eventTypeMap);
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





    private void openAttendingView(Event selectedEvent, Map<String, String> eventTypeMap) {
        AttendingEvent attendingEventFragment = new AttendingEvent(eventTypeMap);
        // Create a Bundle and put the selected Event information
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedEvent", selectedEvent);
        // Pass the eventTypeMap to the fragment
        attendingEventFragment.setArguments(bundle);
        // Show the AttendingEvent fragment
        attendingEventFragment.show(getParentFragmentManager(), "AttendingEventFragment");
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


    private void openSendNotifcationFragment(Event selectedEvent) {
        SendNotificationFragment sendNoti = new SendNotificationFragment();
        sendNoti.setSendNotificationListener(this);
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedEvent", selectedEvent);
        sendNoti.setArguments(bundle);
        sendNoti.show(getParentFragmentManager(), "sendNoti");
    }

    /**
     * Handles the sending of announcement
     *
     * @param event The event the announcement is for.
     */
    public void onSendNotification(Event event) {
        sendNotification(event);
    }


    /**
     * Sends the announcement to relevant people
     *
     * @param event The event the announcement is for
     */
    public void sendNotification(Event event) {
        //this needs to be filled
    }

    public void notificationSent(boolean notified){
        this.notified = notified;
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


    public void toggleNotifyMode(boolean isNotifyMode) {
        this.isNotifyMode = true;
        this.isEventsMode = false;
        // Refresh the events list based on the new mode
    }

    public boolean getToggleNotifyMode() {
        return isNotifyMode;
        // Refresh the events list based on the new mode
    }
    public void toggleEventsMode(boolean isEventsMode) {
        this.isEventsMode = true;
        this.isNotifyMode = false;
        // Refresh the events list based on the new mode
    }

}


// update events in real time
//        eventsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.e("Firestore", error.toString());
//                    return;
//                }
//                if (querySnapshots != null) { // if there is an update then..
//                    //SendNotification sendNoti = new SendNotification();
//                    ownedEvents.clear();
//                    inEvents.clear();
//                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
//                        Event event = doc.toObject(Event.class);
//                        Organizer organizer = event.getOrganizer();
//                       if (organizer != null) {
//                            String organizerDeviceID = organizer.getDeviceID();
////                            // display for when notification button is pressed (displays event announcements)
////                            if(isNotifyMode) {
////                                if (organizerDeviceID != null) {
////                                    Log.d("Firestore", "Organizer Device ID in EventFragment notifyMode: " + organizerDeviceID);
////                                    String currentDeviceID = getDeviceId(getContext());
////                                    Log.d("Firestore", "Current Device ID: " + currentDeviceID);
////                                    if (organizerDeviceID.equals(currentDeviceID)) {
////                                        ownedEventsAdapter.add(event);
////                                        ownedEventsAdapter.notifyDataSetChanged();
////                                    }
////                                    if (notified) {
////                                        Log.d("Notified", "notifed = true");
////                                        inEventsAdapter.add(event);
////                                        inEventsAdapter.notifyDataSetChanged();
////                                    }
////                                    event.setDesc(event.getDesc());
////                                } else {
////                                    Log.e("Firestore", "Organizer Device ID is null");
////                                }
////                                event.setDesc(event.getDesc());
//                                // display for when events button is pressed (displays owned and in events)
//                            //if (isEventsMode){
//                            if (organizerDeviceID != null) {
//                                Log.d("Firestore", "Organizer Device ID in EventFragment: " + organizerDeviceID);
//                                String currentDeviceID = getDeviceId(getContext());
//                                Log.d("Firestore", "Current Device ID: " + currentDeviceID);
//                                if (organizerDeviceID.equals(currentDeviceID)) {
//                                    Log.d("OwnedEvents", "Current Device ID: " + currentDeviceID);
//                                    ownedEventsAdapter.add(event);
//                                    ownedEventsAdapter.notifyDataSetChanged();
//                                } else {
//                                    inEventsAdapter.add(event);
//                                    inEventsAdapter.notifyDataSetChanged();
//                                }
//                            } else {
//                                Log.e("Firestore", "Organizer Device ID is null");
//                            }
//
//                        } else { // need to add conditionals for admins and attendees
//                            Log.e("Firestore", "Organizer is null for event: " + event.getEventID());
//                        }
//
//                    }
//                    ownedEventsAdapter.notifyDataSetChanged(); // update listviews
//                    inEventsAdapter.notifyDataSetChanged();
//                }
//            }
//        });



