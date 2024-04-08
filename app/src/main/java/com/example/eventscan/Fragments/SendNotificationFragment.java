package com.example.eventscan.Fragments;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.load.data.DataRewinder;
import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Announcement;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SendNotificationFragment extends DialogFragment {
    /**
     * Interface for handling sending notifications.
     */
    public interface SendNotificationListener {
        void onSendNotification(Event event);
    }
    private SendNotificationListener sendNotificationListener;
    private String eventAnnouncement;
    private Database db = Database.getInstance();

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Event selectedEvent = (Event) getArguments().getSerializable("selectedEvent");
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.send_notification_layout, null);
        //db = Database.getInstance();
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);

        TextView eventNameText = view.findViewById(R.id.stored_event_name);
        eventNameText.setText(selectedEvent.getName());

        EditText announcementEditText = view.findViewById(R.id.event_announcement);


        Button sendNoti = view.findViewById(R.id.send_noti);
        Button cancelNoti = view.findViewById(R.id.cancel_noti);
        EventFragment eventFragment = new EventFragment();
        eventFragment.notificationSent(false);
        sendNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventAnnouncement = announcementEditText.getText().toString();
                Announcement announcement = new Announcement(eventAnnouncement);
//                Task<Void> eventNotification = db.announcements.saveNotification(selectedEvent, announcement);
                Task<Void> eventNotification = Database.getInstance().announcements.saveNotification(selectedEvent, announcement);

                // Call the saveNotification method
                eventNotification
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Announcement successfully added to Firestore");
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
                                Log.e("Firebase", "Error saving announcement", e);
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

// this needs to be fixed
//                db.collection("events").document(selectedEvent.getEventID())
//                        .update("eventAnnouncement", eventAnnouncement)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                // Notify listeners if needed
//                                if (sendNotificationListener != null) {
//                                    sendNotificationListener.onSendNotification(selectedEvent);
//                                }
//                                // Dismiss the dialog
//                                dismiss();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Handle any errors
//                                Log.e("Firebase", "Error updating document", e);
//                                // You might want to notify the user or retry the operation
//                            }
//                        });