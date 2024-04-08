package com.example.eventscan.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
/*
 * A simple dialogfragment that displays some basic info about the User Profile thats passed into it, and gives
 * the user the ability to delete the profile from the app.
 */


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
        View view = inflater.inflate(R.layout.fragment_browse_profile, null);

        TextView profileNameText = view.findViewById(R.id.nameEditText);
        profileNameText.setText(selectedUser.getName());

        Button deleteButton = view.findViewById(R.id.deleteProfile);
        deleteButton.setOnClickListener(v -> {
            if (deleteProfileListener != null) {
                deleteProfileListener.onDeleteProfile(selectedUser);
            }
            dismiss();
        });

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.dimAmount = 0.7f; // Adjust this value as needed
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button returnButton = view.findViewById(R.id.saveButton);
        // Set OnClickListener to collapse the fragment
        returnButton.setOnClickListener(v -> dismiss());

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

