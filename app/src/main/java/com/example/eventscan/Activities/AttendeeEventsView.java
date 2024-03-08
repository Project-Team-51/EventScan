package com.example.eventscan.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
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
    Button buttonEventsView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_events_view);

        buttonAttendeeProfile = findViewById(R.id.buttonAttendeeProfile);
        buttonEventsView = findViewById(R.id.buttonViewEvents);

        buttonAttendeeProfile.setOnClickListener(this);
        buttonEventsView.setOnClickListener(this);



    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.buttonAttendeeProfile){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();

            buttonEventsView.setVisibility(View.GONE);
            buttonAttendeeProfile.setVisibility(View.GONE);
        }
    }




}
