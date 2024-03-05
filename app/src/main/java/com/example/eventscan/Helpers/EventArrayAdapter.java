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

import java.util.ArrayList;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    // The class responsible for displaying the list and the list's objects //
    //  -- Pretty simple, just has the View method to show individual list objects, and within View-
    // retrieves the necessary information about the event object, and pcik -- //
    // seems to be bug free //
    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.content, parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);
        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventDesc = view.findViewById(R.id.author_name);

        assert event != null;
        eventName.setText(event.getName());
        eventDesc.setText(event.getDesc());

        return view;
    }
}
