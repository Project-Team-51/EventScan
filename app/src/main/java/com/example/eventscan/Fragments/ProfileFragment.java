package com.example.eventscan.Fragments;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.DeviceID;
import com.example.eventscan.Entities.PicGen;
import com.example.eventscan.Helpers.GeolocationHandler;
import com.example.eventscan.Helpers.ImageUploader;
import com.example.eventscan.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.provider.Settings.Secure;
import android.widget.Switch;

import java.util.Random;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


/*
 * A simple fragment subclass for managing user profiles. Allows us to view our or another users profile, as well as edit some fields
 * if desired.
 */

public class ProfileFragment extends Fragment {

    private Database db;
    ImageView profilePic;
    EditText usernameInput;
    EditText phoneInput;
    EditText emailInput;
    EditText bioInput;
    Button saveProfileBtn;
    Button deleteProfilePicBtn;
    Switch locationToggle;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;
    public String attendeeName;
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

        db = Database.getInstance();

        deviceID = DeviceID.getDeviceID(requireContext());

        initializeViews(view);

        loadProfileInfo();
        setClickListeners();

        return view;
    }

    /**
     * Saves the attendee's profile information to Firestore.
     * If a profile picture is selected, uploads it to Firebase Storage.
     *
     * @param attendee The Attendee object containing profile information.
     */
    private void saveAttendeeProfile(Attendee attendee) {
        db.attendees.set(attendee).addOnSuccessListener(voidReturn -> {
            Log.d(TAG, "Profile saved successfully");
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Error saving profile");
        });

        //TODO replace with DB call
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
        PicGen.deleteProfilePicture(requireContext());
        profilePicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
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
            }
        });
    }

    /**
     * Loads the user's profile information from Firestore and updates the UI.
     */
    private void loadProfileInfo() {
        db.attendees.get(deviceID)
                .addOnSuccessListener(attendee -> {
                    // Update UI with profile information
                    updateUIWithProfileInfo(attendee);
                    // Generate or load profile picture
                    loadProfilePicture(attendee.getName());
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
        if (attendee != null) {
            // Update UI elements with profile information
            usernameInput.setText(attendee.getName());
            phoneInput.setText(attendee.getPhoneNum());
            emailInput.setText(attendee.getEmail());
            bioInput.setText(attendee.getBio());
        }
    }
    private void loadProfilePicture(String name) {
        if (PicGen.isProfilePictureExists(requireContext())) {
            Bitmap profileBitmap = PicGen.loadProfilePicture(requireContext());
            profilePic.setImageBitmap(profileBitmap);
        } else {
            String nameToUse = TextUtils.isEmpty(name) ? getRandomLetter() : name;
            Bitmap profileBitmap = PicGen.generateProfilePicture(nameToUse, 200); // Adjust size as needed
            profilePic.setImageBitmap(profileBitmap);
            PicGen.saveProfilePicture(requireContext(), profileBitmap);
        }
    }

    private String getRandomLetter() {
        Random random = new Random();
        char randomChar = (char) (random.nextInt(26) + 'a');
        return String.valueOf(randomChar).toUpperCase(); // Convert to uppercase to match the profile picture generator
    }
    private void initializeViews(View view) {
        // Initialize UI components
        profilePic = view.findViewById(R.id.profileImageView);
        usernameInput = view.findViewById(R.id.nameEditText);
        phoneInput = view.findViewById(R.id.phoneEditText);
        emailInput = view.findViewById(R.id.emailEditText);
        bioInput = view.findViewById(R.id.homepageEditText);
        saveProfileBtn = view.findViewById(R.id.saveButton);
        deleteProfilePicBtn = view.findViewById(R.id.deleteProfilePicButton);
        locationToggle = view.findViewById(R.id.locationToggle);
        boolean isLocationEnabled = GeolocationHandler.isLocationEnabled(requireContext());
        if (isLocationEnabled) {
            GeolocationHandler.enableLocationUpdates(getContext());
        }
        locationToggle.setChecked(isLocationEnabled);
    }
    private void setClickListeners() {
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

                // Updates UI to display the default profile picture
                profilePic.setImageResource(defaultProfileIcon);

                // Hides the delete profile pic button
                deleteProfilePicBtn.setVisibility(View.GONE);
            }
        });

        // Geolocation Handling
        locationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    GeolocationHandler.enableLocationUpdates(getContext());
                    GeolocationHandler.setLocationEnabled(getContext(),true);

                } else {
                    // User disabled location services
                    GeolocationHandler.disableLocationUpdates();
                    GeolocationHandler.setLocationEnabled(getContext(),false);
                }
            }
        });
    }
}