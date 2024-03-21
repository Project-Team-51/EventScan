package com.example.eventscan;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AssumptionViolatedException;
import org.junit.Test;
import static org.junit.Assert.*;

import android.util.Log;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class DatabaseTest extends Database {

    public static Database getInstance(){
        return Database.getInstance();
    }


    @Test
    public void attendee_save_and_recover() throws ExecutionException, InterruptedException {
        Database db = DatabaseTest.getInstance();
        String deviceIDTest = "92813701928371";
        Attendee attendee = new Attendee();
        attendee.setName("Test Name Attendee");
        attendee.setBio("Test Bio Attendee");
        attendee.setDeviceID(deviceIDTest);
        attendee.setEmail("Test Email Attendee");
        attendee.setPhoneNum("012983013");
        Task<Void> setTask = db.attendees.set(attendee);
        Tasks.await(setTask);
        if(!setTask.isSuccessful()){
            /* 2024-MR-18, OpenAI ChatGPT 3.5,
            "in android studio, I am writing a unit test that interfaces with firebase, how can I make the test handle an error to the firebase properly, in this case the test did not fail but did not complete, would blocked be accurate?"
            "how would I make a JUnit test blocked?"
            "what other states can a junit test be in?"
            "would assumptionFailure fit the scenario above?"
            "can I throw a testAbortedException from within my test?"

            Gave me information about the below exception, and what other states a test can be in
            */
            // updated with https://www.baeldung.com/junit-fail
            fail(setTask.getException().toString());
            //throw new AssumptionViolatedException("Skipping test - firebase connection failed: "+setTask.getException().toString());
        }
        // now get it back
        Task<Attendee> attendeeTask = db.attendees.get(deviceIDTest);
        Tasks.await(attendeeTask);
        if(!attendeeTask.isSuccessful()){
            fail(attendeeTask.getException().toString());
            //throw new AssumptionViolatedException("Skipping test - firebase connection failed: "+attendeeTask.getException().toString());
        }
        Attendee fetchedAttendee = attendeeTask.getResult();
        assertEquals(attendee, fetchedAttendee);
        assertEquals(fetchedAttendee,attendee);
    }
    @Test
    public void organizer_save_and_recover() throws ExecutionException, InterruptedException {
        Database db = DatabaseTest.getInstance();
        String deviceIDTest = "98765";
        Organizer organizer = new Organizer();
        organizer.setName("Test Name Organizer");
        organizer.setBio("Test Bio Organizer");
        organizer.setDeviceID(deviceIDTest);
        organizer.setEmail("Test Email Organizer");
        organizer.setPhoneNum("1902380");
        Task<Void> setTask = db.attendees.set(organizer);
        Tasks.await(setTask);
        if(!setTask.isSuccessful()){
            fail(setTask.getException().toString());
        }
        // now get it back
        Task<Attendee> attendeeTask = db.attendees.get(deviceIDTest);
        Tasks.await(attendeeTask);
        if(!attendeeTask.isSuccessful()){
            fail(attendeeTask.getException().toString());
        }
        Organizer fetchedOrganizer = (Organizer) attendeeTask.getResult();
        assertEquals(organizer, fetchedOrganizer);
        assertEquals(fetchedOrganizer,organizer);

    }
    @Test
    public void event_save_and_recover() throws ExecutionException, InterruptedException {
        Database db = DatabaseTest.getInstance();
        String eventIDTest = "129038712039";
        Organizer organizer1 = new Organizer();
        organizer1.setDeviceID("abc123");
        organizer1.setPhoneNum("123456");
        organizer1.setEmail("abc@abc.com");
        organizer1.setName("Test Test");
        organizer1.setBio("Test Bio");
        organizer1.setProfilePictureID("123");
        Event event1 = new Event(
                "Test Event",
                "Test Description",
                organizer1,
                "Poster? This will be changed in the future",
                eventIDTest
        );
        db.attendees.set(organizer1);
        Task<Event> event1ModifiedTask = db.events.create(event1);
        Tasks.await(event1ModifiedTask);
        if(!event1ModifiedTask.isSuccessful()){
            fail(event1ModifiedTask.getException().toString());
            //throw new AssumptionViolatedException("Skipping test - firebase connection failed: "+event1ModifiedTask.getException().toString());
        }
        Event event1Modified = event1ModifiedTask.getResult();
        Task<Event> returnEvent = db.events.get(eventIDTest);
        Tasks.await(returnEvent);
        if(!returnEvent.isSuccessful()){
            fail("Complete: "+returnEvent.isComplete()+" | "+returnEvent.getException().toString());
            //throw new AssumptionViolatedException("Skipping test - firebase connection failed: "+returnEvent.getException().toString());
        }
        Event returnedEvent = returnEvent.getResult();
        assertEquals(event1Modified, returnedEvent);
        assertEquals(returnedEvent, event1Modified);
    }

}