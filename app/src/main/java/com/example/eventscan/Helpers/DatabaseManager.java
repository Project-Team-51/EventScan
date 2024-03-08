package com.example.eventscan.Helpers;

import androidx.annotation.NonNull;

import com.example.eventscan.Entities.Attendee;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//references
//https://jc1175.medium.com/a-crash-course-in-java-futures-cebae805cebb
//https://www.baeldung.com/java-future

/**
 * Helper class for easily pulling/pushing data from/to the firestore
 */
public class DatabaseManager {
    // References
    // Bing Copilot, 2024-03-08, "how can I turn a FirebaseFirestore document search into a future in java android" -> "I have a document containing the data needed to build an object, I want the future to return a built object"
    // gave me information on how to use Task.continueWith(new Continuation...)
    // implementation written by us though

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    public static class attendees {
        public static Task<Attendee> getAttendee(String AttendeeID){
            return FirebaseFirestore.getInstance()
                    .collection("attendees")
                    .document(AttendeeID).get()
                    .continueWith(new Continuation<DocumentSnapshot, Attendee>() {
                        @Override
                        public Attendee then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            if(task.isSuccessful()){
                                // we can build the Attendee object here with the result TODO vvvv
                                return new Attendee();
                            } else {
                                throw new Exception("Could not fetch Attendee "+AttendeeID+" | "+task.getException());
                            }
                        }
                    });
        }
    }

    public static class events {

    }

    // admins and users?

}
