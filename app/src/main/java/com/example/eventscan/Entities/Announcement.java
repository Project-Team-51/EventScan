package com.example.eventscan.Entities;

import java.io.Serializable;

public class Announcement implements Serializable {
    String message;
    public Announcement(){}

    public Announcement(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
