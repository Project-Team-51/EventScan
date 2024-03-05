package com.example.eventscan.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.Entities.Administrator;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Organizer;
import com.example.eventscan.Entities.User;
import com.example.eventscan.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class UserSelection extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.userselection);

        // Handle user type selection
        final Button organizerButton = findViewById(R.id.buttonOrganizer);
        Button userType2Button = findViewById(R.id.buttonAttendee);
        final Button adminButton = findViewById(R.id.buttonAdministrator);

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSelection.this, Login.class);
                startActivity(intent);
            }
        });
        organizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSelection.this, OrganizerEventsView.class);
                startActivity(intent);
            }
        });
        //userType1Button.setOnClickListener(view -> startApp(Organizer.class));
        //userType2Button.setOnClickListener(view -> startApp(Attendee.class));
        //userType3Button.setOnClickListener(view -> startApp(Administrator.class));
    }

}

