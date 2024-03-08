package com.example.eventscan.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventscan.Fragments.AddEvent;
import com.example.eventscan.Fragments.ProfileFragment;
import com.example.eventscan.R;

public class OrganizerEventsView extends AppCompatActivity implements View.OnClickListener {

    Button buttonOrganizerProfile;
    Button buttonSendNoti;
    Button buttonViewEvents;
    Button buttonAddEvent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_events_view);

        buttonOrganizerProfile = findViewById(R.id.buttonOrganizerProfile);
        buttonSendNoti = findViewById(R.id.buttonSendEventNoti);
        buttonViewEvents = findViewById(R.id.buttonViewEvents);
        buttonAddEvent = findViewById(R.id.buttonAddEvent);

        buttonOrganizerProfile.setOnClickListener(this);
        buttonSendNoti.setOnClickListener(this);
        buttonViewEvents.setOnClickListener(this);
        buttonAddEvent.setOnClickListener(this);

        buttonViewEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the content view to organizer_view_all.xml
                Intent intent = new Intent(OrganizerEventsView.this, OrganizerViewAllEvents.class);
                startActivity(intent);
            }
        });


        buttonAddEvent.setOnClickListener(this);
        buttonOrganizerProfile.setOnClickListener(this);

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

        } else if(v.getId()==R.id.buttonOrganizerProfile){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();

            buttonOrganizerProfile.setVisibility(View.GONE);
            buttonSendNoti.setVisibility(View.GONE);
            buttonViewEvents.setVisibility(View.GONE);
            buttonAddEvent.setVisibility(View.GONE);

            // Add a FragmentTransaction listener to handle button visibility
            getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    // Check if the back stack is empty
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        // Fragment is removed from the back stack, make buttons visible
                        buttonOrganizerProfile.setVisibility(View.VISIBLE);
                        buttonSendNoti.setVisibility(View.VISIBLE);
                        buttonViewEvents.setVisibility(View.VISIBLE);
                        buttonAddEvent.setVisibility(View.VISIBLE);

                        // Remove the listener to avoid multiple callbacks
                        getSupportFragmentManager().removeOnBackStackChangedListener(this);
                    }
                }
            });

        }

    }

    public static String getDeviceId(Context context) {
        // Retrieve the device ID using Settings.Secure class
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}