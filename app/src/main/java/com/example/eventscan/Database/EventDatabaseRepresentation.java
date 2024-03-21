package com.example.eventscan.Database;

import android.location.Location;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;

import java.util.ArrayList;

/**
 * The representation of an Event inside the database
 * Currently stripping out the list of attendees as well as the organizer objects
 * and replace them with just their UIDs
 */
public class EventDatabaseRepresentation {
    private Location location;
    private String desc;
    private String name;
    private ArrayList<String> attendeeIDs;
    private String organizerID;
    private String posterID; // TODO make this a URI instead
    private String eventID;

    public EventDatabaseRepresentation(){}
    public EventDatabaseRepresentation(Event event){
        location = event.getLocation();
        desc = event.getDesc();
        attendeeIDs = new ArrayList<>();
        name = event.getName();
        for(Attendee attendee: event.getAttendees()){
            attendeeIDs.add(attendee.getDeviceID());
        }
        organizerID = event.getOrganizer().getDeviceID();
        posterID = event.getPoster();
        eventID = event.getEventID();

    }

    /**
     * converts self to a barebones event (the event contains all non-referenced data)
     * @return
     */
    Event convertToBarebonesEvent(){
        Event event = new Event();
        event.setLocation(this.location);
        event.setDesc(this.getDesc());
        event.setName(this.name);
        event.setEventID(this.eventID);

        return event;
    }
    public ArrayList<String> getAttendeeIDs() {
        return attendeeIDs;
    }
    public String getName(){
        return name;
    }
    public String getDesc(){
        return desc;
    }
    public String getEventID(){
        return eventID;
    }

    public String getOrganizerID(){
        return organizerID;
    }
}
