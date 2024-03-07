package com.example.eventscan.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventscan.R;

import com.example.eventscan.Activities.OrganizerEventsView;

public class AddEvent extends Fragment{
    public AddEvent() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.add_event, container, false);

        Button returnToEventsButton = view.findViewById(R.id.return_to_event);
        Button generateQRCodeButton = view.findViewById(R.id.generate_QRCode);

        returnToEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OrganizerEventsView.class);
                startActivity(intent);
            }
        });

        generateQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle generate QR code button click
            }
        });

        return view;
    }
}

