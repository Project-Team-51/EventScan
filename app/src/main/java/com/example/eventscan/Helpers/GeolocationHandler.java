package com.example.eventscan.Helpers;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper class for managing location updates.
 */
public class GeolocationHandler {

    private static boolean locationUpdatesEnabled = false;
    private static LocationManager locationManager;
    private static LocationListener locationListener;
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
        }

        // Check for location permissions
        if (checkLocationPermissions(context) && !locationUpdatesEnabled) {
            // Location permissions granted, enable updates
            toggleLocationUpdates();
        } else {
            // Location permissions not granted, request permissions
            requestLocationPermissions((Activity) context);
        }
    }

    /**
     * Disables location updates.
     */
    public static void disableLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            // Stop location updates
            locationManager.removeUpdates(locationListener);
            locationUpdatesEnabled = false;
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
     * Toggles location updates based on current state.
     */
    private static void toggleLocationUpdates() {
        if (locationUpdatesEnabled) {
            // Stop location updates
            locationManager.removeUpdates(locationListener);
            locationUpdatesEnabled = false;
            Log.d("GeolocationHandler", "Location updates disabled");
        } else {
            // Start location updates
            try {
                // Check if GPS provider is enabled
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!isGpsEnabled) {
                    // Handle case where GPS provider is not enabled
                    Log.d("GeolocationHandler", "GPS provider is not enabled");
                    return;
                }

                // Request location updates to get current location
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationUpdatesEnabled = true;
                Log.d("GeolocationHandler", "Location updates enabled");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a location listener for handling location updates.
     * @return The created location listener.
     */
    private static LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Print geolocation information to logcat
                Log.d("GeolocationHandler", "Latitude: " + latitude + ", Longitude: " + longitude);
            }

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }
}