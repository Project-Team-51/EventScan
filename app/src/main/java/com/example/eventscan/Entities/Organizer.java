package com.example.eventscan.Entities;

import java.util.ArrayList;
/*
 * Organizer subclass of the user class. As is, doesnt have any extra features.
 */
public class Organizer extends Attendee {
    //public ArrayList<Event> ownedEvents;
    // empty constructor so it works with firestore
    public Organizer(){
        this.type = "organizer";
    };
}
