package com.example.eventscan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Organizer;

import org.junit.Test;

/**
 * Testing class for Organizer usertype
 */
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
    @Test
    public void different_organizers_are_unequal(){
        Organizer organizer1 = new Organizer();
        organizer1.setDeviceID("devID");
        organizer1.setPhoneNum("123456");
        organizer1.setEmail("abc@abc.com");
        organizer1.setName("Test Test");
        organizer1.setBio("Test Bio");
        organizer1.setProfilePictureID("123");
        Organizer organizer2 = new Organizer();
        organizer2.setDeviceID("devID");
        organizer2.setPhoneNum("123456");
        organizer2.setEmail("abc@abc.com");
        organizer2.setName("Test Test");
        organizer2.setBio("Test Bio");
        organizer2.setProfilePictureID("123");
        // they are now equal, do different changes and make sure they're !=
        // 1
        organizer1.setDeviceID("ABC");
        assertNotEquals(organizer1, organizer2);
        assertNotEquals(organizer2, organizer1);
        organizer1.setDeviceID("devID");
        // 2
        organizer1.setPhoneNum("654321");
        assertNotEquals(organizer1, organizer2);
        assertNotEquals(organizer2, organizer1);
        organizer1.setPhoneNum("123456");
        // 3
        organizer1.setEmail("different@email.com");
        assertNotEquals(organizer1, organizer2);
        assertNotEquals(organizer2, organizer1);
        organizer1.setEmail("abc@abc.com");
        // 4
        organizer1.setName("different name");
        assertNotEquals(organizer1, organizer2);
        assertNotEquals(organizer2, organizer1);
        organizer1.setName("Test Test");
        // 5
        organizer1.setBio("Different Bio");
        assertNotEquals(organizer1, organizer2);
        assertNotEquals(organizer2, organizer1);
    }
}
