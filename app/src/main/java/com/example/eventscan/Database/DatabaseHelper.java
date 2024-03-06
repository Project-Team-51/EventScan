package com.example.eventscan.Database;

import android.graphics.Bitmap;
import android.location.Location;
import android.media.Image;

import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DatabaseHelper {
    private Bitmap posterBitmap;
    private FirebaseFirestore db;

    public DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void addSampleEvents() {

        Organizer placeholderOrganizer = new Organizer("gojo satoru", "password");

        // Create and add sample events
        Event event1 = new Event("Sample Event1", "sample desc1", placeholderOrganizer, posterBitmap);
        event1.setName("Event 1");

        Event event2 = new Event("Sample Event2", "sample desc2", placeholderOrganizer, posterBitmap);
        event2.setName("Event 2");


        db.collection("events").document("placeholder event id 123").set(event1);
        db.collection("events").document("placeholder event id 456").set(event2);

    }
}

