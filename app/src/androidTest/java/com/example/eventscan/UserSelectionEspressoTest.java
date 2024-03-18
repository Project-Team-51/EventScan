package com.example.eventscan;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventscan.DeprecatedActivities.AttendeeEventsView;
import com.example.eventscan.Activities.LoginActivity;
import com.example.eventscan.DeprecatedActivities.OrganizerEventsView;
import com.example.eventscan.Activities.UserSelection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserSelectionEspressoTest {

    @Before
    public void setUp() {
        // Initialize Intents before each test
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release Intents after each test
        Intents.release();
    }

    @Test
    public void testOrganizerButton() {
        // Launch the UserSelection activity
        ActivityScenario.launch(UserSelection.class);

        // Click the "Organizer" button
        Espresso.onView(ViewMatchers.withId(R.id.buttonOrganizer))
                .perform(ViewActions.click());

        // Verify that the OrganizerEventsView activity is started
        Intents.intended(IntentMatchers.hasComponent(OrganizerEventsView.class.getName()));
    }

    @Test
    public void testAttendeeButton() {
        // Launch the UserSelection activity
        ActivityScenario.launch(UserSelection.class);

        // Click the "Attendee" button
        Espresso.onView(ViewMatchers.withId(R.id.buttonAttendee))
                .perform(ViewActions.click());

        // Verify that the AttendeeEventsView activity is started
        Intents.intended(IntentMatchers.hasComponent(AttendeeEventsView.class.getName()));
    }

    @Test
    public void testAdministratorButton() {
        // Launch the UserSelection activity
        ActivityScenario.launch(UserSelection.class);

        // Click the "Administrator" button
        Espresso.onView(ViewMatchers.withId(R.id.buttonAdministrator))
                .perform(ViewActions.click());

        // Verify that the LoginActivity activity is started
        Intents.intended(IntentMatchers.hasComponent(LoginActivity.class.getName()));
    }
}

