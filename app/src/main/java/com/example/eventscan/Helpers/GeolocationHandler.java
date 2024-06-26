package com.example.eventscan.Helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;

import android.util.Log;

import android.content.SharedPreferences;

import org.osmdroid.util.GeoPoint;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * Helper class for managing location updates.
 */
public class GeolocationHandler {

    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private static final String LOCATION_PREFS = "location_prefs";
    private static final String LOCATION_ENABLED_KEY = "location_enabled";
    private static boolean isEnabled;
    private static double latitude;
    private static double longitude;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    /**
     * Enables location updates if permissions are granted, otherwise requests permissions.
     * @param context The context from which this method is called.
     */
    public static void enableLocationUpdates(Context context) {
        // Initialize location manager and listener if not already initialized
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationListener = createLocationListener();
            isEnabled = true;
        }

        // Check for location permissions
        if (checkLocationPermissions(context)) {
            // Location permissions granted, request a single location update
            Log.d("GeolocationHandler", "Location updates enabled");
            isEnabled = true;
            requestSingleLocationUpdate(context);
        } else {
            // Location permissions not granted, request permissions
            requestLocationPermissions((Activity) context);
        }
    }

    /**
     * Requests a single location update.
     * @param context The context from which this method is called.
     */
    public static void requestSingleLocationUpdate(Context context) {
        try {
            // Check if GPS provider is enabled
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGpsEnabled) {
                // Handle case where GPS provider is not enabled
                Log.d("GeolocationHandler", "GPS provider is not enabled");
                return;
            }
            if (isEnabled) {
                // Request a single location update to get the current location
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }
            else{
                Log.d("GeolocationHandler", "Geolocation is turned off");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disables location updates.
     */
    public static void disableLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            // Stop location updates
            locationManager.removeUpdates(locationListener);
            isEnabled = false;
            Log.d("GeolocationHandler", "Location updates disabled");
        }
    }

    /**
     * Checks if location permissions are granted.
     * @param context The context from which this method is called.
     * @return True if permissions are granted, false otherwise.
     */
    private static boolean checkLocationPermissions(Context context) {
        // Check if location permissions are granted
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests location permissions.
     * @param activity The activity from which this method is called.
     */
    private static void requestLocationPermissions(Activity activity) {
        // Request location permissions
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Creates a location listener for handling location updates.
     * @return The created location listener.
     */
    private static LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // Print geolocation information to logcat
                Log.d("GeolocationHandler", "Latitude: " + latitude + ", Longitude: " + longitude);

                // Stop listening for location updates after receiving the first update
            }

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    /**
     * Gets the latitude value of the last retrieved location.
     * @return The latitude value.
     */
    public static double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude value of the last retrieved location.
     * @return The longitude value.
     */
    public static double getLongitude() {
        return longitude;
    }

    /**
     * Combines latitude and latitude into a GeoPoint for map use
     * @return GeoPoint value.
     */
    public static GeoPoint getGeoPoint(){
        return new GeoPoint(latitude,longitude);
    }

    /**
     * Returns whether user location is enabled or not
     * @return boolean isEnabled
     */
    public static boolean getLocationEnabled(){
        return isEnabled;
    }

    /**
     * Sets location enabled after an app reboot
     * @param context The context of the app
     * @param isEnabled boolean of whether locaiton is enabled
     */
    public static void setLocationEnabled(Context context, boolean isEnabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATION_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOCATION_ENABLED_KEY, isEnabled);
        editor.apply();
    }

    /**
     * Retrieve the state of the location setting
     * @param context The context of the app
     */
    public static boolean isLocationEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATION_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(LOCATION_ENABLED_KEY, false); // Default value is false
    }

}
