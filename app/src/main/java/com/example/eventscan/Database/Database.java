package com.example.eventscan.Database;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Helper class for easily pulling/pushing data from/to the database
 * call the getter functions as early as possible,
 * use foo.getResult() to resolve them, do this as late as possible
 */
public class Database {
    // References
    // Bing Copilot (Bing Chat), 2024-MR-08, "how can I turn a FirebaseFirestore document search into a future in java android" -> "I have a document containing the data needed to build an object, I want the future to return a built object"
    // gave me information on how to use Task.continueWith(new Continuation...)
    // implementation written by us though

    /*
    TODO find out if database calls block the main thread, if so, look into Executors
     https://www.baeldung.com/java-future (they can be used with ContinueWith and ContinueWithTask)
     */

    private static final String attendeeCollectionPath = "prod/attendees"; // easier to change here if we refactor the DB later
    private static final String eventsCollectionPath = "prod/events";
    private static final String qrDirectionCollectionPath = "prod/qr_codes";
    private static final String storageRootFolder = "prod";
    private static final String postersStoragePath = "posters";

    public static class attendees{
        /**
         * Get an object that may contain an Attendee in the future.
         * Call this as early as possible, then call foo.getResult() later to get the request's output
         * give as much space as possible between calling this and calling .getResult() so that the request can process
         * @param attendeeID the ID of the attendee to fetch
         * @return a Task\<Attendee\> object, call getResult() on it to get the Attendee or an error
         */
        public static Task<Attendee> get(String attendeeID){
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
        public static Task<Void> set(Attendee attendee){
            return FirebaseFirestore.getInstance()
                    .collection(attendeeCollectionPath)
                    .document(attendee.getDeviceID())
                    .set(attendee);
        }
    }

    public static class events {
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
        public static Task<Event> get(String eventID, boolean fetchAttendees, boolean fetchOrganizer, boolean fetchPoster){
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
                                        attendeeTasks.add(Database.attendees.get(databaseEvent.getAttendeeIDs().get(i)));
                                    }
                                }
                                Task<Attendee> organizerTask = null;
                                //  1.2 get the Organizer
                                if(fetchOrganizer) {
                                    organizerTask = Database.attendees.get(databaseEvent.getOrganizerID());
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
                                    try {
                                        eventOrganizer = (Organizer) organizerTask.getResult();
                                    } catch(ClassCastException e){
                                        throw new Exception("The organizer of this event is not an organizer");
                                    }
                                } // else eventOrganizer stays null, we're good :)

                                // TODO poster stuff

