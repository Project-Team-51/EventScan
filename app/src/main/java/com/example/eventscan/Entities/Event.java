package com.example.eventscan.Entities;

import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

import com.example.eventscan.Database.EventDatabaseRepresentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * The event class that holds all the necessary information for an event. Provides
 * sufficient setters and getters for interfacing with the class.
 * <b>If you change this don't forget to change Database/EventDatabaseRepresentation</b>
 */
public class Event implements Serializable {
    private Location location;
    private String desc;
    private String name;
    public ArrayList<Attendee> attendees;
    private Organizer organizer;
    private String poster;
    private String eventID;

  
  
    // empty constructor so it works with firestore
    public Event() {
        this.attendees = new ArrayList<>();
    };

    /**
     * Constructs a new Event object with the specified parameters.
     *
     * @param eventName The name of the event.
     * @param desc The description of the event.
     * @param organizer The organizer of the event.
     * @param poster The URL of the poster for the event.
     * @param eventID The unique ID of the event.
     */
    public Event(String eventName, String desc, Organizer organizer, String poster, String eventID) {
        this.name = eventName;
        this.desc = desc;
        this.attendees = new ArrayList<>();
        this.organizer = organizer;
        this.poster = poster;
        this.eventID = eventID;
    }

    // getters and setters for all parameters below
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

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public EventDatabaseRepresentation convertToDatabaseRepresentation(){
        return new EventDatabaseRepresentation(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(getLocation(), event.getLocation()) && Objects.equals(getDesc(), event.getDesc()) && Objects.equals(getName(), event.getName()) && Objects.equals(getAttendees(), event.getAttendees()) && Objects.equals(getOrganizer(), event.getOrganizer()) && Objects.equals(getPoster(), event.getPoster()) && Objects.equals(getEventID(), event.getEventID());
    }

    @Override
    public int hashCode() {
        //auto-generated
        return Objects.hash(getLocation(), getDesc(), getName(), getAttendees(), getOrganizer(), getPoster(), getEventID());
    }
}

