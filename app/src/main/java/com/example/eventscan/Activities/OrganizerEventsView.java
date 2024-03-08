package com.example.eventscan.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventscan.Entities.Event;
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

public class OrganizerEventsView extends AppCompatActivity implements View.OnClickListener {

    private ListView ownedEventsListView;
    private ListView inEventsListView;
    private ArrayList<Event> ownedEvents;
    private ArrayList<Event> inEvents;
    private EventArrayAdapter ownedEventsAdapter;
    private EventArrayAdapter inEventsAdapter;
    private FirebaseFirestore db;
    private CollectionReference eventsCollection;

    Button buttonAddEvent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_events_view);

        final Button buttonOrganizerProfile = findViewById(R.id.buttonOrganizerProfile);
        final Button buttonSendNoti = findViewById(R.id.buttonSendEventNoti);
        final Button buttonViewEvents = findViewById(R.id.buttonViewEvents);
        Button buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(this);

        ownedEvents = new ArrayList<>();
        inEvents = new ArrayList<>();

        ownedEventsAdapter = new EventArrayAdapter(OrganizerEventsView.this, R.layout.event_list_content, ownedEvents);
        inEventsAdapter = new EventArrayAdapter(OrganizerEventsView.this, R.layout.event_list_content, inEvents);

        ownedEventsListView = findViewById(R.id.ownedEvents);
        inEventsListView = findViewById(R.id.inEvents);

        ownedEventsListView.setAdapter(ownedEventsAdapter);
        inEventsListView.setAdapter(inEventsAdapter);

        db = FirebaseFirestore.getInstance();
        eventsCollection = db.collection("events");

        eventsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                // Handle the snapshot and error
                onEvent(querySnapshots, error);
            }
        });
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

    public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
        if (error != null) {
            Log.e("Firestore", error.toString());
            return;
        }
        if (querySnapshots != null) { // if there is an update then..
            Log.e("HELLO", error.toString());
            ownedEvents.clear();
            inEvents.clear();
            for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                Event event = doc.toObject(Event.class);
                if ( event.getOrganizer().getDeviceID() == getDeviceId(OrganizerEventsView.this) ){
                    ownedEventsAdapter.add(event);
                    ownedEventsAdapter.notifyDataSetChanged(); // update listviews
                    Log.d("EventAdded (Owned)", "Event added to ownedEvents: " + event.getName());
                } else {
                    inEventsAdapter.add(event);
                    inEventsAdapter.notifyDataSetChanged();
                    Log.d("EventAdded (Attending)", "Event added to inEvents: " + event.getName());
                }
            }
        } else {
            Log.e("BYE", error.toString());
        }
    }

    public static String getDeviceId(Context context) {
        // Retrieve the device ID using Settings.Secure class
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}