                                // 3. build the Event object finally
                                Event event = new Event(
                                        databaseEvent.getName(),
                                        databaseEvent.getDesc(),
                                        eventOrganizer,
                                        null, // poster
                                        databaseEvent.getEventID()
                                );
                                for(Attendee attendee : attendeesResolved){
                                    event.addAttendee(attendee);
                                }
                                return event;
                            } else {
                                throw Objects.requireNonNull(task.getException());
                            }
                        }
                    });
        }
        /**
         * get an Event object from the database
         * @param eventID ID of the event
         * @return a Task\<Event\> object, call getResult() on it to get the Event or an error
         */
        @NonNull
        public static Task<Event> get(String eventID){
            return get(eventID, true, true, true);
        }


        /**
         * Add an attendee to an event if they aren't already on it
         * @param eventDatabaseRepresentation the event to add to
         * @param attendee the attendee to be added
         * @return a task that will be resolved when the adding finishes
         */
        @NonNull
        public static Task<Void> addAttendee(@NonNull EventDatabaseRepresentation eventDatabaseRepresentation, @NonNull Attendee attendee) {
            //https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array
            return FirebaseFirestore.getInstance()
                    .collection(eventsCollectionPath)
                    .document(eventDatabaseRepresentation.getEventID())
                    .update("attendees", FieldValue.arrayUnion(attendee));
        }
        /**
         * Add an attendee to an event if they aren't already on it
         * @param event the event to add to
         * @param attendee the attendee to be added
         * @return a task that will be resolved when the adding finishes
         */
        @NonNull
        public static Task<Void> addAttendee(@NonNull Event event, @NonNull Attendee attendee) {
            EventDatabaseRepresentation eventDatabaseRepresentation = event.convertToDatabaseRepresentation();
            return FirebaseFirestore.getInstance()
                    .collection(eventsCollectionPath)
                    .document(eventDatabaseRepresentation.getEventID())
                    .update("attendees", FieldValue.arrayUnion(attendee));
        }

        /**
         * create an event on the database, with a guarantee that it has a unique ID
         * <p>
         * <b>The returned task will contain an event with an potentially updated ID, be sure to double check it if necessary</b>
         * <p>
         * use setEvent or a specific setter/adder to update an already existing event
         * @param event event to create
         * @return a task that will be resolved when the database write is completed or failed,
         * it will contain the Event object you passed in with a potentially updated ID
         */
        @NonNull
        public static Task<Event> create(Event event){
            //https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection

            // 2024-MR-11, ChatGPT, OpenAI, prompt: "what's the difference between ContinueWith and ContinueWithTask?"
            // provided information that continueWithTask is for asynchronous work after a task's completion
            return FirebaseFirestore.getInstance()
                    .collection(eventsCollectionPath)
                    .document(event.getEventID())
                    .get()
                    .continueWithTask(task -> {
                        if(task.isSuccessful()){
                            if(!task.getResult().exists()){
                                // nothing exists with this ID, we're good :)
                                return FirebaseFirestore.getInstance()
                                        .document(event.getEventID())
                                        .set(event.convertToDatabaseRepresentation())
                                        .continueWith(task1 -> {
                                            return event;
                                        });
                            } else {
                                // already exists :( regenerate ID and try again
                                // TODO move the regeneration to somewhere it makes more sense and also make it better
                                String randomID = String.valueOf((int) (Math.random()*10000));
                                event.setEventID(randomID);
                                return create(event); // recurse with new ID
                            }
                        } else {
                            Exception taskException = task.getException();
                            if(taskException != null){
                                throw taskException;
                            }
                            throw new Exception("Unknown Error Occurred");
                        }
                    });
        }


    }

    private static class posters{
        static FileDownloadTask get(String posterID, Uri destinationURI){
            return FirebaseStorage.getInstance().getReference()
                    .child(storageRootFolder)
                    .child(postersStoragePath)
                    .child(posterID)
                    .getFile(destinationURI);

        }
        static UploadTask set(String posterID, Uri posterUri){
            return FirebaseStorage.getInstance().getReference()
                    .child(storageRootFolder)
                    .child(postersStoragePath)
                    .child(posterID)
                    .putFile(posterUri);
        }
    }

    public static class qr_codes {
        // set(qr_data, event) makes it so scanning a QR with that data will send you to that event

        /**
         * get a QR code's direction type and destination
         * @param decoded_qr_data the <b>decoded</b> qr data
         * @return a task that should resolve to a QRDatabaseRedirection object
         */
        public Task<QRDatabaseEventLink> get(String decoded_qr_data){
            return FirebaseFirestore.getInstance()
                    .collection(qrDirectionCollectionPath)
                    .document(decoded_qr_data)
                    .get()
                    .continueWith(task ->{
                        return task.getResult().toObject(QRDatabaseEventLink.class);
                    });
        }

        /**
         * Set a QR code's direction type and destination
         * @param decoded_qr_data the decoded data of the QR code you are setting the link from
         * @param directedEvent the event you are setting the link to
         * @param linkType the type of link.
         *                 Use QRDatabaseEventLink.DIRECT_SIGN_IN, or
         *                     QRDatabaseEventLink.DIRECT_SEE_DETAILS when setting this
         * @return a Task that will be resolved when the database write is complete or failed
         */
        public Task<Void> set(String decoded_qr_data, Event directedEvent, int linkType){
            return FirebaseFirestore.getInstance()
                    .collection(qrDirectionCollectionPath)
                    .document(decoded_qr_data)
                    .set(new QRDatabaseEventLink(
                            linkType,
                            directedEvent
                    ));
        }
    }


}
