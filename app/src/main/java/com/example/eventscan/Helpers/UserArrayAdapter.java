package com.example.eventscan.Helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.User;
import com.example.eventscan.R;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<User> {

    /**
     * Constructs a new UserArrayAdapter.
     *
     * @param context The context in which the adapter is being used.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param users The list of User objects to represent in the ListView.
     */
    public UserArrayAdapter(Context context, int resource, ArrayList<User> users) {
        super(context, resource, users);
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

        User user = getItem(position);
        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventDesc = view.findViewById(R.id.event_desc);

        assert user != null;
        eventName.setText(user.getName());
        eventDesc.setText(user.getEmail());

        return view;
    }
}

