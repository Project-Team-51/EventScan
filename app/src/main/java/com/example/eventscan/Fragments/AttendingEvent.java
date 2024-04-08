package com.example.eventscan.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;
import java.util.Objects;

public class AttendingEvent extends DialogFragment {

    Database db;
    Attendee selfAttendee = null;
    Map<String, String> eventTypeMap;

    /**
     * Default constructor for the ViewEvent DialogFragment.
     */
    public AttendingEvent(Map<String, String> map) {
        this.eventTypeMap = map;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        Event selectedEvent = (Event) getArguments().getSerializable("selectedEvent");
        String eventType = eventTypeMap.get(selectedEvent.getEventID());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.event_information_view, null);

        TextView eventNameText = view.findViewById(R.id.stored_event_name);
        eventNameText.setText(selectedEvent.getName());

        TextView eventDetailsTextView = view.findViewById(R.id.stored_event_desc);
        eventDetailsTextView.setText(selectedEvent.getDesc());

        Dialog dialog = new Dialog(requireContext());
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);

        ImageView posterView = view.findViewById(R.id.poster_view);
        Button returnView = view.findViewById(R.id.return_view);
        Button SignupEventButton = view.findViewById(R.id.signup_event);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        db = Database.getInstance();
        SignupEventButton.setVisibility(View.GONE);
        TextView statusTextView = view.findViewById(R.id.status_event);
        switch (eventType) {
            case "interested":
                statusTextView.setText("Interested");
                break;
            case "checked_in":
                statusTextView.setText("Checked In");
                break;
        }
        StorageReference storageRef = storage.getReference().child("poster_pics");
        StorageReference posterRef = storageRef.child(selectedEvent.getEventID());

        posterRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Load the profile picture using an image loading library
                    Glide.with(this)
                            .load(uri)
                            .error(R.drawable.profile_icon) // Image to display in case of error
                            .into(posterView);
                });

        Fragment parentFragment = getParentFragment();
        EventFragment eventFragment = (EventFragment) parentFragment;

        returnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.dimAmount = 0.7f;
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}


