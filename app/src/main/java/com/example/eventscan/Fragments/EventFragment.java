package com.example.eventscan.Fragments;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Administrator;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;

import com.example.eventscan.Entities.Organizer;
import com.example.eventscan.Entities.User;

import com.example.eventscan.Helpers.AnnouncementArrayAdapter;
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

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

import java.util.List;
/*
 * A fragment subclass that handles the displaying of events in two listviews. As of writing, it displays both attending classes
 * and owned classes as the same thing, which is all the events on the firestore. Clicking on an item in the ownedEvents list
 * brings up a delete dialog.
 */
public class EventFragment extends Fragment implements DeleteEvent.DeleteEventListener, SendNotification.SendNotificationListener{
    private ListView ownedEventsListView;
    private ListView inEventsListView;
    private ListView announcementsListView;
    private ArrayList<Event> ownedEvents;
    private String userType;
    private ArrayList<Event> inEvents;
    private EventArrayAdapter ownedEventsAdapter;
    private EventArrayAdapter inEventsAdapter;
    private AnnouncementArrayAdapter announcementsAdapter;
    private ArrayList<Event> eventAnnouncements;
    private AnnouncementArrayAdapter eventAnnouncementsAdapter;
    private SendNotification sendNoti;

    private FirebaseFirestore db;
    private CollectionReference eventsCollection;
    private boolean isNotifyMode = false;
    private boolean isEventsMode = true;
    private boolean notified = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_observer_fragment, container, false);

        ownedEvents = new ArrayList<>();
        inEvents = new ArrayList<>();
        eventAnnouncements = new ArrayList<>();

        ownedEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, ownedEvents);
        inEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, inEvents);
        eventAnnouncementsAdapter = new AnnouncementArrayAdapter(getActivity(), R.layout.event_announcements_list_content, eventAnnouncements);
//
        if (isEventsMode) {
            Log.d("Event mode", "in event mode");
            inEventsListView = view.findViewById(R.id.inEvents);
            inEventsListView.setAdapter(inEventsAdapter);
        } else {
            Log.d("NOTIFIED", "in notify mode");

            announcementsListView = view.findViewById(R.id.inEvents);
            announcementsListView.setAdapter(announcementsAdapter);
        }

        ownedEventsListView = view.findViewById(R.id.ownedEvents);
        ownedEventsListView.setAdapter(ownedEventsAdapter);

        // initialize firestore
        db = FirebaseFirestore.getInstance();
        eventsCollection = db.collection("events");


//        eventsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.e("Firestore", error.toString());
//                    return;
//                }
//                if (querySnapshots != null) { // if there is an update then..
//                    ownedEvents.clear();
//                    inEvents.clear();
//                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
//                        Event event = doc.toObject(Event.class);
//                        Organizer organizer = event.getOrganizer();
//                        if (organizer != null) {
//                            String organizerDeviceID = organizer.getDeviceID();
//                            if (organizerDeviceID != null) {
//                                Log.d("Firestore", "Organizer Device ID in EventFragment notifyMode: " + organizerDeviceID);
//                                String currentDeviceID = getDeviceId(getContext());
//                                Log.d("Firestore", "Current Device ID: " + currentDeviceID);
//                                if (organizerDeviceID.equals(currentDeviceID)) {
//                                    ownedEventsAdapter.add(event);
//                                    ownedEventsAdapter.notifyDataSetChanged();
//                                } else {
//                                    if (isEventsMode) {
//                                        inEventsAdapter.add(event);
//                                    } else if (isNotifyMode){
//                                        eventAnnouncementsAdapter.add(event);
//                                    }
//                                }
//                            } else {
//                                Log.e("Firestore", "Organizer Device ID is null");
//                            }
//                        }
//                    }
//
//                    ownedEventsAdapter.notifyDataSetChanged(); // update listviews
//                    inEventsAdapter.notifyDataSetChanged();
//                    eventAnnouncementsAdapter.notifyDataSetChanged();
//                }
//            }
//        });
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
                if (isNotifyMode){
                    openSendNotifcationFragment(selectedEvent);
//                } else if(userType.equals("attendee")){
//                    openEventView(selectedEvent);
                } else {
                    openEventView(selectedEvent);
                    //openDeleteEventFragment(selectedEvent);
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


//        db.collection("users")
//                .document(deviceID)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot documentSnapshot = task.getResult();
//                        Organizer organizer = documentSnapshot.toObject(Organizer.class);
//                        userType = organizer.getType();
//                        customizeLayout(userType, view);
//                    } else {
//                        Log.e("elephant", "Error getting document: ", task.getException());
//                    }
//                });

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
        TextView atEventsText = view.findViewById(R.id.atEventsText);

        if(isNotifyMode){

            yourEventsText.setText("Select Event");
            atEventsText.setText("Event Announcements");
        } else if (isEventsMode) {

            yourEventsText.setText("Your Events");
        } else if ("attendee".equals(userType)) {
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

    private void openSendNotifcationFragment(Event selectedEvent) {
        SendNotification sendNoti = new SendNotification();
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
        eventAnnouncementsAdapter.add(event);
        eventAnnouncementsAdapter.notifyDataSetChanged();
        notified = true;
        if (!isEventsMode) {
            announcementsListView.setAdapter(eventAnnouncementsAdapter);
        }
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
                            ArrayList<Attendee> attendeesList = event.getAttendees();
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

    public static String getDeviceId(Context context) {
        // Retrieve the device ID using Settings.Secure class
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
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

    public void notificationSent(boolean notified){
        this.notified = notified;
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
