package com.example.eventscan.Entities;

import java.util.ArrayList;

public class Administrator extends User{
    public ArrayList<Event> ownedEvents;
    public Administrator(String name, String password) {
        super(name, password);
    }
    public ArrayList<Event> getOwnedEvents(){ return this.ownedEvents; }
}
