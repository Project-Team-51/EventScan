package com.example.eventscan.DeprecatedActivities;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Helpers.EventArrayAdapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;


import com.example.eventscan.Entities.Event;
import com.example.eventscan.Fragments.ProfileFragment;
import com.example.eventscan.Fragments.QrScannerFragment;
import com.example.eventscan.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/*
 * OFFICIALLY DEPRECATED. USE ONLY FOR REFERENCE WHEN BUILDING NEW FRAGMENTS.
 */

public class AttendeeEventsView extends AppCompatActivity implements View.OnClickListener {

    // UI Components
    private Button buttonAttendeeProfile;
    private Button buttonQRScanner;
    private Button buttonEventsView;
    private ListView eventsListView;
    private ListView announcementsListView;

    // Data
    private ArrayList<Event> upcomingEvents;
    private ArrayList<Event> eventAnnouncements;
    private EventArrayAdapter upcomingEventsAdapter;
    private EventArrayAdapter announcementsAdapter;

    // Firebase
    private Database db;

    /**
     * Called when the activity is created. Initializes UI components, adapters,
     * and sets up Firebase Firestore listeners for events and announcements.
     *
     * @param savedInstanceState A Bundle containing the saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_events_view);

        buttonAttendeeProfile = findViewById(R.id.buttonAttendeeProfile);
        buttonEventsView = findViewById(R.id.buttonViewEvents);

        buttonAttendeeProfile.setOnClickListener(this);
        buttonQRScanner = findViewById(R.id.buttonQRScanner);
        buttonQRScanner.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new QrScannerFragment()).commit();
        });
        buttonEventsView.setOnClickListener(this);

        upcomingEvents = new ArrayList<>();
        eventAnnouncements = new ArrayList<>();

        upcomingEventsAdapter = new EventArrayAdapter(this, R.layout.event_list_content, upcomingEvents);
        announcementsAdapter = new EventArrayAdapter(this, R.layout.event_list_content, eventAnnouncements);

        eventsListView = findViewById(R.id.upcomingEvents);
        announcementsListView = findViewById(R.id.announcements);

        eventsListView.setAdapter(upcomingEventsAdapter);
        announcementsListView.setAdapter(announcementsAdapter);

        // Firebase
        db = Database.getInstance();

        // Set up Firestore snapshot listener
        db.getEventsCollection().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) { // if there is an update then..
                    upcomingEvents.clear();
                    eventAnnouncements.clear();
                    ArrayList<Task<Event>> updateTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                        updateTasks.add(
                                db.events.get(doc.get("eventID", String.class)).addOnSuccessListener(event -> {
                                    upcomingEventsAdapter.add(event);
                                    announcementsAdapter.add(event); // CHANGE FOR ANNOUNCEMENTS
                                })
                        );
                    }
                    Tasks.whenAllComplete(updateTasks).addOnCompleteListener(task -> {
                        upcomingEventsAdapter.notifyDataSetChanged(); // update listviews
                        announcementsAdapter.notifyDataSetChanged();
                    });
                }
            }
        });

        // Set item click listener for events list view
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = upcomingEvents.get(position);
            }
        });
    }

    /**
     * Handles button clicks. Opens the corresponding fragment or initiates
     * the attendee profile view when the Attendee Profile button is clicked.
     *
     * @param v The clicked View.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonAttendeeProfile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();

            buttonEventsView.setVisibility(View.GONE);
            buttonAttendeeProfile.setVisibility(View.GONE);
            buttonQRScanner.setVisibility(View.GONE);

            getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    // Checks if the back stack is empty
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        // Fragment is removed from the back stack, makes buttons visible
                        buttonEventsView.setVisibility(View.VISIBLE);
                        buttonAttendeeProfile.setVisibility(View.VISIBLE);
                        buttonQRScanner.setVisibility(View.VISIBLE);

                        // Removes the listener to avoid multiple callbacks
                        getSupportFragmentManager().removeOnBackStackChangedListener(this);
                    }
                }
            });

        }
    }
}
