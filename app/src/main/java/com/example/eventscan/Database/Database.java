package com.example.eventscan.Database;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

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
    protected CollectionReference attendeeCollection;
    protected CollectionReference eventsCollection;
    protected CollectionReference qrLinkCollection;
    private final String storageRootFolder = "prod";
    private final String postersStoragePath = "posters";

    public AttendeeOperations attendees;
    public EventOperations events;
    public QRCodeOperations qr_codes;


    private static final Database instance = new Database();

    protected Database(){
        attendeeCollection = FirebaseFirestore.getInstance()
                .collection("prod")
                .document("attendees")
                .collection("attendees");
        eventsCollection = FirebaseFirestore.getInstance()
                .collection("prod")
                .document("events")
                .collection("events");
        qrLinkCollection = FirebaseFirestore.getInstance()
                .collection("prod")
                .document("qr_links")
                .collection("qr_links");
        setupChildren();
    }

    /**
     * Sets up self.attendees, self.events, etc...
     */
    protected void setupChildren(){
        this.attendees = new AttendeeOperations(this);
        this.events = new EventOperations(this);
    }
    public static Database getInstance(){
        return instance;
    }

    public class AttendeeOperations{
        private Database owner;
        private AttendeeOperations(Database owner){
            this.owner = owner;
        }
        /**
         * Get an object that may contain an Attendee in the future.
         * Call this as early as possible, then call foo.getResult() later to get the request's output
         * give as much space as possible between calling this and calling .getResult() so that the request can process
         * @param attendeeID the ID of the attendee to fetch
         * @return a Task object, will contain an Attendee when done, or an error
         */
        public Task<Attendee> get(String attendeeID){
            return attendeeCollection
                    .document(attendeeID).get()
                    .continueWith(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.get("type") == null){
                            throw new Exception("Malformed attendee in firebase: " +attendeeID);
                        }
                        if(documentSnapshot.get("type").toString().equals("organizer")){
                            return documentSnapshot.toObject(Organizer.class);
                        }
                        return documentSnapshot.toObject(Attendee.class);
                    });
        }

        /**
         * Upload/update an attendee on the database
         * @param attendee the attendee to add/update
         * @return a task object, check it to see if the action was successful
         */
        public Task<Void> set(Attendee attendee){
            return attendeeCollection
                    .document(attendee.getDeviceID())
                    .set(attendee);
        }

        /**
         * Delete an attendee from the database
         * @param attendee the attendee to delete
         * @return a task that will be resolved when the operation finishes
         */
        public Task<Void> delete(Attendee attendee){
            return attendeeCollection
                    .document(attendee.getDeviceID())
                    .delete();
        }
    }

    public class EventOperations {
        private Database owner;
        private EventOperations(Database owner){
            this.owner = owner;
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
         * @return a Task\<Event\> object that will resolve to an Event later (or an error)
         */
        @NonNull
        public Task<Event> get(String eventID, boolean fetchAttendees, boolean fetchOrganizer, boolean fetchPoster){
            // 2024-MR-20 OpenAI ChatGPT
            // I am writing java android and have a function that returns a firebase Task<Event> where event is a custom class. There is an EventDatabaseRepresentation stored in firebase, which contains a list of attendee IDs, the function needs to return a task that will fetch all of the attendees and only resolve when all of the sub-tasks are done, how is this possible?
            // -> provided information about tasks.whenAllComplete()
            // how could I extend this if I needed to fetch other event details from the firestore, for example if the event has an organizer ID whose organizer needs to be fetched, you don't have to write the whole code just the structure
            // -> provided general structure of a task list where each task has an onCompleteListener, you add all tasks to a list, return Tasks.onComplete(list)
            // implementation written by me
            return eventsCollection.document(eventID).get().continueWithTask(task -> {
                if(!task.isSuccessful()){
                    if(task.getException() == null){
                        return Tasks.forException(new Exception("Unknown Error occurred"));
                    }
                    return Tasks.forException(task.getException());
                }
                ArrayList<Task<?>> tasks = new ArrayList<>();
                EventDatabaseRepresentation eventDatabaseRepresentation = task.getResult().toObject(EventDatabaseRepresentation.class);
                if(eventDatabaseRepresentation == null){
                    throw new Exception("Unknown error occured when fetching event "+eventID);
                }
                Event event = eventDatabaseRepresentation.convertToBarebonesEvent();
                // attendees get added
                for(String attendeeID:eventDatabaseRepresentation.getAttendeeIDs()){
                    tasks.add(owner.attendees.get(attendeeID).addOnCompleteListener(task1 -> {
                                event.addAttendee(task1.getResult());
                            })

                    );
                }
                // organizer gets added
                tasks.add(owner.attendees.get(eventDatabaseRepresentation.getOrganizerID())
                        .addOnCompleteListener(task1 -> {
                            Attendee attendee = task1.getResult();
                            event.setOrganizer((Organizer) attendee);
                        }));
                // TODO fetch poster
                return Tasks.whenAllComplete(tasks).continueWith(task1 -> {
                    return event;
                });
            });
        }
        /**
         * get an Event object from the database
         * @param eventID ID of the event
         * @return a Task\<Event\> object, call getResult() on it to get the Event or an error
         */
        @NonNull
        public Task<Event> get(String eventID){
            return get(eventID, true, true, true);
        }


        /**
         * Add an attendee to an event if they aren't already on it
         * @param event the event to add to
         * @param attendee the attendee to be added
         * @return a task that will be resolved when the adding finishes
         */
        @NonNull
        public Task<Void> addAttendee(@NonNull Event event, @NonNull Attendee attendee) {
            return eventsCollection
                    .document(event.getEventID())
                    .update("attendeeIDs", FieldValue.arrayUnion(attendee.getDeviceID()));
        }

        /**
         * Remove an attendee from an event's 'attendees' field
         * @param event the event to modify
         * @param attendee the attendee to remove
         * @return a task that will be resolved when the DB actions finish
         */
        @NonNull
        public Task<Void> removeAttendee(@NonNull Event event, @NonNull Attendee attendee) {
            return eventsCollection
                    .document(event.getEventID())
                    .update("attendeeIDs", FieldValue.arrayRemove(attendee.getDeviceID()));
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
        public Task<Event> create(Event event){
            //https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection

            // 2024-MR-11, ChatGPT, OpenAI, prompt: "what's the difference between ContinueWith and ContinueWithTask?"
            // provided information that continueWithTask is for asynchronous work after a task's completion
            return eventsCollection
                    .document(event.getEventID())
                    .get()
                    .continueWithTask(task -> {
                        if(task.isSuccessful()){
                            if(!task.getResult().exists()){
                                // nothing exists with this ID, we're good :)
                                return eventsCollection
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

        /**
         * delete an event from the database
         * @param event event to delete
         * @return a task that will be resolved when the database actions are done
         */
        public Task<Void> delete(Event event){
            return eventsCollection
                    .document(event.getEventID())
                    .delete();
        }

    }

    private class posters{
        //TODO better references, owner class
        FileDownloadTask get(String posterID, Uri destinationURI){
            return FirebaseStorage.getInstance().getReference()
                    .child(storageRootFolder)
                    .child(postersStoragePath)
                    .child(posterID)
                    .getFile(destinationURI);

        }
        UploadTask set(String posterID, Uri posterUri){
            return FirebaseStorage.getInstance().getReference()
                    .child(storageRootFolder)
                    .child(postersStoragePath)
                    .child(posterID)
                    .putFile(posterUri);
        }
    }

    public class QRCodeOperations {
        private Database owner;

        private QRCodeOperations(Database owner){
            this.owner = owner;
        }
        // set(qr_data, event) makes it so scanning a QR with that data will send you to that event

        /**
         * get a QR code's direction type and destination
         * @param decoded_qr_data the <b>decoded</b> qr data
         * @return a task that should resolve to a QRDatabaseRedirection object
         */
        public Task<QRDatabaseEventLink> get(String decoded_qr_data){
            return qrLinkCollection
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
            return qrLinkCollection
                    .document(decoded_qr_data)
                    .set(new QRDatabaseEventLink(
                            linkType,
                            directedEvent
                    ));
        }

        /**
         * delete the link between this QR code and anything it points to
         * @param decoded_qr_data decoded data of the QR code to delete the link from
         * @return a task that will be resolved when the database write is complete or failed
         */
        public Task<Void> delete(String decoded_qr_data){
            return qrLinkCollection
                    .document(decoded_qr_data)
                    .delete();
        }
    }


}
