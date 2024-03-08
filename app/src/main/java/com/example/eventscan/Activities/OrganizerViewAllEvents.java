package com.example.eventscan.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Fragments.OrganizerViewAttendee;
import com.example.eventscan.Helpers.EventArrayAdapter;
import com.example.eventscan.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

/*
 * The activity that an Organizer see's when they click on the navigation button to see all events. Allows them to see all events.
 * Utilizes very similar code to the EventFragment. This will be consolidated into the EventFragment in a future build, as this is unneeded as a standalone activity.
 */
public class OrganizerViewAllEvents extends AppCompatActivity implements View.OnClickListener  {

    private ListView allEventsListView;
    private ArrayList<Event> allEvents;
    private EventArrayAdapter allEventsAdapter;
    private FirebaseFirestore db;
    private CollectionReference eventsCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_view_all);

        allEvents = new ArrayList<>();
        allEventsAdapter = new EventArrayAdapter(this, R.layout.event_list_content, allEvents);
        allEventsListView = findViewById(R.id.allEvents);
        allEventsListView.setAdapter(allEventsAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        eventsCollection = db.collection("events");

        // Update events in real-time
        eventsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    allEvents.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Event event = doc.toObject(Event.class);
                        allEventsAdapter.add(event);
                    }
                    allEventsAdapter.notifyDataSetChanged();
                }
            }
        });

        allEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = allEvents.get(position);

                // Navigate to OrganizerViewAttendee fragment passing event ID as argument
                OrganizerViewAttendee fragment = new OrganizerViewAttendee();
                Bundle args = new Bundle();
                args.putString("eventId", selectedEvent.getEventID());
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit();
            }

        });
    }

    /**
     * Handles button clicks. This method is not implemented in this class.
     *
     * @param v The clicked View.
     */

    @Override
    public void onClick(View v) {
        // Handle clicks if necessary
    }
}
