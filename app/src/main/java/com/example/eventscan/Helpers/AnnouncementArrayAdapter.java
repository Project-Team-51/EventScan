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

public class AnnouncementArrayAdapter extends ArrayAdapter<Event> {

    public AnnouncementArrayAdapter(Context context, int resource, ArrayList<Event> events) {
        super(context, resource, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_announcements_list_content, parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);
        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventAnnouncement = view.findViewById(R.id.event_announcement);

        assert event != null;
        eventName.setText(event.getName());

        eventAnnouncement.setText(event.getEventAnnouncement());


        return view;
    }
}
