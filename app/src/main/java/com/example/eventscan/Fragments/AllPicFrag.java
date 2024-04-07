package com.example.eventscan.Fragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventscan.Helpers.ImageAdapter;
import com.example.eventscan.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AllPicFrag extends Fragment {
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<Uri> imageList;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    // Get reference to the directory containing the images
    StorageReference imagesRef = storageRef.child("profile_pics");

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_images, container, false);

        recyclerView = view.findViewById(R.id.recyclerViews);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // Adjust the span count as needed
        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(getContext(), imageList);
        recyclerView.setAdapter(imageAdapter);

        imageAdapter.setOnItemClickListener(imageUri -> {

            showDialog(imageUri);
        });

        loadImagesFromFirebaseStorage();

        return view;
    }

    private void loadImagesFromFirebaseStorage() {
        // Retrieve all images from Firebase Storage
        imagesRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageList.add(uri);
                    imageAdapter.notifyDataSetChanged();
                });
            }
        }).addOnFailureListener(e -> {
        });
    }

    private void showDialog(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Do you want to delete?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Handle deletion
                    deleteImage(imageUri);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Do nothing
                })
                .show();
    }

    private void deleteImage(Uri imageUri) {
        // Convert image Uri to path
        String imagePath = getImagePath(imageUri);

        if (imagePath != null) {
            // Get reference to the image in Firebase Storage
            StorageReference imageRef = storageRef.child(imagePath);

            // Delete the image from Firebase Storage
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        // Image deleted successfully
                        // You may also want to remove the image from the RecyclerView and update its adapter
                        imageList.remove(imageUri);
                        imageAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to delete image
                        Toast.makeText(requireContext(), "Failed to delete image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Failed to get image path
            Toast.makeText(requireContext(), "Failed to delete image: Invalid path", Toast.LENGTH_SHORT).show();
        }
    }

    private String getImagePath(Uri imageUri) {
        String imagePath = null;
        if (imageUri != null) {
            imagePath = imageUri.getLastPathSegment();
        }
        return imagePath;
    }
}

