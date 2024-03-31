package com.example.eventscan.Helpers;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

public class GeolocationHandler {

    private Context context;
    private boolean locationUpdatesEnabled = false;

    public GeolocationHandler(Context context) {
        this.context = context;
    }

    public void enableLocationUpdates() {
        // Implement logic to start location updates here
        // For example, if using LocationManager:
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            // Check if network provider is enabled
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Start location updates using NETWORK_PROVIDER
                // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
                locationUpdatesEnabled = true;
                Log.d("GeolocationHandler", "Location updates enabled");
            } else {
                Log.e("GeolocationHandler", "Network provider is not enabled");
            }
        } else {
            Log.e("GeolocationHandler", "LocationManager is null");
        }
    }

    public void disableLocationUpdates() {
        // Implement logic to stop location updates here
        // For example, if using LocationManager:
        // locationManager.removeUpdates(locationListener);
        locationUpdatesEnabled = false;
        Log.d("GeolocationHandler", "Location updates disabled");
    }

    public boolean isLocationUpdatesEnabled() {
        return locationUpdatesEnabled;
    }
}
