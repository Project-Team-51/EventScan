package com.example.eventscan.Activities;


import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.eventscan.Database.DatabaseHelper;

import com.example.eventscan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.login_activity);
        db = FirebaseFirestore.getInstance();

        EditText username = findViewById(R.id.editTextUsername);
        EditText password = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView result = findViewById(R.id.loginResult);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String user = username.getText().toString();
                String pass = password.getText().toString();
                db.collection("admin")
                        .whereEqualTo("user", user)
                        .whereEqualTo("pass", pass)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult() != null && !task.getResult().isEmpty()) {

                                        result.setText("Login Successful");

                                        Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                        startActivity(intent);

                                    } else {
                                        result.setText("Invalid Credentials");
                                    }
                                } else {
                                    result.setText("Error");
                                }
                            }
                        });
            }
        });
    }
}


