package com.example.eventscan.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventscan.Database.DatabaseHelper;
import com.example.eventscan.Fragments.EventFragment;
import com.example.eventscan.Fragments.qrCodeTestFrag;
import com.example.eventscan.R;

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);
        DatabaseHelper eventRepository = new DatabaseHelper();
        eventRepository.addSampleEvents();

        if (savedInstanceState == null) {
            EventFragment eventFragment = new EventFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_view, eventFragment);
            transaction.commit();
        }
    }
}
