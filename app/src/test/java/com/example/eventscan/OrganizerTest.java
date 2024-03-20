package com.example.eventscan;

import static org.junit.Assert.assertEquals;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Organizer;

import org.junit.Test;

public class OrganizerTest {
    @Test
    public void organizer_equals(){
        Organizer organizer1 = new Organizer();
        organizer1.setDeviceID("abc123");
        organizer1.setPhoneNum("123456");
        organizer1.setEmail("abc@abc.com");
        organizer1.setName("Test Test");
        organizer1.setBio("Test Bio");
        organizer1.setProfilePictureID("123");
        Organizer organizer2 = new Organizer();
        organizer2.setDeviceID("abc123");
        organizer2.setPhoneNum("123456");
        organizer2.setEmail("abc@abc.com");
        organizer2.setName("Test Test");
        organizer2.setBio("Test Bio");
        organizer2.setProfilePictureID("123");
        assertEquals(organizer1, organizer2);
        assertEquals(organizer2, organizer1);
    }
}
