package com.example.eventscan.Entities;

import java.util.ArrayList;

public class Organizer extends User {
    //public ArrayList<Event> ownedEvents;
    // empty constructor so it works with firestore

    public Organizer(){};


    public Organizer(String userID) {
        this.setDeviceID(userID);
    }
}
