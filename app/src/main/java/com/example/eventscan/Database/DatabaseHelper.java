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
        // Placeholder organizer for sample events
        Organizer placeholderOrganizer = new Organizer("gojo satoru", "password");

        // Creating and adding sample events
        Event event1 = new Event("Sample Event1", "sample desc1", placeholderOrganizer, posterBitmap);
        event1.setName("Event 1");

        Event event2 = new Event("Sample Event2", "sample desc2", placeholderOrganizer, posterBitmap);
        event2.setName("Event 2");

        // Setting documents in "events" collection with specific IDs for sample events
        db.collection("events").document("placeholder event id 123").set(event1);
        db.collection("events").document("placeholder event id 456").set(event2);
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

