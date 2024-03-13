package com.example.eventscan.Entities;

import java.util.ArrayList;
/*
 * Organizer subclass of the user class. As is, doesnt have any extra features.
 */
public class Organizer extends User {
    //public ArrayList<Event> ownedEvents;
    // empty constructor so it works with firestore

    public Organizer(){};
    public String type() {
        return "organizer";
    }
}
