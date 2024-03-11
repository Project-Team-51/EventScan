package com.example.eventscan.Database;

import androidx.annotation.NonNull;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Helper class for easily pulling/pushing data from/to the database
 * call the getter functions as early as possible,
 * use foo.getResult() to resolve them, do this as late as possible
 */
public class DatabaseManager {
    // References
    // Bing Copilot (Bing Chat), 2024-MR-08, "how can I turn a FirebaseFirestore document search into a future in java android" -> "I have a document containing the data needed to build an object, I want the future to return a built object"
    // gave me information on how to use Task.continueWith(new Continuation...)
    // implementation written by us though
    private static final String attendeeCollectionPath = "attendees"; // easier to change here if we refactor the DB later
    private static final String eventsCollectionPath = "events";

    /**
     * Get an object that may contain an Attendee in the future.
     * Call this as early as possible, then call foo.getResult() later to get the request's output
     * give as much space as possible between calling this and calling .getResult() so that the request can process
     * @param attendeeID the ID of the attendee to fetch
     * @return a Task\<Attendee\> object, call getResult() on it to get the Attendee or an error
     */
    public static Task<Attendee> getAttendee(String attendeeID){
        return FirebaseFirestore.getInstance()
                .collection(attendeeCollectionPath)
                .document(attendeeID).get()
                .continueWith(new Continuation<DocumentSnapshot, Attendee>() {
                    @Override
                    public Attendee then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        if(task.isSuccessful()){
                            // we can build the Attendee object here with the result
                            // may need to customize if you're copy pasting
                            return task.getResult().toObject(Attendee.class);
                        } else {
                            throw new Exception("Could not fetch Attendee "+attendeeID+" | "+task.getException());
                        }
                    }
                });
    }

    /**
     * Upload/update an attendee on the database
     * @param attendee the attendee to add/update
     * @return a task object, check it to see if the action was successful
     */
    public static Task<Void> setAttendee(Attendee attendee){
        return FirebaseFirestore.getInstance()
                .collection(attendeeCollectionPath)
                .document(attendee.getDeviceID())
                .set(attendee);
    }

    /**
     * get an Event object from the database
     * @param eventID ID of the event
     * @param fetchAttendees if set to false, the Event's attendees will all be null
     *                       this can save some query time if you know you know you don't need them
     * @param fetchOrganizer if set to false, the Event's organizer will be null
     *                       this can save some query time if you know you don't need it
     * @param fetchPoster if set to false, the Event's poster will be null
     *                    this can save some query time if you know you don't need it
     * @return a Task\<Event\> object, call getResult() on it to get the Event or an error
     */
    @NonNull
    public static Task<Event> getEvent(String eventID, boolean fetchAttendees, boolean fetchOrganizer, boolean fetchPoster){
        return FirebaseFirestore.getInstance().
                collection(eventsCollectionPath).
                document(eventID).get()
                .continueWith(new Continuation<DocumentSnapshot, Event>() {
                    @Override
                    public Event then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        if(task.isSuccessful()) {
                            EventDatabaseRepresentation databaseEvent = task.getResult()
                                    .toObject(EventDatabaseRepresentation.class);
                            // build the Event object from the EventDatabaseRepresentation object
                            // 1. queue up the async stuff
                            //  1.1 get the attendees
                            if (databaseEvent == null) {// mostly for resolving IDE warnings: exit early if we get a null response
                                throw new Exception("event " + eventID + " is null, this should not be possible");
                            }
                            ArrayList<Task<Attendee>> attendeeTasks = new ArrayList<>(); // tasks for resolving each attendee
                            if(fetchAttendees) {
                                for (int i = 0; i < databaseEvent.getAttendeeIDs().size(); i++) {
                                    attendeeTasks.add(DatabaseManager.getAttendee(databaseEvent.getAttendeeIDs().get(i)));
                                }
                            }
                            Task<Organizer> organizerTask = null;
                            //  1.2 get the Organizer
                            if(fetchOrganizer) {
                                // TODO get the organizer
                            }
                            //  1.3 get the Poster
                            if(fetchPoster) {
                                // TODO get other things like poster here
                            }
                            // 2. await the results of the tasks
                            ArrayList<Attendee> attendeesResolved = new ArrayList<>();
                            if(fetchAttendees) {
                                for (Task<Attendee> attendeeTask : attendeeTasks) {
                                    if (attendeeTask.isSuccessful()) {
                                        attendeesResolved.add(attendeeTask.getResult());
                                    } else {
                                        throw new Exception("Attendee fetch failure: "+attendeeTask.getException());
                                    }
                                }
                            } else {
                                // fill it with nulls
                                for (int i=0; i<databaseEvent.getAttendeeIDs().size(); i++){
                                    attendeesResolved.add(null);
                                }
                            }

                            Organizer eventOrganizer = null;
                            if (fetchOrganizer) {
                                eventOrganizer = organizerTask.getResult();
                            } // else eventOrganizer stays null, we're good :)

                            // TODO poster stuff

                            // 3. build the Event object finally
                            return new Event(
                                    databaseEvent.getName(),
                                    databaseEvent.getDesc(),
                                    eventOrganizer,
                                    null, // poster
                                    databaseEvent.getEventID()
                            );
                        } else {
                            throw new Exception("Could not fetch Event"+eventID+" | "+task.getException().toString());
                        }
                    }
                });
    }



    // admins and users?

}
