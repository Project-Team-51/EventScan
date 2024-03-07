package com.example.eventscan.Database;

import android.graphics.Bitmap;
import android.location.Location;
import android.media.Image;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DatabaseHelper {
    private Bitmap posterBitmap;
    private FirebaseFirestore db;

    // Constructor initializes Firestore instance
    public DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Adds sample events to Firestore
    public void addSampleEvents() {
        Organizer placeholderOrganizer = new Organizer("gojo satoru", "password");

        Integer eventID1 = (int) Math.floor(Math.random() * 90000) + 10000;
        String eventID12 = eventID1.toString();

        // Creating and adding sample events
        Event event1 = new Event("Sample Event1", "sample desc1", placeholderOrganizer, posterBitmap, eventID12);
        event1.setName("Event 1");

        Integer eventID2 = (int) Math.floor(Math.random() * 90000) + 10000;
        String eventID22 = eventID2.toString();

        Event event2 = new Event("Sample Event2", "sample desc2", placeholderOrganizer, posterBitmap, eventID22);
        event2.setName("Event 2");

        db.collection("events").document(event1.getEventID()).set(event1);
        db.collection("events").document(event2.getEventID()).set(event2);
    }

    // Adds sample users (Attendee and Organizer) to Firestore
    public void addSampleUsers() {
        // Adding a sample Attendee
        Attendee sampleAttendee = new Attendee("John Doe", "device1", "john@example.com", "123456789", "Bio for John", "profile1");

        // Adding a sample Organizer
        Organizer sampleOrganizer = new Organizer("Jane Doe", "password");

        // Setting documents in "users" collection with specific IDs for sample users
        db.collection("users").document("placeholder attendee").set(sampleAttendee);
        db.collection("users").document("placeholder organizer").set(sampleOrganizer);
    }
}

