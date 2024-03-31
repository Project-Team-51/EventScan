package com.example.eventscan.Entities;

import android.location.Location;

import java.util.HashMap;

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
    private HashMap<Attendee, Integer> checkedInAttendees;
    private ArrayList<Attendee> interestedAttendees;
    private Organizer organizer;
    private String poster;
    private String eventID;

  
  
    // empty constructor so it works with firestore
    public Event() {
        this.checkedInAttendees = new HashMap<>();
        this.interestedAttendees = new ArrayList<>();
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
        this.checkedInAttendees = new HashMap<>();
        this.interestedAttendees = new ArrayList<>();
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

    /**
     * "check in" an attendee, the event keeps track of how many times someone has been checked in
     * @param attendee the attendee to check in
     */
    public void checkInAttendee(Attendee attendee) {
        this.checkedInAttendees.merge(attendee, 1, Integer::sum);
    }

    /**
     * specify a specific check-in count for an attendee to this event
     * @param attendee the attendee to set the check-in count of
     * @param checkInCount the count they should have
     */
    public void setAttendeeCheckInCount(Attendee attendee, int checkInCount) {
        this.checkedInAttendees.put(attendee, checkInCount);
    }

    /**
     * remove any "check in" count this attendee may have
     * @param attendee the attendee to "erase"
     */
    public void removeCheckedInAttendee(Attendee attendee) {
        this.checkedInAttendees.remove(attendee);
    }

    /**
     * get an ArrayList of the currently checked in attendees, <b>Does not contain the count</b>
     * This only exists for legacy reasons, use getCheckedInAttendees() instead to get their count too
     * @return an ArrayList of attendees that have been checked in at least once.
     */
    public ArrayList<Attendee> getCheckedInAttendeesList() {
        ArrayList<Attendee> output = new ArrayList<>();
        for(Attendee attendee: this.checkedInAttendees.keySet()){
            if(this.checkedInAttendees.get(attendee) > 0){
                output.add(attendee);
            } else {
                throw new RuntimeException("There is a checked in attendee with a <= 0 check-in count, this should not be possible");
            }
        }
        return output;
    }

    /**
     * @return a hashmap that maps attendee object to check-in count
     */
    public HashMap<Attendee, Integer> getCheckedInAttendees(){
        return this.checkedInAttendees;
    }

    /**
     * add an attendee to the list of interested attendees for this event,
     * only if it hasn't been added previously
     * @param attendee the attendee to be added to the interested attendees list
     */
    public void addInterestedAttendee(Attendee attendee){
        if(!this.interestedAttendees.contains(attendee)) {
            this.interestedAttendees.add(attendee);
        }
    }

    /**
     * remove an attendee from the list of attendees interested in this event
     * @param attendee the attendee to remove
     */
    public void removeInterestedAttendee(Attendee attendee){
        this.interestedAttendees.remove(attendee);
    }

    /**
     * @return the list of attendees interested in this event
     */
    public ArrayList<Attendee> getInterestedAttendees(){
        return this.interestedAttendees;
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
        return Objects.equals(getLocation(), event.getLocation())
                && Objects.equals(getDesc(), event.getDesc())
                && Objects.equals(getName(), event.getName())
                && Objects.equals(getCheckedInAttendees(), event.getCheckedInAttendees())
                && Objects.equals(getOrganizer(), event.getOrganizer())
                && Objects.equals(getPoster(), event.getPoster())
                && Objects.equals(getEventID(), event.getEventID())
                && Objects.equals(getInterestedAttendees(), event.getInterestedAttendees());
    }

    @Override
    public int hashCode() {
        //auto-generated
        return Objects.hash(getLocation(), getDesc(), getName(), getCheckedInAttendeesList(), getOrganizer(), getPoster(), getEventID(), getInterestedAttendees());
    }
}

