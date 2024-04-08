package com.example.eventscan.Activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.DeviceID;
import com.example.eventscan.Fragments.AddEvent;
import com.example.eventscan.Fragments.AllPicFrag;
import com.example.eventscan.Fragments.AttendeeFragment;
import com.example.eventscan.Fragments.EventFragment;
import com.example.eventscan.Fragments.ProfileFragment;
import com.example.eventscan.Fragments.QrScannerFragment;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;



/*
 * The main activity that an Admin sees. Calls fragments instead of other activities. The navigation
 * bar at the bottom allows access to the EventFragment or the AttendeeFragment. By default, it shows
 * the EventFragment. This activity will become the overall MainActivity in the near future, and most
 * activities will be refactored into fragments that are called from this activity depending on buttons pressed
 * and user permissions.
 */


public class MainActivity extends AppCompatActivity implements AddEvent.OnEventAddedListener{
    private ImageButton buttonEvents;
    private ImageButton buttonProfile;
    private ImageButton buttonQR;
    private ImageButton buttonAdd;
    private ImageButton buttonNotify;
    private ImageButton buttonAllPic;
    private ImageButton buttonAllProfile;
    private CollectionReference eventsCollection;
    private Database db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

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
                eventFragment.toggleEventsMode(true);
                loadFragment(eventFragment);
            }
        });

        buttonNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the EventFragment
                EventFragment eventFragment = new EventFragment();
                eventFragment.toggleNotifyMode(true);
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

        db = Database.getInstance();

        eventsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) { // if there is an update then..
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Event event = (Event) querySnapshots.toObjects(Event.class);
                        String deviceID = DeviceID.getDeviceID(getApplicationContext());
                        if (event.getCheckedInAttendeesList().size() == event.getAttendeeLimit() && event.getOrganizer().getDeviceID() == deviceID) {
                            makeNotification(event);
                        }
                    }
                }
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

    public void makeNotification(Event event){
        String notificationID = "NOTIFICATION_ID";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), notificationID);
        builder.setSmallIcon(R.drawable.notification)
                .setContentTitle(event.getName()) // event name should go here
                .setContentText("This event has reached full capacity!")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);



        EventFragment eventFragment = new EventFragment();
        loadFragment(eventFragment);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(notificationID);
            if (notificationChannel == null){
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(notificationID, "description...", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(0,builder.build());

    }






}