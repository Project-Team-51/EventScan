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

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Entities.User;
import com.example.eventscan.Helpers.EventArrayAdapter;
import com.example.eventscan.Helpers.UserArrayAdapter;
import com.example.eventscan.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
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
    private Database db;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views.
     * @param container          This is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState This fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attendee_list_content, container, false);

        allUser = new ArrayList<>();
        userAdapter = new UserArrayAdapter(getActivity(), R.layout.event_list_content, allUser);
        allUserList = view.findViewById(R.id.allUserList);
        allUserList.setAdapter(userAdapter);

        db = Database.getInstance();

        // update events in real time
        db.getAttendeeCollection().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) { // if there is an update then..
                    allUser.clear();
                    ArrayList<Task<Attendee>> userFetchTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) { // turn every stored "Event" into an event class, add to adapters
                        userFetchTasks.add(
                                db.attendees.get(doc.get("deviceID",String.class))
                                        .addOnSuccessListener(user -> {
                                            userAdapter.add(user);
                                        })
                        );
                    }
                    Tasks.whenAllComplete(userFetchTasks).addOnCompleteListener(task -> {
                        userAdapter.notifyDataSetChanged(); // update listviews
                    });
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

    /**
     * Callback method to handle profile deletion.
     *
     * @param user The user whose profile is to be deleted.
     */
    public void onDeleteProfile(User user) {
        deleteProfile(user);
    }

    /**
     * Opens the DeleteProfile fragment to confirm profile deletion.
     *
     * @param selectedUser The selected user whose profile deletion is to be confirmed.
     */
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

    /**
     * Deletes the user profile.
     *
     * @param user The user whose profile is to be deleted.
     */
    private void deleteProfile(User user) {
        // removes user from both the adapter and the firestore.
        userAdapter.remove(user);
        userAdapter.notifyDataSetChanged();
        Attendee temp = new Attendee();
        temp.setDeviceID(user.getDeviceID());
        db.attendees.delete(temp);
        //TODO refactor this so it uses Attendees instead of Users
    }
}
