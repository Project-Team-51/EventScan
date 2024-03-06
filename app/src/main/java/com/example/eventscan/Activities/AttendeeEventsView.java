package com.example.eventscan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.R;

public class AttendeeEventsView extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_events_view);
        final Button buttonAttendeeProfile = findViewById(R.id.buttonAttendeeProfile);

        buttonAttendeeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeEventsView.this, AttendeeProfile.class);
                startActivity(intent);
            }
        });




    }
}
