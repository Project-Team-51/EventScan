package com.example.eventscan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.R;
import com.google.firebase.FirebaseApp;
/*
 * The activity that serves as the first activity the user sees. Gives the user 3 buttons, one for each user type,
 * and choosing one will take you to that user types main activity. In the future, we will consolidate all 3 main
 * activities into one main activity.
 */
public class UserSelection extends AppCompatActivity {

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState A Bundle containing the saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.userselection);

        // Handle user type selection
        final Button organizerButton = findViewById(R.id.buttonOrganizer);
        final Button attendeeButton = findViewById(R.id.buttonAttendee);
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

        attendeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSelection.this, AttendeeEventsView.class);
                startActivity(intent);
            }
        });

    }

}

