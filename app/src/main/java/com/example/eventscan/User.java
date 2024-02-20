package com.example.eventscan;

public class User {
    private String name;
    private String phoneNum;
    private String password;
    private String email;
    private String bio;
    private Boolean checkedIn;
    private Location location;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

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
}
