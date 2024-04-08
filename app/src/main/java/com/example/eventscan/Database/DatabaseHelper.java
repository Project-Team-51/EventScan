package com.example.eventscan.Database;

import android.graphics.Bitmap;
import android.location.Location;
import android.media.Image;
import android.net.Uri;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * Testing class for adding sample events and users to the Firestore. As of writing,
 * it has been completely broken by the addition of URI to store event posters.
 * Do not use.
 */
public class DatabaseHelper {
    private String posterUriString;
    private FirebaseFirestore db;

    // Constructor initializes Firestore instance
    public DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Adds sample events to Firestore
    public void addSampleEvents() {
        Integer orgID = (int) Math.floor(Math.random() * 90000) + 10000;
        Organizer placeholderOrganizer = new Organizer();

        Integer eventID1 = (int) Math.floor(Math.random() * 90000) + 10000;
        String eventID12 = eventID1.toString();

        // Creating and adding sample events
        Event event1 = new Event("Sample Event1", "sample desc1", placeholderOrganizer, posterUriString, eventID12);
        event1.setName("Event 1");

        Integer eventID2 = (int) Math.floor(Math.random() * 90000) + 10000;
        String eventID22 = eventID2.toString();

        Event event2 = new Event("Sample Event2", "sample desc2", placeholderOrganizer, posterUriString, eventID22);
        event2.setName("Event 2");

        db.collection("events").document(event1.getEventID()).set(event1);
        db.collection("events").document(event2.getEventID()).set(event2);
    }

    // Adds sample users (Attendee and Organizer) to Firestore
    /*


    public void addSampleUsers() {
        // Adding a sample Attendee
        Integer attendeeID1 = (int) Math.floor(Math.random() * 90000) + 10000;
        String attendeeID12 = attendeeID1.toString();
        sampleAttendee.setDeviceID(attendeeID12);
        // Adding a sample Organizer
        Integer attendeeID2 = (int) Math.floor(Math.random() * 90000) + 10000;
        String attendeeID22 = attendeeID2.toString();
        Organizer sampleOrganizer = new Organizer(attendeeID22);
        // Setting documents in "users" collection with specific IDs for sample users
        db.collection("users").document(sampleAttendee.getDeviceID()).set(sampleAttendee);
        db.collection("users").document(sampleOrganizer.getDeviceID()).set(sampleOrganizer);
    }
         */
}

