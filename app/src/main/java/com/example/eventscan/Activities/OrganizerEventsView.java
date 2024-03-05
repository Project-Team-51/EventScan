package com.example.eventscan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.R;

public class OrganizerEventsView extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_events_view);
        final Button buttonAddEvent = findViewById(R.id.buttonAddEvent);
        final Button buttonOrganizerProfile = findViewById(R.id.buttonOrganizerProfile);
        final Button buttonSendNoti = findViewById(R.id.buttonSendEventNoti);

        buttonOrganizerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventsView.this, OrganizerProfile.class);
                startActivity(intent);
            }
        });

        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventsView.this, AddEvent.class);
                startActivity(intent);
            }
        });

    }
}
