package com.example.eventscan.Entities;

public class Organizer extends User {

    // empty constructor so it works with firestore
    public Organizer(){};


    public Organizer(String userID) {
        this.setDeviceID(userID);
    }
}
