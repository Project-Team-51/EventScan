package com.example.eventscan.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.User;
import com.example.eventscan.Helpers.EventArrayAdapter;
import com.example.eventscan.Helpers.UserArrayAdapter;
import com.example.eventscan.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AttendeeFragment extends Fragment {

    private ArrayList<User> allUser;
    private ListView allUserList;
    private UserArrayAdapter userAdapter;
    private FirebaseFirestore db;
    private CollectionReference usersCollection;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attendee_list_content, container, false);

        allUser = new ArrayList<>();

        userAdapter = new UserArrayAdapter(getActivity(), R.layout.event_list_content, allUser);

        allUserList = view.findViewById(R.id.allUserList);

        allUserList.setAdapter(userAdapter);

        // initialize firestore
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("users");

        // update events in real time
        usersCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) { // if there is an update then..
                    allUser.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                        User user = doc.toObject(User.class);
                        userAdapter.add(user);
                    }
                    userAdapter.notifyDataSetChanged(); // update listviews
                }
            }
        });

        return view;
    }

}
