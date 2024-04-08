package com.example.eventscan.Entities;

import java.util.ArrayList;
/**
 * Organizer subclass of the user class. As is, doesnt have any extra features. The ownedEvents attribute and getter
 *  is only for testing purposes, and will be removed in a future build.
 */

public class Administrator extends User{
    public ArrayList<Event> ownedEvents;
    public Administrator(String name, String password) {
        super(name, password);
    }
    public ArrayList<Event> getOwnedEvents(){ return this.ownedEvents; }
}
