package com.example.eventscan.Fragments;

import com.example.eventscan.Database.Database;
import com.example.eventscan.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AttendeeLimitDialogFragment extends DialogFragment {

    public interface AttendeeLimitListener {
        void onAttendeeLimitSet(int attendeeLimit);
    }

    private AttendeeLimitListener listener;

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.attendee_limit_dialog, null);

        EditText attendeeLimit = view.findViewById(R.id.editTextAttendeeLimit);
        Button confirmLimit = view.findViewById(R.id.buttonOK);
        Button cancelLimit = view.findViewById(R.id.buttonCancel);

        confirmLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String attendeeLimitString = attendeeLimit.getText().toString();
                if (!attendeeLimitString.isEmpty()) {
                    int attendeeLimitValue = Integer.parseInt(attendeeLimitString);
                    if (attendeeLimitValue <= 0) {
                        Toast.makeText(requireContext(), "Limit must be greater than 0", Toast.LENGTH_SHORT).show();
                    } else {
                        confirmAttendeeLimit(attendeeLimitValue);
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid integer", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        });

        cancelLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);

        return dialog;
    }

    public void setAttendeeLimitListener(AttendeeLimitListener listener) {
        this.listener = listener;
    }

    private void confirmAttendeeLimit(int attendeeLimitValue) {
        if (listener != null) {
            listener.onAttendeeLimitSet(attendeeLimitValue);
        }
        dismiss();
    }
}
