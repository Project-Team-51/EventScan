package com.example.eventscan;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.example.eventscan.Activities.LoginActivity;
import com.example.eventscan.Activities.MainActivity;
import com.example.eventscan.Activities.UserSelection;
import com.example.eventscan.Entities.DeviceID;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

@RunWith(AndroidJUnit4.class)
public class loginEspresso {

    @Test
    public void testAdminLogin() {
        ActivityScenario.launch(UserSelection.class);

        // Perform click on the admin button
        onView(withId(R.id.buttonAdministrator))
                .perform(click());

        // Enter login credentials
        onView(withId(R.id.editTextUsername))
                .perform(ViewActions.typeText("admin"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.editTextPassword))
                .perform(ViewActions.typeText("adminpass"), ViewActions.closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.buttonLogin))
                .perform(click());


    }
}