package com.example.eventscan.Database;

import com.example.eventscan.Entities.Event;

import javax.annotation.Nullable;

/**
 * Object that is stored in the database for linking a QR code to an event
 */
public class QRDatabaseEventLink {

    /**QRs using this will direct the user to the "check into event" page*/
    public static final int DIRECT_CHECK_IN = 0;


    /**QRs using this will direct the user to the "see event details (and possibly add to the 'want to go' list)" page*/
    public static final int DIRECT_SEE_DETAILS = 1;
    private int directionType; //0 for sign in, 1 for seeing details
    @Nullable
    private String directedEventID; // the ID of the event to direct the scanner to

    /**
     * Create a QRDatabaseRedirection object
     * @param directionType the type of direction to use, when implementing, use QRDatabaseEventLink.DIRECT_SIGN_IN or DIRECT_SEE_DETAILS
     * @param directedEvent the event that this database entry will point to
     */
    public QRDatabaseEventLink(int directionType, Event directedEvent){
        this.directionType = directionType;
        directedEventID = (directedEvent == null) ? null : directedEvent.getEventID();
    }

    public int getDirectionType(){
        return directionType;
    }

    /**
     * @return event that this QR code directs to, if null, this means that the QR was reserved but not written to
     */
    @Nullable
    public String getDirectedEventID(){
        return directedEventID;
    }
}
