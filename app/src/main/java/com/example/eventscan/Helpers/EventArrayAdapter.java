package com.example.eventscan.Helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;

import java.util.ArrayList;
/*
Just a simple subclass that extends ArrayAdapter, retrieves the needed info from the Event class, and displays it.
For use in the ListView of all events, or events attending.
 */



public class EventArrayAdapter extends ArrayAdapter<Event> {
    // The class responsible for displaying the list and the list's objects //
    //  -- Pretty simple, just has the View method to show individual list objects, and within View-
    // retrieves the necessary information about the event object//
    // seems to be bug free //

    public EventArrayAdapter(Context context, int resource, ArrayList<Event> events) {
        super(context, resource, events);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_list_content, parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);
        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventDesc = view.findViewById(R.id.event_desc);

        assert event != null;
        eventName.setText(event.getName());
        eventDesc.setText(event.getDesc());

        return view;
    }
}
