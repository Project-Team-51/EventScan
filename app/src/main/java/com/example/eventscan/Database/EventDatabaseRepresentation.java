package com.example.eventscan.Database;

import android.location.Location;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The representation of an Event inside the database
 * Currently stripping out the list of attendees as well as the organizer objects
 * and replace them with just their UIDs
 */
public class EventDatabaseRepresentation {
    private Location location;
    private String desc;
    private String name;
    private HashMap<String, Integer> checkedInAttendeeIDs;
    private ArrayList<String> interestedAttendeeIDs;
    private String organizerID;
    private String posterID; // TODO make this a URI instead
    private String eventID;

    public EventDatabaseRepresentation(){}
    public EventDatabaseRepresentation(Event event){
        location = event.getLocation();
        desc = event.getDesc();
        interestedAttendeeIDs = new ArrayList<>();
        name = event.getName();
        for(Map.Entry<Attendee, Integer> entry : event.getCheckedInAttendees().entrySet()){
            checkedInAttendeeIDs.put(
                    entry.getKey().getDeviceID(),
                    entry.getValue()
            );
        }
        for(Attendee attendee : event.getInterestedAttendees()){
            interestedAttendeeIDs.add(attendee.getDeviceID());
        }
        organizerID = event.getOrganizer().getDeviceID();
        posterID = event.getPoster();
        eventID = event.getEventID();

    }

    /**
     * converts self to a "barebones" event (the event contains all non-referenced data)
     * @return an Event object containing only the fields that do not require additional Database fetches
     */
    Event convertToBarebonesEvent(){
        Event event = new Event();
        event.setLocation(this.location);
        event.setDesc(this.getDesc());
        event.setName(this.name);
        event.setEventID(this.eventID);

        return event;
    }
    public HashMap<String, Integer> getCheckedInAttendeeIDs(){
        return checkedInAttendeeIDs;
    }
    public ArrayList<String> getInterestedAttendeeIDs() {
        return interestedAttendeeIDs;
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
