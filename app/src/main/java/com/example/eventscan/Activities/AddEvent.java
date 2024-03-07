package com.example.eventscan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventscan.R;

import org.checkerframework.checker.units.qual.A;

public class AddEvent extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event);

        Button returnToEventsButton = findViewById(R.id.return_to_event);

        returnToEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddEvent.this, OrganizerEventsView.class);
                startActivity(intent);
            }
        });
    }


}

