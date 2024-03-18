package com.example.eventscan.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Organizer;
import com.example.eventscan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class UserSelection extends AppCompatActivity {

    private static final String PREF_USER_SELECTION = "UserSelection";
    private static final String KEY_SELECTION = "Selection";
    private static final String PREF_INSTALLATION_ID = "InstallationId";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.userselection);
        db = FirebaseFirestore.getInstance();

        String installationId = getInstallationId();

        if (installationId == null) {
            // Installation ID not found, generate a new one and create user
            installationId = generateInstallationId();
            saveInstallationId(installationId);
            showUserTypeSelection();
        } else {
            // Installation ID found, check user type
            goToActivity(getUserSelection());
        }
    }

    private void showUserTypeSelection() {
        final Button organizerButton = findViewById(R.id.buttonOrganizer);
        final Button attendeeButton = findViewById(R.id.buttonAttendee);
        final Button adminButton = findViewById(R.id.buttonAdministrator);

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSelection.this, LoginActivity.class);
                saveUserSelection("Admin");
                startActivity(intent);
            }
        });

        organizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserSelection("Organizer");
                createOrganizerUser();
            }
        });

        attendeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserSelection("Attendee");
                createAttendeeUser();
            }
        });
    }

    // generates id and saves it to shared preferences
    private String generateInstallationId() {
        String uuid = UUID.randomUUID().toString();
        saveInstallationId(uuid);
        return uuid;
    }

    // saves the installation ID to shared preferences
    private void saveInstallationId(String installationId) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_INSTALLATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_INSTALLATION_ID, installationId);
        editor.apply();
    }

    // retrieves the installation ID from shared preferences
    private String getInstallationId() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_INSTALLATION_ID, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREF_INSTALLATION_ID, null);
    }

    // creates an attendee user in Firestore and navigates to AttendeeEventsView activity
    private void createAttendeeUser() {
        String installationId = getInstallationId();
        Attendee attendee = new Attendee();
        db.collection("users").document(installationId).set(attendee)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(UserSelection.this, MainActivity.class);
                            intent.putExtra("userType", "Attendee");
                            startActivity(intent);
                            finish();
                        } else {
                            // Error creating user
                        }
                    }
                });
    }

    // creates an organizer user in Firestore and navigates to OrganizerEventsView activity
    private void createOrganizerUser() {
        String installationId = getInstallationId();
        Organizer organizer = new Organizer();
        db.collection("users").document(installationId).set(organizer)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(UserSelection.this, MainActivity.class);
                            intent.putExtra("userType", "Organizer");
                            startActivity(intent);
                            finish();
                        } else {
                            // Error creating user
                        }
                    }
                });
    }

    // saves the user selection (organizer or attendee) to shared preferences
    private void saveUserSelection(String selection) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_USER_SELECTION, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SELECTION, selection);
        editor.apply();
    }

    // navigates to the appropriate activity based on the user selection
    private void goToActivity(String selection) {
        if (selection != null) {
            if (selection.equals("Organizer")) {
                Intent intent = new Intent(UserSelection.this, MainActivity.class);
                intent.putExtra("userType", selection);
                startActivity(intent);
                finish();
            } else if (selection.equals("Attendee")) {
                Intent intent = new Intent(UserSelection.this, MainActivity.class);
                intent.putExtra("userType", selection);
                startActivity(intent);
                finish();
            } else if (selection.equals("Admin")) {
                Intent intent = new Intent(UserSelection.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    // retrieves the user selection (organizer or attendee) from shared preferences
    private String getUserSelection() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_USER_SELECTION, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SELECTION, null);
    }
}
