package com.example.eventscan.Fragments;


import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Dialog;


import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;

import com.example.eventscan.Database.Database;

import com.bumptech.glide.request.RequestOptions;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Database.DatabaseHelper;


import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.DeviceID;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;


public class ViewMap extends DialogFragment {

    Database db;
    ArrayList<IGeoPoint> points;
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
        View view = inflater.inflate(R.layout.map_view, null);

        Dialog dialog = new Dialog(requireContext());
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);

        Button returnView = view.findViewById(R.id.return_button);
        MapView mapView = view.findViewById(R.id.mapView);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        db = Database.getInstance();

        mapView.setTileSource(TileSourceFactory.MAPNIK); // Use the default OpenStreetMap tile source
        mapView.setMultiTouchControls(true); // Enable multi-touch controls

        for (GeoPoint geoPoint : points) {
            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }

        returnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }
}