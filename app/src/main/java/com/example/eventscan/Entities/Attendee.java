package com.example.eventscan.Entities;

import java.util.ArrayList;

/*
 * The Attendee class represents a user attending an event and extends the User class.
 * It contains information about the attendee's name, phone number, email, biography,
 * device ID, and profile picture ID. It provides methods to retrieve and modify this information.
 */
public class Attendee extends User {

    private String name;
    private String phoneNum;
    private String email;
    private String bio;
    private String deviceID;
    private String profilePictureID;

    /**
     * Default constructor for the Attendee class.
     */
    public Attendee() {
    }

    /**
     * Parameterized constructor for the Attendee class.
     *
     * @param name             The name of the attendee.
     * @param phoneNum         The phone number of the attendee.
     * @param email            The email address of the attendee.
     * @param bio              The biography of the attendee.
     * @param deviceID         The unique device ID of the attendee.
     * @param profilePictureID The unique ID of the attendee's profile picture.
     */
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

    /**
     * Retrieves the name of the attendee.
     *
     * @return The name of the attendee.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the attendee.
     *
     * @param name The new name of the attendee.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the phone number of the attendee.
     *
     * @return The phone number of the attendee.
     */
    public String getPhoneNum() {
        return phoneNum;
    }

    /**
     * Sets the phone number of the attendee.
     *
     * @param phoneNum The new phone number of the attendee.
     */
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    /**
     * Retrieves the email address of the attendee.
     *
     * @return The email address of the attendee.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the attendee.
     *
     * @param email The new email address of the attendee.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retrieves the biography of the attendee.
     *
     * @return The biography of the attendee.
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the biography of the attendee.
     *
     * @param bio The new biography of the attendee.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Retrieves the unique device ID of the attendee.
     *
     * @return The unique device ID of the attendee.
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Sets the unique device ID of the attendee.
     *
     * @param deviceID The new unique device ID of the attendee.
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * Retrieves the unique ID of the attendee's profile picture.
     *
     * @return The unique ID of the attendee's profile picture.
     */
    public String getProfilePictureID() {
        return profilePictureID;
    }

    /**
     * Sets the unique ID of the attendee's profile picture.
     *
     * @param profilePictureID The new unique ID of the attendee's profile picture.
     */
    public void setProfilePictureID(String profilePictureID) {
        this.profilePictureID = profilePictureID;
    }


}
