package com.example.eventscan.Entities;

import android.media.Image;

import java.util.ArrayList;

public class Event {
    private Location location;
    private String desc;
    private String name;
    public ArrayList<Attendee> attendees;
    private Organizer organizer;
    private Image poster;

    // Constructor
    public Event(Location location, String desc, Organizer organizer, Image poster) {
        this.location = location;
        this.desc = desc;
        this.attendees = new ArrayList<>();
        this.organizer = organizer;
        this.poster = poster;
    }

    // Getter and Setter methods for Location
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // Getter and Setter methods for Description
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() { return name; }

    // Method to add an attendee to the event
    public void addAttendee(Attendee attendee) {
        attendees.add(attendee);
    }

    // Method to remove an attendee from the event
    public void removeAttendee(Attendee attendee) {
        attendees.remove(attendee);
    }

    // Getter method for Attendees
    public ArrayList<Attendee> getAttendees() {
        return attendees;
    }

    // Getter and Setter methods for Organizer
    public Organizer getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    // Getter and Setter methods for Poster
    public Image getPoster() {
        return poster;
    }

    public void setPoster(Image poster) {
        this.poster = poster;
    }


}

