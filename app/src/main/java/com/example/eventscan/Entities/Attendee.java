package com.example.eventscan.Entities;

import java.util.ArrayList;

public class Attendee extends User {

    private String name;
    private String phoneNum;
    private String email;
    private String bio;
    private String deviceID;
    private String profilePictureID;

    public Attendee() {
    }

    public Attendee(String name, String phoneNum, String email, String bio, String deviceID, String profilePictureID) {
        super(name, phoneNum, email, bio, deviceID, profilePictureID);
        this.name = name;
        this.phoneNum = phoneNum;
        this.email = email;
        this.bio = bio;
        this.deviceID = deviceID;
        this.profilePictureID = profilePictureID;



    }

    ArrayList<Attendee> attendeeDataList;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getProfilePictureID() {
        return profilePictureID;
    }

    public void setProfilePictureID(String profilePictureID) {
        this.profilePictureID = profilePictureID;
    }


}
