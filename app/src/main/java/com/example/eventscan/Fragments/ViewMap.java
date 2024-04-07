package com.example.eventscan.Fragments;


import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Dialog;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.eventscan.Database.Database;

import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.android.datatransport.backend.cct.BuildConfig;
import com.google.firebase.storage.FirebaseStorage;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;


public class ViewMap extends DialogFragment {

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
                Log.d("GeoLocation", String.valueOf(geoPoint));
            }
            avgLatitude = avgLatitude/points.size();
            avgLongitude = avgLongitude/points.size();
            GeoPoint center = new GeoPoint(avgLatitude,avgLongitude);
            mapView.getController().setCenter(center);
        }).addOnFailureListener(e -> {
            Log.d("GeolocationHandler", e.toString());
        });

        mapView.getController().setZoom(12.0);
        //GeoPoint
        returnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }
}
