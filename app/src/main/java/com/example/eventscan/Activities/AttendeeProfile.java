package com.example.eventscan.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.Database.Profile;
import com.example.eventscan.R;

import java.util.ArrayList;
import java.util.List;

public class AttendeeProfile extends AppCompatActivity {

    private ImageView profilePicture;
    private EditText editTextName, editTextEmail, editTextPhoneNumber, editTextDescription;
    private List<Profile> profileList;
    private int currentProfileIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        // Views
        profilePicture = findViewById(R.id.profilePicture);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextDescription = findViewById(R.id.editTextDescription);

        // Create a list to store profiles
        profileList = new ArrayList<>();



        // Add a sample profile to the list (you can replace this with actual data)
        //profileList.add(new Profile(R.drawable.default_profile_image, "John Doe", "john.doe@example.com", "+1 (555) 123-4567", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."));

        // Set the initial profile data
        setCurrentProfile(0);

        Button backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Go back to the previous activity
            }
        });

    }

    private void setCurrentProfile(int index) {
        if (index >= 0 && index < profileList.size()) {
            Profile currentProfile = profileList.get(index);
            profilePicture.setImageResource(currentProfile.getProfilePictureId());
            editTextName.setText(currentProfile.getName());
            editTextEmail.setText(currentProfile.getEmail());
            editTextPhoneNumber.setText(currentProfile.getPhoneNumber());
            editTextDescription.setText(currentProfile.getDescription());

            currentProfileIndex = index;
        }
    }

}
