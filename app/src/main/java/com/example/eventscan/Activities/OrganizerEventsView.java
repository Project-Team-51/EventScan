package com.example.eventscan.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventscan.Fragments.AddEvent;
import com.example.eventscan.Fragments.ProfileFragment;
import com.example.eventscan.R;

public class OrganizerEventsView extends AppCompatActivity implements View.OnClickListener {

    Button buttonAddEvent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_events_view);

        final Button buttonOrganizerProfile = findViewById(R.id.buttonOrganizerProfile);
        final Button buttonSendNoti = findViewById(R.id.buttonSendEventNoti);
        final Button buttonViewEvents = findViewById(R.id.buttonViewEvents);
        Button buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(this);

        buttonViewEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the content view to organizer_view_all.xml
                Intent intent = new Intent(OrganizerEventsView.this, OrganizerViewAllEvents.class);
                startActivity(intent);
            }
        });
        buttonOrganizerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventsView.this, OrganizerProfile.class);
                startActivity(intent);
            }
        });

        buttonAddEvent.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.buttonAddEvent){
            String deviceID = getDeviceId(OrganizerEventsView.this);
            AddEvent eventFragment = new AddEvent();

            // Pass the device ID to the fragment using a Bundle
            Bundle bundle = new Bundle();
            bundle.putString("DEVICE_ID", deviceID);
            eventFragment.setArguments(bundle);

            // Begin a fragment transaction
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, eventFragment)
                    .commit();

        }

    }

    public static String getDeviceId(Context context) {
        // Retrieve the device ID using Settings.Secure class
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}