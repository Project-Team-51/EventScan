package com.example.eventscan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.R;
import com.google.firebase.FirebaseApp;

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
                Intent intent = new Intent(UserSelection.this, LoginActivity.class);
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
=======


    }

}

