package com.example.eventscan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.Database.Database;
import com.example.eventscan.R;
import com.google.firebase.FirebaseApp;
/*
 * This activity handles the logging in of an admin. Prompts the user to enter a user and password, and checks the inputted
 * text to the admin credentials stored in Firestore. Has error handling for incorrect credentials and a failure to connect to firestore.
 * Sign up button is unused, and will likely be removed in a future build.
 */
public class LoginActivity extends AppCompatActivity {
    private Database db;

    /**
     * Called when the activity is created. Initializes Firebase Firestore and sets up
     * the UI components for user login.
     *
     * @param savedInstanceState A Bundle containing the saved state of the activity.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.login_activity);

        db = Database.getInstance();

        // UI components
        EditText username = findViewById(R.id.editTextUsername);
        EditText password = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView result = findViewById(R.id.loginResult);

        // login button click listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Get entered username and password
                String user = username.getText().toString();
                String pass = password.getText().toString();

                // Access Firestore, compare user and passwords with inputted details
                db.admins.checkCredentials(user, pass)
                            .addOnSuccessListener(credentialsAreValid -> {
                                if(credentialsAreValid){
                                    // Start the admin activity
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("userType", "Admin");
                                    startActivity(intent);
                                } else {
                                    result.setText("Invalid Login");
                                }
                            }).addOnFailureListener(e -> {
                                result.setText("Error");
                            });
            }
        });
    }
}



