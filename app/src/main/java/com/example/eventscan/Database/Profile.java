package com.example.eventscan.Database;

public class Profile {
    private int profilePictureId;
    private String name;
    private String email;
    private String phoneNumber;
    private String description;

    public Profile(int profilePictureResId, String name, String email, String phoneNumber, String description) {
        this.profilePictureId = profilePictureResId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.description = description;
    }

    public int getProfilePictureId() {
        return profilePictureId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDescription() {
        return description;
    }
}
