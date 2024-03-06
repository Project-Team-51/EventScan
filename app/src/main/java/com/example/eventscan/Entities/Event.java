package com.example.eventscan.Entities;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.ArrayList;

public class Event {
    private Location location;
    private String desc;
    private String name;
    public ArrayList<Attendee> attendees;
    private Organizer organizer;
    private Bitmap poster;
  
  
    // empty constructor so it works with firestore
    public Event() {};
  
    public Event(String eventName, String desc, Organizer organizer, Bitmap poster) {
        this.name = eventName;

        this.desc = desc;
        this.attendees = new ArrayList<>();
        this.organizer = organizer;
        this.poster = poster;
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addAttendee(Attendee attendee) {
        attendees.add(attendee);
    }

    public void removeAttendee(Attendee attendee) {
        attendees.remove(attendee);
    }

    public ArrayList<Attendee> getAttendees() {
        return attendees;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }
}

