package com.example.eventscan.Fragments;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.R;
import com.google.firebase.firestore.FirebaseFirestore;


public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    ImageView profilePic;
    EditText usernameInput;
    EditText phoneInput;
    EditText emailInput;
    EditText bioInput;
    Button saveProfileBtn;
    ActivityResultLauncher<Intent> imagePickLauncher;



    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();

        profilePic = view.findViewById(R.id.profileImageView);
        usernameInput = view.findViewById(R.id.nameEditText);
        phoneInput = view.findViewById(R.id.phoneEditText);
        emailInput = view.findViewById(R.id.emailEditText);
        bioInput = view.findViewById(R.id.bioEditText);
        saveProfileBtn = view.findViewById(R.id.saveButton);

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve input values
                String username = usernameInput.getText().toString();
                String phone = phoneInput.getText().toString();
                String email = emailInput.getText().toString();
                String bio = bioInput.getText().toString();

                String deviceID = "exampleDeviceID"; // Replace with actual value
                String profilePictureID = "exampleProfilePictureID";

                // Create a new Attendee object with the input values
                Attendee attendee = new Attendee(username, phone, email, bio, deviceID, profilePictureID);

                // Save the attendee's profile to Firestore
                saveAttendeeProfile(attendee);
            }
        });


        return view;
    }

    private void saveAttendeeProfile(Attendee attendee) {
        // Replace "attendees" with the appropriate Firestore collection name
        db.collection("attendees")
                .document(attendee.getEmail()) // Use email as a document ID
                .set(attendee)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error saving profile");
                });
    }



}