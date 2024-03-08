package com.example.eventscan.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
/*
 * Displays all users in a ListView. Currently only supports the Admin, but can be reworked
 * to fit the Organizer class as well, when clicking on one of their events. Clicking on a list item
 * gives you the option to delete the profile from the app, deleting the user linked to their deviceID
 * on firestore.
 */
public class AttendeeFragment extends Fragment implements DeleteProfile.DeleteProfileListener{

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
        allUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedBook = allUser.get(position);
                openDeleteProfileFragment(selectedBook);
            }
        });
        return view;
    }
    public void onDeleteProfile(User user) {
        deleteProfile(user);
    }

    private void openDeleteProfileFragment(User selectedUser) {
        DeleteProfile deleteProfileFragment = new DeleteProfile();
        deleteProfileFragment.setDeleteProfileListener(this);

        // Create a Bundle and put the selected User information
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedProfile", selectedUser);
        deleteProfileFragment.setArguments(bundle);

        // Show the DeleteProfile fragment
        deleteProfileFragment.show(getParentFragmentManager(), "DeleteProfileFragment");
    }

    private void deleteProfile(User user) {
        // removes user from both the adapter and the firestore.
        userAdapter.remove(user);
        userAdapter.notifyDataSetChanged();
        db.collection("users").document(user.getDeviceID()).delete();
    }
}
