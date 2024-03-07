package com.example.eventscan.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.Fragments.ProfileFragment;
import com.example.eventscan.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AttendeeEventsView extends AppCompatActivity implements View.OnClickListener {

    Button buttonAttendeeProfile;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_events_view);

        buttonAttendeeProfile = findViewById(R.id.buttonAttendeeProfile);
        buttonAttendeeProfile.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.buttonAttendeeProfile){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileFragment()).commit();
        }
    }


}
