package com.example.eventscan.Database;

/**
 * A test database, will only write to the test collection
 */
public class TestDatabase extends Database {
    private static final String attendeeCollectionPath = "test/attendees"; // easier to change here if we refactor the DB later
    private static final String eventsCollectionPath = "test/events";

}
