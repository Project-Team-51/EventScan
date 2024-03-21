package com.example.eventscan;

import com.example.eventscan.Entities.Attendee;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttendeeTest {
    @Test
    public void attendee_equals(){
        Attendee attendee1 = new Attendee();
        attendee1.setDeviceID("abc123");
        attendee1.setPhoneNum("123456");
        attendee1.setEmail("abc@abc.com");
        attendee1.setName("Test Test");
        attendee1.setBio("Test Bio");
        attendee1.setProfilePictureID("123");
        Attendee attendee2 = new Attendee();
        attendee2.setDeviceID("abc123");
        attendee2.setPhoneNum("123456");
        attendee2.setEmail("abc@abc.com");
        attendee2.setName("Test Test");
        attendee2.setBio("Test Bio");
        attendee2.setProfilePictureID("123");
        assertEquals(attendee1, attendee2);
        assertEquals(attendee2, attendee1);
    }
    @Test
    public void different_attendee_are_unequal(){
        Attendee attendee1 = new Attendee();
        attendee1.setDeviceID("devID");
        attendee1.setPhoneNum("123456");
        attendee1.setEmail("abc@abc.com");
        attendee1.setName("Test Test");
        attendee1.setBio("Test Bio");
        attendee1.setProfilePictureID("123");
        Attendee attendee2 = new Attendee();
        attendee2.setDeviceID("devID");
        attendee2.setPhoneNum("123456");
        attendee2.setEmail("abc@abc.com");
        attendee2.setName("Test Test");
        attendee2.setBio("Test Bio");
        attendee2.setProfilePictureID("123");
        // they are now equal, do different changes and make sure they're !=
        // 1
        attendee1.setDeviceID("ABC");
        assertNotEquals(attendee1, attendee2);
        assertNotEquals(attendee2, attendee1);
        attendee1.setDeviceID("devID");
        // 2
        attendee1.setPhoneNum("654321");
        assertNotEquals(attendee1, attendee2);
        assertNotEquals(attendee2, attendee1);
        attendee1.setPhoneNum("123456");
        // 3
        attendee1.setEmail("different@email.com");
        assertNotEquals(attendee1, attendee2);
        assertNotEquals(attendee2, attendee1);
        attendee1.setEmail("abc@abc.com");
        // 4
        attendee1.setName("different name");
        assertNotEquals(attendee1, attendee2);
        assertNotEquals(attendee2, attendee1);
        attendee1.setName("Test Test");
        // 5
        attendee1.setBio("Different Bio");
        assertNotEquals(attendee1, attendee2);
        assertNotEquals(attendee2, attendee1);
    }
}
