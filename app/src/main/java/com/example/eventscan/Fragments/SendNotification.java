package com.example.eventscan.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SendNotification extends DialogFragment {

    /**
     * Interface for handling sending notifications.
     */
    public interface SendNotificationListener {
        void onSendNotification(Event event);
    }
    private SendNotificationListener sendNotificationListener;
    private String eventAnnouncement;
    private FirebaseFirestore db;

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        assert getArguments() != null;
        Event selectedEvent = (Event) getArguments().getSerializable("selectedEvent");
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.notification_fragment_layout, null);

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);

        TextView eventNameText = view.findViewById(R.id.stored_event_name);
        eventNameText.setText(selectedEvent.getName());

        EditText announcementEditText = view.findViewById(R.id.event_announcement);
        String eventAnnouncement = announcementEditText.getText().toString();

        Button cancelNoti = view.findViewById(R.id.send_noti);
        Button sendNoti = view.findViewById(R.id.cancel_noti);
        EventFragment eventFragment = new EventFragment();
        eventFragment.notificationSent(false);
        sendNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                selectedEvent.eventAnnouncements.add(eventAnnouncement); // Set the announcement to the selected event
//                eventFragment.notificationSent(true);
//                dismiss();
                selectedEvent.setEventAnnouncement(eventAnnouncement);
                db.collection("events").document(selectedEvent.getEventID())
                        .update("eventAnnouncement", eventAnnouncement)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Notify listeners if needed
                                if (sendNotificationListener != null) {
                                    sendNotificationListener.onSendNotification(selectedEvent);
                                }
                                // Dismiss the dialog
                                dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle any errors
                                Log.e("Firebase", "Error updating document", e);
                                // You might want to notify the user or retry the operation
                            }
                        });

            }
        });

        cancelNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    /**
     * Sets the event listener for event deletion.
     *
     * @param listener The listener to be set for event deletion.
     */

    public void setSendNotificationListener(SendNotificationListener listener) {
        this.sendNotificationListener = listener;
    }


}
