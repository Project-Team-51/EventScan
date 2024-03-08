package com.example.eventscan.Fragments;

import android.app.Dialog;
import android.os.Bundle;
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
import com.example.eventscan.Entities.User;
import com.example.eventscan.R;

import java.util.Objects;


public class DeleteProfile extends DialogFragment {

    /**
     * Default constructor for the DeleteProfile DialogFragment.
     */
    public DeleteProfile() {
        // Required empty public constructor
    }

    /**
     * Interface for handling profile deletion.
     */
    public interface DeleteProfileListener {
        void onDeleteProfile(User user);
    }

    private DeleteProfileListener deleteProfileListener;

    /**
     * Called to create the dialog view.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state.
     * @return A Dialog representing the profile deletion dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        User selectedUser = (User) getArguments().getSerializable("selectedProfile");
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_delete_event_admin, null);
        TextView profileNameText = view.findViewById(R.id.stored_event_name);
        profileNameText.setText(selectedUser.getName());

        TextView eventDetailsTextView = view.findViewById(R.id.stored_event_desc);
        eventDetailsTextView.setText(selectedUser.getBio());

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        Button delEvent = view.findViewById(R.id.delete_event);
        Button returnAdmin = view.findViewById(R.id.return_admin);
        Fragment parentFragment = getParentFragment();
        ProfileFragment profileFragment = (ProfileFragment) parentFragment;

        delEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteProfileListener != null) {
                    deleteProfileListener.onDeleteProfile(selectedUser);}
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

    /**
     * Sets the profile listener for profile deletion.
     *
     * @param listener The listener to be set for profile deletion.
     */

    public void setDeleteProfileListener(DeleteProfileListener listener) {
        this.deleteProfileListener = listener;
    }
}

