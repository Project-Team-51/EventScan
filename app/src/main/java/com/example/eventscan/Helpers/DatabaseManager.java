package com.example.eventscan.Helpers;

import androidx.annotation.NonNull;

import com.example.eventscan.Entities.Attendee;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Helper class for easily pulling/pushing data from/to the firestore
 * call the getter functions as early as possible,
 * use xyz.get() to resolve them, do this as late as possible
 */
public class DatabaseManager {
    // References
    // Bing Copilot (Bing Chat), 2024-MR-08, "how can I turn a FirebaseFirestore document search into a future in java android" -> "I have a document containing the data needed to build an object, I want the future to return a built object"
    // gave me information on how to use Task.continueWith(new Continuation...)
    // implementation written by us though

    public static class attendees {
        private static final String attendeeCollectionPath = "attendees"; // easier to change here if we refactor the DB later

        /**
         * Get an object that may contain an Attendee in the future.
         * Call this as early as possible, then call foo.get() later to get the request's output
         * give as much space as possible between calling this and calling .get() so that the request can process
         * @param AttendeeID the ID of the attendee to fetch
         * @return a Task\<Attendee\> object, will contain an attendee or an error once the request finishes
         */
        public static Task<Attendee> getAttendee(String AttendeeID){
            return FirebaseFirestore.getInstance()
                    .collection(attendeeCollectionPath)
                    .document(AttendeeID).get()
                    .continueWith(new Continuation<DocumentSnapshot, Attendee>() {
                        @Override
                        public Attendee then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            if(task.isSuccessful()){
                                // we can build the Attendee object here with the result
                                // may need to customize if you're copy pasting
                                return task.getResult().toObject(Attendee.class);
                            } else {
                                throw new Exception("Could not fetch Attendee "+AttendeeID+" | "+task.getException());
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
    }

    public static class events {
        private static final String eventsCollectionPath = "events";


    }

    // admins and users?

}
