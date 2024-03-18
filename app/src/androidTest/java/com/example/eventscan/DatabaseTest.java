package com.example.eventscan;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AssumptionViolatedException;
import org.junit.Test;
import static org.junit.Assert.*;

public class DatabaseTest extends Database {
    private static final CollectionReference attendeeCollection = FirebaseFirestore.getInstance()
            .collection("test")
            .document("attendees")
            .collection("attendees");
    private static final CollectionReference eventsCollection = FirebaseFirestore.getInstance()
            .collection("test")
            .document("events")
            .collection("events");
    private static final CollectionReference qrLinkCollection = FirebaseFirestore.getInstance()
            .collection("test")
            .document("qr_links")
            .collection("qr_links");

    @Test
    public void attendee_save_and_recover() throws InterruptedException {
        String deviceIDTest = "92813701928371";
        Attendee attendee = new Attendee();
        attendee.setName("Test Name Attendee");
        attendee.setBio("Test Bio Attendee");
        attendee.setDeviceID(deviceIDTest);
        attendee.setEmail("Test Email Attendee");
        attendee.setPhoneNum("012983013");
        Task<Void> setTask = DatabaseTest.attendees.set(attendee);
        DatabaseTest.waitForTask(setTask);
        if(!setTask.isSuccessful()){
            /* 2024-MR-18, OpenAI ChatGPT 3.5,
            "in android studio, I am writing a unit test that interfaces with firebase, how can I make the test handle an error to the firebase properly, in this case the test did not fail but did not complete, would blocked be accurate?"
            "how would I make a JUnit test blocked?"
            "what other states can a junit test be in?"
            "would assumptionFailure fit the scenario above?"
            "can I throw a testAbortedException from within my test?"

            Gave me information about the below exception, and what other states a test can be in
            */
            throw new AssumptionViolatedException("Skipping test - firebase connection failed: "+setTask.getException().toString());
        }
        // now get it back
        Task<Attendee> attendeeTask = DatabaseTest.attendees.get(deviceIDTest);
        DatabaseTest.waitForTask(attendeeTask);
        if(!attendeeTask.isSuccessful()){
            throw new AssumptionViolatedException("Skipping test - firebase connection failed: "+attendeeTask.getException().toString());
        }
        Attendee fetchedAttendee = attendeeTask.getResult();
        assertEquals(attendee, fetchedAttendee);




    }

}
