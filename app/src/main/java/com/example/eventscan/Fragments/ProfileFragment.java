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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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


/*
 * A simple fragment subclass for managing user profiles. Allows us to view our or another users profile, as well as edit some fields
 * if desired.
 */

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    ImageView profilePic;
    EditText usernameInput;
    EditText phoneInput;
    EditText emailInput;
    EditText bioInput;
    Button saveProfileBtn;
    Button deleteProfilePicBtn;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;
    public String deviceID;
    private static final int defaultProfileIcon = R.drawable.profile_icon;


    /**
     * Called when the fragment is being created.
     * Initializes the image pick launcher for selecting profile pictures.
     */
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

    /**
     * Inflates the layout for this fragment and initializes UI components.
     * Loads user profile information from Firestore and sets listeners for button clicks.
     */
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
        bioInput = view.findViewById(R.id.homepageEditText);
        saveProfileBtn = view.findViewById(R.id.saveButton);
        deleteProfilePicBtn = view.findViewById(R.id.deleteProfilePicButton);

        deleteProfilePicBtn.setVisibility(isProfilePictureUploaded() ? View.VISIBLE : View.GONE);

        loadProfileInfo();

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve input values
                String username = usernameInput.getText().toString();
                String phone = phoneInput.getText().toString();
                String email = emailInput.getText().toString();
                String bio = bioInput.getText().toString();


                String profilePictureID = "exampleProfilePictureID";

                // Creates a new Attendee object with the input values
                Attendee attendee = new Attendee();

                // Set attributes using setter methods
                attendee.setName(username);
                attendee.setPhoneNum(phone);
                attendee.setEmail(email);
                attendee.setBio(bio);
                attendee.setDeviceID(deviceID);
                attendee.setProfilePictureID(profilePictureID);

                // Saves the attendee's profile to Firestore
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
                // Calls a method to delete the profile picture
                deleteProfilePicture();
            }
        });

        return view;
    }

    /**
     * Saves the attendee's profile information to Firestore.
     * If a profile picture is selected, uploads it to Firebase Storage.
     *
     * @param attendee The Attendee object containing profile information.
     */
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

    /**
     * Checks if a profile picture is uploaded.
     *
     * @return True if a profile picture is uploaded, false otherwise.
     */
    private boolean isProfilePictureUploaded() {
        return selectedImageUri != null;
    }

    /**
     * Deletes the profile picture from Firebase Storage and updates the UI.
     */
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

    /**
     * Loads the user's profile information from Firestore and updates the UI.
     */
    private void loadProfileInfo() {
        db.collection("attendees")
                .document(deviceID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Attendee attendee = documentSnapshot.toObject(Attendee.class);
                        updateUIWithProfileInfo(attendee);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving profile information", e);
                });
    }

    /**
     * Updates the UI with the provided Attendee's profile information.
     *
     * @param attendee The Attendee object containing the profile information to display.
     */
    private void updateUIWithProfileInfo(Attendee attendee) {
        // Update UI elements with profile information
        if (attendee != null) {
            // Assuming that you have getters in the Attendee class
            usernameInput.setText(attendee.getName());
            phoneInput.setText(attendee.getPhoneNum());
            emailInput.setText(attendee.getEmail());
            bioInput.setText(attendee.getBio());


            // Load profile picture using Glide or Picasso (or any image loading library)
            if (attendee.getProfilePictureID() != null) {
                StorageReference profilePicRef = FirebaseStorage.getInstance()
                        .getReference()
                        .child("profile_pics")
                        .child(deviceID);

                profilePicRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // Load the profile picture using an image loading library
                            Glide.with(this)
                                    .load(uri)
                                    .apply(RequestOptions.circleCropTransform())
                                    .placeholder(R.drawable.profile_icon) // Placeholder image while loading
                                    .error(R.drawable.profile_icon) // Image to display in case of error
                                    .into(profilePic);


                            profilePic.setBackgroundResource(R.drawable.circular_background);

                            deleteProfilePicBtn.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error loading profile picture", e);
                        });
            }
        }
    }

}