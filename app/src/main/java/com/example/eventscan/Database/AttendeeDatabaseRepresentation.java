package com.example.eventscan.Database;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Organizer;

import java.io.Serializable;

public class AttendeeDatabaseRepresentation implements Serializable {
    String attendeeType;
    Attendee attendee;

    public AttendeeDatabaseRepresentation(){}
    public AttendeeDatabaseRepresentation(Attendee attendee){
        setAttendee(attendee);
    }

    /**
     * Factory-like method
     * @return either an Attendee object or a subclass based on the information stored in self
     */
    public Attendee toAttendee(){
        if(attendeeType.equals("Organizer")){
            assert(attendee instanceof Organizer);
            return (Organizer) attendee;
        }
        return attendee;
    }

    public String getAttendeeType() {
        return attendeeType;
    }

    public Attendee getAttendee() {
        return attendee;
    }

    public void setAttendee(Attendee attendee) {
        if(attendee instanceof Organizer){
            attendeeType = "Organizer";
        } else {
            attendeeType = "Attendee";
        }
        this.attendee = attendee;
    }
}
