package com.example.eventscan.Entities;

import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

public class Event implements Serializable {
    private Location location;
    private String desc;
    private String name;
    public ArrayList<Attendee> attendees;
    private Organizer organizer;
    private Uri poster;
    private String eventID;

  
  
    // empty constructor so it works with firestore
    public Event() {};
  

    public Event(String eventName, String desc, Organizer organizer, Uri poster, String eventID) {
        this.name = eventName;
        this.desc = desc;
        this.attendees = new ArrayList<>();
        this.organizer = organizer;
        this.poster = poster;
        this.eventID = eventID;
    }



    public Location getLocation() {
        return location;
    }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public String getEventID(){
        return this.eventID;
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


    public Uri getPoster() {
        return poster;
    }

    public void setPoster(Uri poster) {
        this.poster = poster;
    }
}

