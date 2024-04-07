package com.example.eventscan.Fragments;


import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Dialog;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.eventscan.Database.Database;

import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
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

        mapView.setTileSource(TileSourceFactory.MAPNIK); // Use the default OpenStreetMap tile source
        mapView.setMultiTouchControls(true); // Enable multi-touch controls

        // Add some GeoPoints to the ArrayList (example)
        db.geolocation.getEventCheckinPoints(selectedEvent).addOnSuccessListener(points1 -> {
            points = points1;
            // Iterate over the ArrayList and add markers to the map
            for (GeoPoint geoPoint : points) {
                Marker marker = new Marker(mapView);
                marker.setPosition(geoPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
            }
        });

        mapView.getController().setZoom(12.0);
        //GeoPoint
        //mapView.getController().setCenter();
        returnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }
}
