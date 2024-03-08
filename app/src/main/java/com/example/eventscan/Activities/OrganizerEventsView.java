package com.example.eventscan.Activities;

import com.example.eventscan.Fragments.AddEvent;
import com.example.eventscan.Activities.OrganizerViewAllEvents;
import com.example.eventscan.Helpers.EventArrayAdapter;

import android.content.Context;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventscan.Entities.Event;

import com.example.eventscan.Fragments.ProfileFragment;
import com.example.eventscan.Fragments.QrScannerFragment;

import com.example.eventscan.Fragments.AddEvent;
import com.example.eventscan.Fragments.ProfileFragment;
import com.example.eventscan.Helpers.EventArrayAdapter;

import com.example.eventscan.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/*
 * The activity that an Organizer first see's when they open the app.  Allows them to see all events, and their own.
 * Utilizes very similar code to the EventFragment. This will be consolidated into the EventFragment in a future build, as this is unneeded as a standalone activity.
 */


public class OrganizerEventsView extends AppCompatActivity implements View.OnClickListener {

    private ListView ownedEventsListView;
    private ListView inEventsListView;
    private ArrayList<Event> ownedEvents;
    private ArrayList<Event> inEvents;
    private EventArrayAdapter ownedEventsAdapter;
    private EventArrayAdapter inEventsAdapter;

    private FirebaseFirestore db;
    private CollectionReference eventsCollection;

    Button buttonOrganizerProfile;
    Button buttonSendNoti;
    Button buttonViewEvents;
    Button buttonAddEvent;

    TextView yourEventsText;
    RelativeLayout bubbleContainer;
    TextView atEventsText;
    RelativeLayout bubbleContainer2;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_events_view);

        ownedEvents = new ArrayList<>();
        inEvents = new ArrayList<>();

        ownedEventsAdapter = new EventArrayAdapter(OrganizerEventsView.this, R.layout.event_list_content, ownedEvents);
        inEventsAdapter = new EventArrayAdapter(OrganizerEventsView.this, R.layout.event_list_content, inEvents);

        ownedEventsListView = findViewById(R.id.ownedEvents);
        inEventsListView = findViewById(R.id.inEvents);

        ownedEventsListView.setAdapter(ownedEventsAdapter);
        inEventsListView.setAdapter(inEventsAdapter);

        // initialize firestore
        db = FirebaseFirestore.getInstance();
        eventsCollection = db.collection("events");

        // update events in real time
        eventsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) { // if there is an update then..
                    ownedEvents.clear();
                    inEvents.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                        Event event = doc.toObject(Event.class);
                        // event.getOrganizer().getDeviceID()
                        if (event.getOrganizer().getDeviceID().equals(getDeviceId(OrganizerEventsView.this))){
                            ownedEventsAdapter.add(event);
                            ownedEventsAdapter.notifyDataSetChanged();
                        } else {
                            inEventsAdapter.add(event);
                            // update listviews
                            inEventsAdapter.notifyDataSetChanged();
                        }
                    }

                }
            }
        });

        // XML
        yourEventsText = findViewById(R.id.yourEventsText);
        bubbleContainer = findViewById(R.id.bubbleContainer);
        atEventsText = findViewById(R.id.atEventsText);
        bubbleContainer2 = findViewById(R.id.bubbleContainer2);

        buttonOrganizerProfile = findViewById(R.id.buttonOrganizerProfile);
        buttonSendNoti = findViewById(R.id.buttonSendEventNoti);
        buttonViewEvents = findViewById(R.id.buttonViewEvents);
        buttonAddEvent = findViewById(R.id.buttonAddEvent);

        buttonOrganizerProfile.setOnClickListener(this);
        buttonSendNoti.setOnClickListener(this);
        buttonViewEvents.setOnClickListener(this);
        buttonAddEvent.setOnClickListener(this);

        // cite but implement correctly


        buttonViewEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        buttonOrganizerProfile.setOnClickListener(this);

    }

    /**
     * Handles button clicks. Opens the corresponding fragment or initiates
     * the organizer's profile view when the respective button is clicked.
     *
     * @param v The clicked View.
     */
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



    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Show XML elements when the back button is pressed
        yourEventsText.setVisibility(View.VISIBLE);
        bubbleContainer.setVisibility(View.VISIBLE);
        atEventsText.setVisibility(View.VISIBLE);
        bubbleContainer2.setVisibility(View.VISIBLE);
    }


    /**
     * Retrieves the device ID using Settings.Secure class.
     *
     * @param context The context of the application.
     * @return The device ID as a String.
     */
    public static String getDeviceId(Context context) {
        // Retrieve the device ID using Settings.Secure class
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}