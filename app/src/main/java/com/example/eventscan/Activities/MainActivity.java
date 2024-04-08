package com.example.eventscan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventscan.Fragments.AddEvent;
import com.example.eventscan.Fragments.AllPicFrag;
import com.example.eventscan.Fragments.AttendeeFragment;
import com.example.eventscan.Fragments.EventFragment;
import com.example.eventscan.Fragments.ProfileFragment;
import com.example.eventscan.Fragments.QrScannerFragment;
import com.example.eventscan.R;

import java.util.Observable;
import java.util.Observer;



/**
 * The main activity that users see. Calls fragments instead of other activities. The navigation
 * bar at the bottom allows access to the EventFragment or the AttendeeFragment. By default, it shows
 * the EventFragment. This activity will become the overall MainActivity in the near future, and most
 * activities will be refactored into fragments that are called from this activity depending on buttons pressed
 * and user permissions.
 */


public class    MainActivity extends AppCompatActivity implements AddEvent.OnEventAddedListener{
    private ImageButton buttonEvents;
    private ImageButton buttonProfile;
    private ImageButton buttonQR;
    private ImageButton buttonAdd;
    private ImageButton buttonNotify;
    private ImageButton buttonAllPic;
    private ImageButton buttonAllProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Intent intent = getIntent();
        String userType = intent.getStringExtra("userType");
        if (savedInstanceState == null) {
            // Load the default fragment (EventFragment)
            EventFragment eventFragment = new EventFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userType", userType);
            eventFragment.setArguments(bundle);
            loadFragment(eventFragment);
        }

        // Set click listeners for navigation buttons
        buttonEvents = findViewById(R.id.buttonEvents);
        buttonProfile = findViewById(R.id.buttonProfile);
        buttonQR = findViewById(R.id.buttonQR);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonNotify = findViewById(R.id.buttonNoti);
        buttonAllPic = findViewById(R.id.buttonAllPic);
        buttonAllProfile = findViewById(R.id.buttonAllP);

        hideAllButtons();
        // Show buttons based on user type
        if (userType != null) {
            switch (userType) {
                case "Organizer":
                    showOrganizerButtons();
                    break;
                case "Admin":
                    showAdminButtons();
                    break;
                case "Attendee":
                    showAttendeeButtons();
                    break;
            }
        }
        // Load EventFragment when the Events button is clicked
        buttonEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the EventFragment
                EventFragment eventFragment = new EventFragment();
                loadFragment(eventFragment);
            }
        });

        // Load AttendeeFragment when the Profile button is clicked
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the AttendeeFragment
                ProfileFragment attendeeFragment = new ProfileFragment();
                loadFragment(attendeeFragment);
            }
        });
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the AttendeeFragment
                AddEvent addevent = new AddEvent();
                loadFragment(addevent);

            }
        });
        buttonQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the AttendeeFragment
                QrScannerFragment qrscan = new QrScannerFragment();
                loadFragment(qrscan);
            }
        });
        buttonAllProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the EventFragment
                AttendeeFragment attendeeFragment = new AttendeeFragment();
                loadFragment(attendeeFragment);
            }
        });
        buttonAllPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the EventFragment
                AllPicFrag picFrag = new AllPicFrag();
                loadFragment(picFrag);
            }
        });
    }



    /**
     * Load the specified fragment into the fragment container view.
     * @param fragment The fragment to be loaded.
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private void hideAllButtons() {
        buttonEvents.setVisibility(View.GONE);
        buttonProfile.setVisibility(View.GONE);
        buttonQR.setVisibility(View.GONE);
        buttonAdd.setVisibility(View.GONE);
        buttonNotify.setVisibility(View.GONE);
        buttonAllPic.setVisibility(View.GONE);
        buttonAllProfile.setVisibility(View.GONE);
    }

    private void showOrganizerButtons() {
        buttonEvents.setVisibility(View.VISIBLE);
        buttonProfile.setVisibility(View.VISIBLE);
        buttonQR.setVisibility(View.VISIBLE);
        buttonAdd.setVisibility(View.VISIBLE);
        buttonNotify.setVisibility(View.VISIBLE);
    }

    private void showAdminButtons() {
        buttonEvents.setVisibility(View.VISIBLE);
        buttonProfile.setVisibility(View.VISIBLE);
        buttonQR.setVisibility(View.VISIBLE);
        buttonAllPic.setVisibility(View.VISIBLE);
        buttonAllProfile.setVisibility(View.VISIBLE);
    }

    private void showAttendeeButtons() {
        buttonEvents.setVisibility(View.VISIBLE);
        buttonProfile.setVisibility(View.VISIBLE);
        buttonQR.setVisibility(View.VISIBLE);
    }
    public void onEventAdded() {
        // Replace the current fragment with EventFragment
        EventFragment eventFragment = new EventFragment();
        loadFragment(eventFragment);
    }
}
