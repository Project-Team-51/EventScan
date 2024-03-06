package com.example.eventscan.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.eventscan.Entities.Administrator;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Helpers.EventArrayAdapter;
import com.example.eventscan.R;

import java.util.ArrayList;

public class EventFragment extends Fragment{
    private ArrayList<Event> ownedEvents;
    private ArrayList<Event> inEvents;
    private ArrayAdapter<Event> ownedEventsAdapter;
    private ArrayAdapter<Event> inEventsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.admin_observer_fragment, container, false);

        ownedEvents = new ArrayList<>(); // Initialize empty, will be updated later
        inEvents = new ArrayList<>(); // Initialize empty, will be updated later
        ownedEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, ownedEvents);
        inEventsAdapter = new EventArrayAdapter(getActivity(), R.layout.event_list_content, inEvents);

        ListView ownedEventsListView = view.findViewById(R.id.ownedEvents);
        ownedEventsListView.setAdapter(ownedEventsAdapter);

        ListView inEventsListView = view.findViewById(R.id.inEvents);
        inEventsListView.setAdapter(inEventsAdapter);
        return view;
    }

    public void updateEvents(Administrator admin) {
        ownedEvents.clear();
        inEvents.clear();
        ownedEvents.addAll(admin.getOwnedEvents());
        inEvents.addAll(admin.getInEvents());
        ownedEventsAdapter.notifyDataSetChanged();
        inEventsAdapter.notifyDataSetChanged();
    }
}
