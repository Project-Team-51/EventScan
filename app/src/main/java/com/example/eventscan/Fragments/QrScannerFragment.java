package com.example.eventscan.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;


import com.example.eventscan.Helpers.QRAnalyzer;
import com.example.eventscan.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class QrScannerFragment extends Fragment {
    //https://developer.android.com/media/camera/camerax/preview
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private QRAnalyzer analyzer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        analyzer = new QRAnalyzer(getContext());
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext());
        View view = getLayoutInflater().inflate(R.layout.scan_qr_layout, container, false);
        previewView = view.findViewById(R.id.CameraPreview);
        checkCameraPermissions(this.requireContext());
        try {
            bindPreview(cameraProviderFuture.get());
            Log.i("Camera View", "should be bound");
        } catch (ExecutionException | InterruptedException e) {
            Log.e("Camera View", "Not working :(");
            throw new RuntimeException(e);
        }
        return view;
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, analyzer.getUseCase());
    }

    private static void checkCameraPermissions(Context context){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // need to ask for permission
            ActivityCompat.requestPermissions((Activity) context, new String[] {Manifest.permission.CAMERA }, 100);
        }
    }
}
