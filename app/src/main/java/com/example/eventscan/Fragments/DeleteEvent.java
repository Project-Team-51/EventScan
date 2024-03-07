package com.example.eventscan.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;

import java.util.Objects;

public class DeleteEvent extends DialogFragment {

    public DeleteEvent() {
        // Required empty public constructor
    }
    public interface DeleteEventListener {
        void onDeleteEvent(Event event);
    }
    private DeleteEventListener deleteEventListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        Event selectedEvent = (Event) getArguments().getSerializable("selectedEvent");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_delete_event_admin, null);

        TextView eventNameText = view.findViewById(R.id.stored_event_name);
        eventNameText.setText(selectedEvent.getName());

        TextView eventDetailsTextView = view.findViewById(R.id.stored_event_desc);
        eventDetailsTextView.setText(selectedEvent.getDesc());

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);

        Button delEvent = view.findViewById(R.id.delete_event);
        Button returnAdmin = view.findViewById(R.id.return_admin);
        Fragment parentFragment = getParentFragment();
        EventFragment eventFragment = (EventFragment) parentFragment;
        delEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteEventListener != null) {
                    deleteEventListener.onDeleteEvent(selectedEvent);}
                dismiss();
            }
        });

        returnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }
    public void setDeleteEventListener(DeleteEventListener listener) {
        this.deleteEventListener = listener;
    }
}



