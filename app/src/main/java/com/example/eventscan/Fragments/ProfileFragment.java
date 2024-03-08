package com.example.eventscan.Fragments;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Helpers.ImageUploader;
import com.example.eventscan.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.provider.Settings.Secure;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    ImageView profilePic;
    EditText usernameInput;
    EditText phoneInput;
    EditText emailInput;
    EditText bioInput;
    Button saveProfileBtn;
    Button backButton;
    Button deleteProfilePicBtn;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;
    public String deviceID;
    private static final int defaultProfileIcon = R.drawable.profile_icon;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if(data!=null && data.getData()!=null) {
                            selectedImageUri = data.getData();
                            ImageUploader.setProfilePic(getContext(), selectedImageUri, profilePic);
                            deleteProfilePicBtn.setVisibility(View.VISIBLE);

                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();

        deviceID = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);

        profilePic = view.findViewById(R.id.profileImageView);
        usernameInput = view.findViewById(R.id.nameEditText);
        phoneInput = view.findViewById(R.id.phoneEditText);
        emailInput = view.findViewById(R.id.emailEditText);
        bioInput = view.findViewById(R.id.bioEditText);
        saveProfileBtn = view.findViewById(R.id.saveButton);
        backButton = view.findViewById(R.id.backButton);
        deleteProfilePicBtn = view.findViewById(R.id.deleteProfilePicButton);

        deleteProfilePicBtn.setVisibility(isProfilePictureUploaded() ? View.VISIBLE : View.GONE);

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve input values
                String username = usernameInput.getText().toString();
                String phone = phoneInput.getText().toString();
                String email = emailInput.getText().toString();
                String bio = bioInput.getText().toString();


                String profilePictureID = "exampleProfilePictureID";

                // Create a new Attendee object with the input values
                Attendee attendee = new Attendee(username, phone, email, bio, deviceID, profilePictureID);

                // Save the attendee's profile to Firestore
                saveAttendeeProfile(attendee);
            }
        });

        profilePic.setOnClickListener((v)->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent(new Function1<Intent, Unit>() {
                @Override
                public Unit invoke(Intent intent) {
                    imagePickLauncher.launch(intent);
                    return null;
                }
            });
        });

        deleteProfilePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call a method to delete the profile picture
                deleteProfilePicture();
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle back button click by popping the fragment from the back stack
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });


        return view;
    }


    private void saveAttendeeProfile(Attendee attendee) {
        db.collection("attendees")
                .document(deviceID) // Use deviceID as a document ID
                .set(attendee)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error saving profile");
                });

        if(selectedImageUri !=null){
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference profilePicRef = storageRef.child("profile_pics").child(deviceID);

            profilePicRef.putFile(selectedImageUri);

        }
    }

    private boolean isProfilePictureUploaded() {
        return selectedImageUri != null;
    }

    private void deleteProfilePicture() {
        // Delete the profile picture from Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePicRef = storageRef.child("profile_pics").child(deviceID);

        profilePicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                // Update the UI or perform any other necessary actions
                Drawable drawableDefaultProfileIcon = ContextCompat.getDrawable(requireContext(), defaultProfileIcon);
                profilePic.setImageDrawable(drawableDefaultProfileIcon);
                selectedImageUri = null; // Clear the selected image URI

                deleteProfilePicBtn.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error occurred while deleting the file
                Log.e(TAG, "Error deleting profile picture", e);
                // You can display an error message to the user if needed
            }
        });
    }

}