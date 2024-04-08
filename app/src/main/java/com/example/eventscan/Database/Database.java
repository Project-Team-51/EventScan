package com.example.eventscan.Database;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventscan.Entities.Announcement;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.osmdroid.util.GeoPoint;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.NotImplementedError;

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
    protected StorageReference posterStorageCollection;

    protected CollectionReference geolocationStorageCollection;

    protected CollectionReference adminCollection;
    protected CollectionReference announcementsCollection;

    public AttendeeOperations attendees;
    public EventOperations events;
    public AdminOperations admins;
    public QRCodeOperations qr_codes;
    public PosterOperations posters;
    public GeolocationOperations geolocation;
    public AnnouncementOperations announcements;


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
        posterStorageCollection = FirebaseStorage.getInstance().getReference()
                        .child("prod")
                        .child("posters");
        adminCollection = FirebaseFirestore.getInstance()
                        .collection("prod")
                        .document("admins")
                        .collection("admins");

        announcementsCollection = FirebaseFirestore.getInstance()
                .collection("prod")
                .document("announcements")
                .collection("announcements");

        geolocationStorageCollection = FirebaseFirestore.getInstance()
                        .collection("prod")
                        .document("geolocations")
                        .collection("geolocations");

        setupChildren();
    }

    public CollectionReference getEventsCollection(){return this.eventsCollection;}
    public CollectionReference getAnnouncementsCollection(){return this.announcementsCollection;}
    public CollectionReference getAttendeeCollection(){return this.attendeeCollection;}
    public CollectionReference getQrLinkCollection(){return this.attendeeCollection;}

    /**
     * Sets up self.attendees, self.events, etc...
     */
    protected void setupChildren(){
        this.attendees = new AttendeeOperations(this);
        this.events = new EventOperations(this);
        this.posters = new PosterOperations(this);
        this.qr_codes = new QRCodeOperations(this);
        this.admins = new AdminOperations(this);
        this.geolocation = new GeolocationOperations(this);
        this.announcements = new AnnouncementOperations(this);

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
                        if(!task.isSuccessful()) {
                            throw new Exception("attendee "+attendeeID+" does not exist or connection to firebase failed");
                        }
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(!documentSnapshot.exists()){
                            throw new Exception("Attendee "+attendeeID+" is not in the firebase, was it written?");
                        }
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

        /**
         * Get the list of events that this attendee is interested in
         * @param attendee the attendee to search for
         * @return the list of events that `attendee` is interested in
         */
        public Task<ArrayList<Event>> getInterestedEvents(Attendee attendee){
            return owner.eventsCollection.whereArrayContains("interestedAttendeeIDs", attendee.getDeviceID())
                    .get().continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<Task<Event>> eventFetchTasks = new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot: task.getResult().getDocuments()){
                            eventFetchTasks.add(owner.events.get((String) documentSnapshot.get("eventID")));
                        }
                        return Tasks.whenAllComplete(eventFetchTasks);
                    }).continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<Event> finalOutput = new ArrayList<>();
                        for(Task<?> eventTask: task.getResult()){
                            if(eventTask.isSuccessful()){
                                finalOutput.add((Event) eventTask.getResult());
                            }
                        }
                        return Tasks.forResult(finalOutput);
                    });
        }

        /**
         * Get the list of events that this attendee has checked into
         * @param attendee the attendee to search for
         * @return the list of events that `attendee` has checked into
         */
        public Task<ArrayList<Event>> getCheckedInEvents(Attendee attendee){
            // Microsoft Bing Chat 2024-04-07
            // "Please help me write a firebase query in java android. I have documents that contain the field "checkedInAttendeeIDs" which is a hashmap from String to int. I need to find documents where the string S is contained within the hashmap keys"
            // -> provided information about collection.whereEqualTo("field.hashMapKey", true) would return those
            // -> questioned a new instance of bing chat: "What will this query do in firestore android java? collection.whereEqualTo("checkedInAttendeeIDs."+attendee.getDeviceID(), true) where collection is a CollectionReference, and attendee.getDeviceID() is a string"
            // -> modified query to use whereGreaterThanOrEqualTo, as the `true` doesn't make sense in our use case
            // -> aside from basis of query, implementation written by me

            return owner.eventsCollection.whereGreaterThanOrEqualTo("checkedInAttendeeIDs."+attendee.getDeviceID(), 1)
                    .get().continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<Task<Event>> eventFetchTasks = new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot : task.getResult().getDocuments()){
                            eventFetchTasks.add(owner.events.get((String) documentSnapshot.get("eventID")));
                        }
                        return Tasks.whenAllComplete(eventFetchTasks);
                    }).continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<Event> finalOutput = new ArrayList<>();
                        for(Task<?> eventTask: task.getResult()){
                            if(eventTask.isSuccessful()){
                                finalOutput.add((Event) eventTask.getResult());
                            }
                        }
                        return Tasks.forResult(finalOutput);
                    });
        }

        /**
         * get the list of events that are "owned" by this attendee/organizer
         * @param organizer the attendee/organizer to search for
         * @return a task that will resolve to an ArrayList of the owned events
         */
        public Task<ArrayList<Event>> getOwnedEvents(Attendee organizer){
            return eventsCollection.whereEqualTo("organizerID", organizer.getDeviceID()).get()
                    .continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<Task<Event>> toReturnEventTasks = new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot: task.getResult().getDocuments()){
                            toReturnEventTasks.add(owner.events.get(documentSnapshot));
                        }
                        return Tasks.whenAllComplete(toReturnEventTasks);
                    }).continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<Event> toReturnEvents = new ArrayList<>();
                        for(Task<?> eventTask : task.getResult()){
                            if(eventTask.isSuccessful()){
                                toReturnEvents.add(((Task<Event>) eventTask).getResult());
                            }
                        }
                        return Tasks.forResult(toReturnEvents);
                    });
        }
        public Task<ArrayList<Event>> getNonOwnedEvents(Attendee organizer) {
            return eventsCollection.whereNotEqualTo("organizerID", organizer.getDeviceID()).get()
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<Task<Event>> toReturnEventTasks = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            toReturnEventTasks.add(owner.events.get(documentSnapshot));
                        }
                        return Tasks.whenAllComplete(toReturnEventTasks);
                    }).continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<Event> toReturnEvents = new ArrayList<>();
                        for (Task<?> eventTask : task.getResult()) {
                            if (eventTask.isSuccessful()) {
                                toReturnEvents.add(((Task<Event>) eventTask).getResult());
                            }
                        }
                        return Tasks.forResult(toReturnEvents);
                    });
        }

        /**
         * Create a unique ID for an attendee
         * @return a unique Attendee ID
         */
        public Task<String> generateUniqueUserId() {
            String randomID = ((Integer)((int)(Math.random()*10000000))).toString();
            return attendeeCollection.document(randomID)
                    .get()
                    .continueWithTask(task -> {
                        if(task.isSuccessful()) {
                            if(!task.getResult().exists()){
                                // nothing exists with this, we're good :)
                                // make a blank document here to reserve it, and return the number
                                Attendee blankAttendee = new Attendee();
                                blankAttendee.setDeviceID(randomID);
                                return owner.attendees.set(blankAttendee).continueWith(task1 -> {
                                    return randomID;
                                });
                            } else {
                                // exists, try again
                                return generateUniqueUserId();
                            }
                        } else {
                            return Tasks.forException(getTaskException(task));
                        }
                    });
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
         * @return a Task object that will resolve to an Event later (or an error)
         */
        @NonNull
        public Task<Event> get(String eventID){
            // 2024-MR-20 OpenAI ChatGPT
            // I am writing java android and have a function that returns a firebase Task<Event> where event is a custom class. There is an EventDatabaseRepresentation stored in firebase, which contains a list of attendee IDs, the function needs to return a task that will fetch all of the attendees and only resolve when all of the sub-tasks are done, how is this possible?
            // -> provided information about tasks.whenAllComplete()
            // how could I extend this if I needed to fetch other event details from the firestore, for example if the event has an organizer ID whose organizer needs to be fetched, you don't have to write the whole code just the structure
            // -> provided general structure of a task list where each task has an onCompleteListener, you add all tasks to a list, return Tasks.onComplete(list)
            // implementation written by me
            return eventsCollection.document(eventID).get().continueWithTask(task -> {
                if(!task.isSuccessful()){
                    return Tasks.forException(getTaskException(task));
                }
                ArrayList<Task<?>> tasks = new ArrayList<>();
                EventDatabaseRepresentation eventDatabaseRepresentation = task.getResult().toObject(EventDatabaseRepresentation.class);
                if(eventDatabaseRepresentation == null){
                    throw new Exception("Unknown error occured when fetching event "+eventID);
                }
                Event event = eventDatabaseRepresentation.convertToBarebonesEvent();
                // Interested attendees get added
                for(String attendeeID : eventDatabaseRepresentation.getInterestedAttendeeIDs()){
                    tasks.add(owner.attendees.get(attendeeID).addOnSuccessListener(attendee -> {
                                event.addInterestedAttendee(attendee);
                            }).addOnFailureListener(e -> {
                                event.invalidateFullyFormed();
                            })
                    );
                }
                // Checked-in attendees get added
                for(Map.Entry<String, Integer> entry: eventDatabaseRepresentation.getCheckedInAttendeeIDs().entrySet()){
                    tasks.add(owner.attendees.get(entry.getKey()).addOnSuccessListener(attendee -> {
                        event.setAttendeeCheckInCount(
                                attendee,
                                entry.getValue()
                        );
                    }).addOnFailureListener(e -> {
                        event.invalidateFullyFormed();
                    }));
                }
                // organizer gets added
                if(eventDatabaseRepresentation.getOrganizerID() != null) {
                    tasks.add(owner.attendees.get(eventDatabaseRepresentation.getOrganizerID())
                            .addOnSuccessListener(attendee -> {
                                event.setOrganizer((Organizer) attendee);
                            }).addOnFailureListener(e -> {
                                event.invalidateFullyFormed();
                            }));
                }
                // TODO fetch poster
                return Tasks.whenAllComplete(tasks).continueWith(task1 -> {
                     return event;
                });
            });
        }

        /**
         * get an event from the documentSnapshot of the document of this event
         * @param doc the firestore documentSnapshot that contains the event in question
         * @return a task that will resolve to an Event
         */
        public Task<Event> get(DocumentSnapshot doc){
            if(doc.get("eventID") != null){
                return get(doc.get("eventID").toString());
            }
            return Tasks.forException(new Exception("Tried to load an event from a non-event DocumentSnapshot"));
        }

        /**
         * Check if an eventID exists as an event
         * @param eventID the event ID to search for
         * @return a task that will resolve to a boolean of whether it exists in the DB or not
         */
        public Task<Boolean> checkExistence(String eventID){
            return eventsCollection.document(eventID).get().continueWithTask(task -> {
                if(!task.isSuccessful()){
                    return Tasks.forException(getTaskException(task));
                }
                return Tasks.forResult(task.getResult().exists());
            });
        }


        /**
         * Check an attendee into an event
         * @param event the event to add to
         * @param attendee the attendee to be added
         * @return a task that will be resolved when the adding finishes
         */
        @NonNull
        public Task<Void> checkInAttendee(@NonNull Event event, @NonNull Attendee attendee) {
            return eventsCollection
                    .document(event.getEventID())
                    .update(FieldPath.of("checkedInAttendeeIDs",attendee.getDeviceID()),FieldValue.increment(1));
        }

        /**
         * Check an attendee into an event, with a GeoPoint location
         * @param event the event to add to
         * @param attendee the attendee to be added
         * @param geoPoint the location of the attendee
         * @return a task that will be resolved when the adding finishes
         */
        public Task<List<Task<?>>> checkInAttendee(@NonNull Event event, @NonNull Attendee attendee, @NonNull GeoPoint geoPoint) {
            ArrayList<Task<Void>> allTasks = new ArrayList<>();
            allTasks.add(checkInAttendee(event, attendee));
            allTasks.add(owner.geolocation.savePointToEvent(geoPoint,event));
            allTasks.get(1).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Log.d("Geolocation", "monkey :D");
                } else {
                    Log.d("GeoLocation", getTaskException(task).toString());
                }});

            return Tasks.whenAllComplete(allTasks);
        }

        /**
         * Remove an attendee from an event's 'attendees' field
         * @param event the event to modify
         * @param attendee the attendee to remove
         * @return a task that will be resolved when the DB actions finish
         */
        @NonNull
        public Task<Void> removeCheckedInAttendee(@NonNull Event event, @NonNull Attendee attendee) {
            return eventsCollection
                    .document(event.getEventID())
                    .update(FieldPath.of("checkedInAttendeeIDs", attendee.getDeviceID()), FieldValue.delete());
        }

        @NonNull
        public Task<Void> addInterestedAttendee(@NonNull Event event, @NonNull Attendee attendee) {
            return eventsCollection
                    .document(event.getEventID())
                    .update("interestedAttendeeIDs", FieldValue.arrayUnion(attendee.getDeviceID()));
        }

        public Task<Void> removeInterestedAttendee(@NonNull Event event, @NonNull Attendee attendee) {
            return eventsCollection
                    .document(event.getEventID())
                    .update("interestedAttendeeIDs", FieldValue.arrayRemove(attendee.getDeviceID()));
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
                            return Tasks.forException(getTaskException(task));
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

    public class PosterOperations{
        private Database owner;

        private PosterOperations(Database owner){
            this.owner = owner;
        }
        Task<File> get(String posterID) throws IOException {
            File downloadDestination = File.createTempFile(posterID,"posterTemp");
            return owner.posterStorageCollection
                    .child(posterID)
                    .getFile(downloadDestination)
                    .continueWith(task -> {
                        if(task.isSuccessful()) {
                            return downloadDestination;
                        } else {
                            if(task.getException() == null){
                                throw new Exception("Unknown Error occurred");
                            }
                            throw task.getException();
                        }
                    });
        }
        UploadTask set(String posterID, Uri posterUri){
            return owner.posterStorageCollection
                    .child(posterID)
                    .putFile(posterUri);
        }
    }

    public class AdminOperations {
        private Database owner;
        private AdminOperations(Database owner) {this.owner = owner;}

        /**
         * Check if this username password combo is a valid administrator
         * @param username username to check
         * @param password password to check
         * @return A task that will resolve to true if the credentials are valid
         */
        public Task<Boolean> checkCredentials(String username, String password){
            if(username == null || password == null){
                return Tasks.forResult(false);
            }
            return owner.adminCollection
                    .whereEqualTo("user", username)
                    .whereEqualTo("pass", password)
                    .get()
                    .continueWith(task -> {
                       if(!task.isSuccessful()){
                           throw getTaskException(task);
                       }
                       // if result is null, false, otherwise only return true if not empty
                       return task.getResult() != null && !task.getResult().isEmpty();
                    });
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
         * @return a Task that contains QR data when completed
         */
        public Task<String> set(String decoded_qr_data, Event directedEvent, int linkType){
            return qrLinkCollection
                    .document(decoded_qr_data)
                    .set(new QRDatabaseEventLink(
                            linkType,
                            directedEvent
                    )).continueWith(task -> {
                        return decoded_qr_data;
                    });
        }

        /**
         * Set a QR code's direction type and destination
         * @param decoded_qr_data the decoded data of the QR code you are setting the link from
         * @param eventLink eventLink you want to set
         * @return a Task that contains QR data when completed
         */
        public Task<String> set(String decoded_qr_data, QRDatabaseEventLink eventLink){
            return qrLinkCollection
                    .document(decoded_qr_data)
                    .set(eventLink).continueWith(task -> {
                        return decoded_qr_data;
                    });
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

        /**
         * Create a unique <b>decoded</b> QR data string, for writing a link
         * @return a unique string for the decoded QR data
         */
        public Task<String> generateUniqueQrID(){
            String randomID = ((Integer)((int)(Math.random()*10000000))).toString();
            return qrLinkCollection.document(randomID)
                    .get()
                    .continueWithTask(task -> {
                        if(task.isSuccessful()) {
                            if(!task.getResult().exists()){
                                // doesn't exist, we can use this
                                // reserve just in case someone else does this at the same time
                                return owner.qr_codes.set(randomID, null, -1)
                                        .continueWith(task1 -> {
                                            return randomID;
                                        });
                            } else {
                                // already exists, try again
                                return generateUniqueQrID();
                            }
                        } else {
                            return Tasks.forException(getTaskException(task));
                        }
                    });
        }

        /**
         * Get a list of existing decoded QR strings that link to this event
         * useful if you don't want to keep generating QR codes for the same event
         * @param event the event to find existing QR codes for
         * @param linkType the link type to search for
         * @return a list of decoded QR data that links to the specified event with the specified link type
         */
        public Task<ArrayList<String>> getExistingQRsForEvent(Event event, int linkType){
            return owner.qrLinkCollection
                    .whereEqualTo("directedEventID",event.getEventID())
                    .whereEqualTo("directionType",linkType)
                    .get().continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(getTaskException(task));
                        }
                        ArrayList<String> results = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            results.add(queryDocumentSnapshot.getId());
                        }
                        return Tasks.forResult(results);
                    });

        }
    }

    public class GeolocationOperations {
        private Database owner;

        private GeolocationOperations(Database owner){
            this.owner = owner;
        }

        private Task<Void> savePointToEvent(GeoPoint geoPoint, Event event){
            return geolocationStorageCollection.document(event.getEventID()).get().continueWithTask(task -> {
                if(!task.isSuccessful()){
                    return Tasks.forException(getTaskException(task));
                }
                if(task.getResult().exists()){
                    return geolocationStorageCollection.document(event.getEventID()).update("check_in_pings",FieldValue.arrayUnion(geoPoint));
                }
                // else it doesn't exist, we need to make it and set it
                HashMap<String, ArrayList<GeoPoint>> newDocument = new HashMap<>();
                ArrayList<GeoPoint> thisPing = new ArrayList<>();
                thisPing.add(geoPoint);
                newDocument.put("check_in_pings", thisPing);
                return geolocationStorageCollection.document(event.getEventID()).set(newDocument);
            });
        }

        public Task<ArrayList<GeoPoint>> getEventCheckinPoints(Event event) {

            return geolocationStorageCollection.document(event.getEventID()).get().continueWithTask(task1 -> {
                if (!task1.isSuccessful()) {
                    return Tasks.forException(getTaskException(task1));
                }
                ArrayList<HashMap<String, ?>> fetchedData = (ArrayList<HashMap<String, ?>>) task1.getResult().get("check_in_pings");
                ArrayList<GeoPoint> output = new ArrayList<>();
                if(fetchedData == null){
                    return Tasks.forResult(output);
                }
                for(HashMap<String, ?> entry: fetchedData){
                    Double latitude;
                    Double longitude;
                    if(entry.get("latitude") != null && entry.get("latitude") instanceof Double){
                        latitude = (Double) entry.get("latitude");
                    } else {
                        latitude = 0.;
                    }
                    if(entry.get("longitude") != null && entry.get("longitude") instanceof Double){
                        longitude = (Double) entry.get("longitude");
                    } else {
                        longitude = 0.;
                    }
                    if(entry.get("latitude") != null && entry.get("longitude") != null) {
                        output.add(new GeoPoint(latitude, longitude));
                    }
                }
                return Tasks.forResult(output);
            });
        }
    }

    public class AnnouncementOperations {
        private Database owner;
        private AnnouncementOperations(Database owner){
            this.owner = owner;
        }

        public Task<Void> saveNotification(Event event, Announcement announcement){
            return owner.announcementsCollection.document(event.getEventID()).get().continueWithTask(task -> {
                if(!task.isSuccessful()){
                    return Tasks.forException(getTaskException(task));
                }
                if(task.getResult().exists()){
                    HashMap<String,Announcement> fetchedAnnouncements = (HashMap<String,Announcement>)task.getResult().get("announcements");
                    Long newAnnouncementIndex = (long) fetchedAnnouncements.keySet().size();
                    return owner.announcementsCollection.document(event.getEventID()).update(
                            FieldPath.of("announcements",String.valueOf(newAnnouncementIndex)),
                            announcement);
                }
                // create a new one
                HashMap<String, HashMap<String,Announcement>> newDocument = new HashMap<>();
                HashMap<String,Announcement> newDocumentAnnouncements = new HashMap<>();
                newDocumentAnnouncements.put("0", announcement);
                newDocument.put("announcements", newDocumentAnnouncements);
                return owner.announcementsCollection.document(event.getEventID()).set(newDocument);
            });
        }

        public Task<ArrayList<Announcement>> getNotifications(Event event){
            return owner.announcementsCollection.document(event.getEventID())
                    .get().continueWithTask(task -> {
                        if(!task.isSuccessful()){
                            return Tasks.forException(getTaskException(task));
                        }
                        HashMap<String,Announcement> fetchedAnnouncements = (HashMap<String, Announcement>)task.getResult().get("announcements");
                        ArrayList<Announcement> toReturnAnnouncements = new ArrayList<>();
                        for(long i=0L; i<fetchedAnnouncements.keySet().size(); i++){
                            toReturnAnnouncements.add(fetchedAnnouncements.get(String.valueOf(i)));
                        }
                        return Tasks.forResult(toReturnAnnouncements);
                    });
        }
    }

    public static Exception getTaskException(Task<?> task){
        return (task.getException() != null) ? task.getException() : new Exception("Unknown Error Occurred");
    }

}
