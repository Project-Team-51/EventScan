package com.example.eventscan.Entities;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
/*
 * The user superclass that the three other user types descend from. Stores info all users may need, as well as
 * providing setters and getters for interfacing with the class.
 */
public class User implements Serializable {
    private String name;
    private String phoneNum;
    private String password;
    private String email;
    private String bio;
    private Boolean checkedIn;
    private Location location;
    private String deviceID;
    private String profilePictureID;
    public ArrayList<Event> inEvents;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }


    // empty constructor so it works with firestore
    public User() {}

    /**
     * Constructs a new User object with the specified attributes.
     *
     * @param name             The name of the user.
     * @param deviceID         The device ID of the user.
     * @param email            The email of the user.
     * @param phoneNum         The phone number of the user.
     * @param bio              The biography of the user.
     * @param profilePictureID The profile picture ID of the user.
     */
    public User(String name, String deviceID, String email, String phoneNum, String bio, String profilePictureID) {
            this.name = name;
            this.deviceID = deviceID;
            this.email = email;
            this.phoneNum = phoneNum;
            this.bio = bio;
            this.profilePictureID = profilePictureID;
        }

        // getters and setters for all parameters of User
        public String getName() {
            return name;
        }

        public String getPhoneNum() {
            return phoneNum;
        }

        public String getEmail() {
            return email;
        }

        public String getBio() {
            return bio;
        }

        public Boolean isCheckedIn() {
            return checkedIn;
        }

        public Location getLocation() {
            return location;
        }

        public String getDeviceID() {
            return deviceID;
        }

        public String getProfilePictureID(){ return profilePictureID;}

        public void setName(String name) {
            this.name = name;
        }

        public void setPhoneNum(String phoneNum) {
            this.phoneNum = phoneNum;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public void setCheckedIn(Boolean checkedIn) {
            this.checkedIn = checkedIn;
        }

        public void setPassword(String password) {
            this.password = password;
        }
        public void setLocation(Location location) {
            this.location = location;
        }

        public void setDeviceID(String deviceID){this.deviceID = deviceID;}
        public void setProfilePictureID(String profilePictureID){this.profilePictureID = profilePictureID;}
        public ArrayList<Event> getInEvents(){ return this.inEvents; }
}