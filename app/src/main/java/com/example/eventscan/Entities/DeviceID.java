package com.example.eventscan.Entities;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

public class DeviceID {
    private static final String PREF_USER_SELECTION = "UserSelection";
    private static final String KEY_SELECTION = "Selection";
    private static final String PREF_INSTALLATION_ID = "InstallationId";

    public static String getDeviceID(Context context) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_INSTALLATION_ID, Context.MODE_PRIVATE);
            return sharedPreferences.getString(PREF_INSTALLATION_ID, null);
        }
    public static String getUserType(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_USER_SELECTION, context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SELECTION, null);
    }
}



    // retrieves the installation ID from shared preferences

