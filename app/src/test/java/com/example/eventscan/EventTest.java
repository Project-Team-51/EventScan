package com.example.eventscan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Organizer;
import com.example.eventscan.Entities.Event;

import org.junit.Test;

public class EventTest {

    @Test
    public void testCheckInAttendee() {
        Event event = createSampleEvent();
        Attendee attendee = createSampleAttendee();
        event.checkInAttendee(attendee);

        assertTrue(event.getCheckedInAttendees().containsKey(attendee));
        assertEquals(Integer.valueOf(1), event.getCheckedInAttendees().get(attendee));
    }
    @Test
    public void testSetAttendeeCheckInCount() {
        Event event = createSampleEvent();
        Attendee attendee = createSampleAttendee();
        event.setAttendeeCheckInCount(attendee, 3);
        assertEquals(Integer.valueOf(3), event.getCheckedInAttendees().get(attendee));
    }

    @Test
    public void testRemoveCheckedInAttendee() {
        Event event = createSampleEvent();
        Attendee attendee = createSampleAttendee();
        event.checkInAttendee(attendee);
        event.removeCheckedInAttendee(attendee);
        assertFalse(event.getCheckedInAttendees().containsKey(attendee));
    }

    @Test
    public void testAddInterestedAttendee() {
        Event event = createSampleEvent();
        Attendee attendee = createSampleAttendee();
        event.addInterestedAttendee(attendee);
        assertTrue(event.getInterestedAttendees().contains(attendee));
    }

    @Test
    public void testRemoveInterestedAttendee() {
        Event event = createSampleEvent();
        Attendee attendee = createSampleAttendee();
        event.addInterestedAttendee(attendee);
        event.removeInterestedAttendee(attendee);
        assertFalse(event.getInterestedAttendees().contains(attendee));
    }

    // Helper methods
    private Event createSampleEvent() {
        return new Event("Sample Event", "Description", new Organizer(), "poster_url", "event_id");
    }

    private Attendee createSampleAttendee() {
        return new Attendee();
    }
}
