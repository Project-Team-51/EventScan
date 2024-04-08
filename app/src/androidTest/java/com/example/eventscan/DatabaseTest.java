package com.example.eventscan;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

/**
 * Testing class for Database implementation
 */
public class DatabaseTest extends Database {

    private static final DatabaseTest instance = new DatabaseTest();
    public static DatabaseTest getInstance(){
        return instance;
    }

    public DatabaseTest(){
        attendeeCollection = FirebaseFirestore.getInstance()
                .collection("test")
                .document("attendees")
                .collection("attendees");
        eventsCollection = FirebaseFirestore.getInstance()
                .collection("test")
                .document("events")
                .collection("events");
        qrLinkCollection = FirebaseFirestore.getInstance()
                .collection("test")
                .document("qr_links")
                .collection("qr_links");
        posterStorageCollection = FirebaseStorage.getInstance().getReference()
                .child("test")
                .child("posters");
        setupChildren();
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
                null, // TODO add to test once functionality is done
                eventIDTest
        );
        event1.setAttendeeLimit(15);
        Tasks.await(db.attendees.set(organizer1));
        Attendee attendee = new Attendee();
        attendee.setName("Event added test Attendee");
        attendee.setBio("event added test bio");
        attendee.setDeviceID("5910293");
        event1.checkInAttendee(attendee);
        Task<Void> setAttendeeTask = db.attendees.set(attendee);
        Tasks.await(setAttendeeTask);
        Task<Event> event1ModifiedTask = db.events.create(event1);
        Tasks.await(event1ModifiedTask);
        if(!event1ModifiedTask.isSuccessful()){
            fail(event1ModifiedTask.getException().toString());
            //throw new AssumptionViolatedException("Skipping test - firebase connection failed: "+event1ModifiedTask.getException().toString());
        }
        Event event1Modified = event1ModifiedTask.getResult();
        Task<Event> returnEvent = db.events.get(event1Modified.getEventID());
        Tasks.await(returnEvent);
        if(!returnEvent.isSuccessful()){
            fail("Complete: "+returnEvent.isComplete()+" | "+returnEvent.getException().toString());
            //throw new AssumptionViolatedException("Skipping test - firebase connection failed: "+returnEvent.getException().toString());
        }
        Event returnedEvent = returnEvent.getResult();
        assertEquals(event1Modified, returnedEvent);
        assertEquals(returnedEvent, event1Modified);
        assertNotNull(returnedEvent.getOrganizer());
        assertEquals(returnedEvent.getAttendeeLimit(), Integer.valueOf(15));
    }

    @Test
    public void event_check_in_remove_attendee() throws ExecutionException, InterruptedException {
        // create dummy data
        Database db = DatabaseTest.getInstance();
        String eventIDTest = "9876543210";
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
                null, // TODO add to test once functionality is done
                eventIDTest
        );
        Tasks.await(db.attendees.set(organizer1));
        Attendee attendee = new Attendee();
        attendee.setName("Event added test Attendee");
        attendee.setBio("event added test bio");
        attendee.setDeviceID("5910293");
        // write it to the database
        Task<Event> potentiallyUpdatedEvent = db.events.create(event1);
        Tasks.await(potentiallyUpdatedEvent);
        event1 = potentiallyUpdatedEvent.getResult();
        // check-in the attendee
        Task<Void> addAttendeeTask = db.events.checkInAttendee(event1, attendee);
        Tasks.await(addAttendeeTask);
        // get the new event
        Task<Event> updatedEventSingleCheckIn = db.events.get(event1.getEventID());
        Tasks.await(updatedEventSingleCheckIn);
        // check it in locally too, and verify they're equal
        event1.checkInAttendee(attendee);
        assertEquals(event1, updatedEventSingleCheckIn.getResult());
        // "check in" a second time and verify they're equal
        Tasks.await(db.events.checkInAttendee(event1, attendee));
        event1.checkInAttendee(attendee);
        Task<Event> updatedEventDoubleCheckIn = db.events.get(event1.getEventID());
        Tasks.await(updatedEventDoubleCheckIn);
        assertEquals(updatedEventDoubleCheckIn.getResult(), event1);
        assertNotEquals(event1, updatedEventSingleCheckIn.getResult());

        // now remove them and verify as well (also make sure they're unequal to the ones that have the attendee)
        Task<Void> removeAttendeeTask = db.events.removeCheckedInAttendee(event1, attendee);
        event1.removeCheckedInAttendee(attendee);
        Tasks.await(removeAttendeeTask);
        Task<Event> updatedEventDeletedCheckIns = db.events.get(event1.getEventID());
        Tasks.await(updatedEventDeletedCheckIns);
        assertEquals(updatedEventDeletedCheckIns.getResult(), event1);
        // (make sure they're unequal to the ones that have the attendees)
        assertNotEquals(event1, updatedEventDoubleCheckIn.getResult());
    }

    @Test
    public void event_add_remove_interested_attendee() throws ExecutionException, InterruptedException {
        // create dummy data
        Database db = DatabaseTest.getInstance();
        String eventIDTest = "207894213";
        Organizer organizer1 = new Organizer();
        organizer1.setDeviceID("12908ash98e2");
        organizer1.setPhoneNum("1029384");
        organizer1.setEmail("interested_test@abc.com");
        organizer1.setName("Interested Test Organizer");
        organizer1.setBio("Test Bio");
        organizer1.setProfilePictureID("123");
        Event event1 = new Event(
                "Test Eventfor Interested Attendee",
                "Test Description",
                organizer1,
                null, // TODO add to test once functionality is done
                eventIDTest
        );
        Tasks.await(db.attendees.set(organizer1));
        Attendee attendee = new Attendee();
        attendee.setName("Interested Test Attendee");
        attendee.setBio("Interested Test bio");
        attendee.setDeviceID("12309865");
        // add it to the DB
        Task<Event> potentiallyUpdatedEvent = db.events.create(event1);
        Tasks.await(potentiallyUpdatedEvent);
        event1 = potentiallyUpdatedEvent.getResult();
        Tasks.await(db.attendees.set(attendee));
        // now add an interested attendee both here and there
        event1.addInterestedAttendee(attendee);
        Task<Void> addInterestedAttendeeTask = db.events.addInterestedAttendee(event1, attendee);
        Tasks.await(addInterestedAttendeeTask);
        Task<Event> addedInterestEventTask = db.events.get(event1.getEventID());
        Tasks.await(addedInterestEventTask);
        assertEquals(addedInterestEventTask.getResult(), event1);
        // now remove and make sure they both are equal too (and unequal to the added ones)
        event1.removeInterestedAttendee(attendee);
        Tasks.await(db.events.removeInterestedAttendee(event1, attendee));
        Task<Event> removeInterestEventTask = db.events.get(event1.getEventID());
        Tasks.await(removeInterestEventTask);
        assertEquals(removeInterestEventTask.getResult(), event1);
        assertNotEquals(addedInterestEventTask.getResult(), event1);

    }

}
