package com.example.eventscan.Fragments;


import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Dialog;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;


import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.eventscan.Database.Database;

import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.android.datatransport.backend.cct.BuildConfig;
import com.google.firebase.storage.FirebaseStorage;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

/**
 * This class represents a fragment used to display a map with checked-in attendee markers for
 * an organizer to see.
 */
public class ViewMap extends DialogFragment {
    // Reference: https://github.com/osmdroid/osmdroid

    Database db;
    ArrayList<GeoPoint> points = new ArrayList<>(); // Initialize the ArrayList

    /**
     * Default constructor for the ViewEvent DialogFragment.
     */
    public ViewMap() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        Event selectedEvent = (Event) getArguments().getSerializable("selectedEvent");
        View view = inflater.inflate(R.layout.map_view, null);

        Dialog dialog = new Dialog(requireContext());
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.dimAmount = 0.7f; // Adjust this value as needed
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button returnView = view.findViewById(R.id.return_button);
        MapView mapView = view.findViewById(R.id.mapView);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        db = Database.getInstance();
        db.geolocation.getEventCheckinPoints(selectedEvent);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        mapView.setTileSource(TileSourceFactory.MAPNIK); // Use the default OpenStreetMap tile source
        mapView.setMultiTouchControls(true); // Enable multi-touch controls

        // Add some GeoPoints to the ArrayList (example)
        db.geolocation.getEventCheckinPoints(selectedEvent).addOnSuccessListener(points1 -> {
            ArrayList<Marker> markers = new ArrayList<>(); // Create an ArrayList to hold markers
            double avgLatitude = 0;
            double avgLongitude = 0;
            points = points1;
            Log.d("GeolocationHandler3", String.valueOf(points.size()));
            // Iterate over the ArrayList and add markers to the map
            for (GeoPoint geoPoint : points) {
                avgLatitude += geoPoint.getLatitude();
                avgLongitude += geoPoint.getLongitude();
                Marker marker = new Marker(mapView);
                marker.setPosition(geoPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
                markers.add(marker); // Add marker to the list
                Log.d("GeoLocation", String.valueOf(geoPoint));
            }
            avgLatitude = avgLatitude / points.size();
            avgLongitude = avgLongitude / points.size();
            GeoPoint center = new GeoPoint(avgLatitude, avgLongitude);
            BoundingBox boundingBox = getBoundingBoxForMarkers(points); // Calculate bounding box
            if (boundingBox != null) {
                mapView.zoomToBoundingBox(boundingBox, true); // Zoom to the bounding box
            } else {
                mapView.getController().setCenter(center);
                mapView.getController().setZoom(12.0); // Set a default zoom level if bounding box is null
            }
        }).addOnFailureListener(e -> {
            Log.d("GeolocationHandler", e.toString());
        });




        //GeoPoint
        returnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }
    private BoundingBox getBoundingBoxForMarkers(ArrayList<GeoPoint> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }

        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (GeoPoint point : points) {
            double lat = point.getLatitude();
            double lon = point.getLongitude();

            minLat = Math.min(minLat, lat);
            maxLat = Math.max(maxLat, lat);
            minLon = Math.min(minLon, lon);
            maxLon = Math.max(maxLon, lon);
        }

        return new BoundingBox(maxLat+1.5, maxLon+1.5, minLat-1.5, minLon-1.5);
    }
}
