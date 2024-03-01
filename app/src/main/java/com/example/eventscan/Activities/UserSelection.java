package com.example.eventscan.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.Entities.Administrator;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Organizer;
import com.example.eventscan.Entities.User;
import com.example.eventscan.R;

public class UserSelection extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userselection);

        // Handle user type selection
        Button userType1Button = findViewById(R.id.buttonOrganizer);
        Button userType2Button = findViewById(R.id.buttonAttendee);
        Button userType3Button = findViewById(R.id.buttonAdministrator);

        //userType1Button.setOnClickListener(view -> startApp(Organizer.class));
        //userType2Button.setOnClickListener(view -> startApp(Attendee.class));
        //userType3Button.setOnClickListener(view -> startApp(Administrator.class));
    }

}

