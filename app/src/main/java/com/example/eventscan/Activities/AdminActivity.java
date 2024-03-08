package com.example.eventscan.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventscan.Database.DatabaseHelper;
import com.example.eventscan.Fragments.AddEvent;
import com.example.eventscan.Fragments.AttendeeFragment;
import com.example.eventscan.Fragments.EventFragment;
import com.example.eventscan.Fragments.qrCodeTestFrag;
import com.example.eventscan.R;

/*
 * The main activity that an Admin sees. Calls fragments instead of other activities. The navigation
 * bar at the bottom allows access to the EventFragment or the AttendeeFragment. By default, it shows
 * the EventFragment. This activity will become the overall MainActivity in the near future, and most
 * activities will be refactored into fragments that are called from this activity depending on buttons pressed
 * and user permissions.
 */

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);

        if (savedInstanceState == null) {
            // Load the default fragment (EventFragment)
            EventFragment eventFragment = new EventFragment();
            loadFragment(eventFragment);
        }

        // Set click listeners for navigation buttons
        ImageButton buttonEvents = findViewById(R.id.buttonEvents);
        ImageButton buttonProfile = findViewById(R.id.buttonProfile);

        buttonEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the EventFragment
                EventFragment eventFragment = new EventFragment();
                loadFragment(eventFragment);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the AttendeeFragment
                AttendeeFragment attendeeFragment = new AttendeeFragment();
                loadFragment(attendeeFragment);
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
