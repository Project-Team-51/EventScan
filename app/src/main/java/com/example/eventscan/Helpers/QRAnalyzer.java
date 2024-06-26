package com.example.eventscan.Helpers;

import static android.view.View.GONE;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.UseCase;
import androidx.fragment.app.FragmentManager;

import com.example.eventscan.Activities.MainActivity;
import com.example.eventscan.Database.Database;
import com.example.eventscan.Database.QRDatabaseEventLink;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.DeviceID;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.Fragments.ViewEvent;
import com.example.eventscan.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
This class handles the opening of the camera, as well as scanning the QR Code and retrieving the relevant information
from it. Will be consolidated with QR Codec in the future.
 */
public class QRAnalyzer{
    //https://developers.google.com/ml-kit/vision/barcode-scanning/android#java

    BarcodeScanner scanner;
    Context context;
    Database db;
    Task<Attendee> selfAttendeeTask = null;
    Attendee selfAttendee = null;
    private boolean qrScanComplete = false;

    private final boolean scanReadOrScanForReuse; // false for read, true for reuse
    QRDatabaseEventLink reuseEventLink;

    FragmentManager parentFragmentManager;

    public QRAnalyzer(Context context, FragmentManager parentFragmentManager, boolean scanReadOrScanForReuse){
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        scanner = BarcodeScanning.getClient(options);
        this.context = context;
        this.parentFragmentManager = parentFragmentManager;
        this.scanReadOrScanForReuse = scanReadOrScanForReuse;
        db = Database.getInstance();
        String deviceID = DeviceID.getDeviceID(context);
        selfAttendeeTask = db.attendees.get(deviceID).addOnSuccessListener(attendee -> {
            selfAttendee = attendee;
        }).addOnFailureListener(e -> {
            Log.e("QR SCAN", "couldn't fetch selfAttendee: "+e.toString());
        });
    }

    /**
     * Analyzes the QR code obtained from the camera feed.
     *
     * @param imageProxy The image proxy containing the captured image.
     */
    private void analyze(@NonNull ImageProxy imageProxy) {
        @OptIn(markerClass = ExperimentalGetImage.class) Image mediaImage = imageProxy.getImage();
        if(qrScanComplete){
            return;
        }
        if(mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            Task<List<Barcode>> result = scanner.process(image).addOnSuccessListener(
                    barcodes -> {
                        Log.d("QR Analyzer", "QR scan success "+barcodes.size()+" barcodes found");
                        ArrayList<Task<QRDatabaseEventLink>> fetchQRTasks = new ArrayList<>();
                        ArrayList<QRDatabaseEventLink> fetchedLinks = new ArrayList<>();
                        AtomicBoolean hasError = new AtomicBoolean(false);
                        for(Barcode barcode: barcodes){
                            if(barcode.getRawValue() == null){
                                continue;
                            }
                            qrScanComplete = true;
                            // else this QR code is most likely from us (fits the encoding scheme)
                            String barcodeContent = QrCodec.decodeQRString(barcode.getRawValue());
                            Task<QRDatabaseEventLink> getQRTask = Database.getInstance().qr_codes.get(barcodeContent);
                            getQRTask.continueWithTask(task -> {
                                if(!task.isSuccessful()){
                                    return Tasks.forException(Database.getTaskException(task));
                                }
                                return Database.getInstance().events.checkExistence(task.getResult().getDirectedEventID())
                                        .continueWithTask(task1 -> {
                                            if(!task1.isSuccessful()){
                                                return Tasks.forException(Database.getTaskException(task1));
                                            }
                                            if(task1.getResult()){
                                                // the event actually exists, return the result of the qr_codes.get() task (1 layer up)
                                                return Tasks.forResult(task.getResult());
                                            }
                                            // else the event doesn't exist, return an error
                                            return Tasks.forException(new Exception("Searched event does not exist"));
                                        });
                            });
                            getQRTask.addOnCompleteListener(task -> {
                                if(!task.isSuccessful()){
                                    hasError.set(true);
                                }
                                fetchedLinks.add(task.getResult());
                            });
                            fetchQRTasks.add(getQRTask);
                        }
                        Tasks.whenAllComplete(fetchQRTasks).addOnCompleteListener(task -> {
                            // ideally fetchedLinks is only one, and hasError is false
                            // if we have at least one event, use the first one, otherwise we can display the error if we have one
                            if(!fetchedLinks.isEmpty()){
                                Log.d("QR Analyzer", "creating a scan result dialog");
                                createScanResultDialog(fetchedLinks.get(0));
                            }
                            else if(hasError.get()){
                                Toast.makeText(context, "Error scanning one or more QR codes", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).addOnFailureListener(e -> {
                        Log.e("QR", e.toString());
                    }).addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
        }
    }

    private void analyzeForReuse(@NonNull ImageProxy imageProxy, QRDatabaseEventLink linkToWrite){
        @OptIn(markerClass = ExperimentalGetImage.class) Image mediaImage = imageProxy.getImage();
        if(qrScanComplete){
            return;
        }
        if(mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            scanner.process(image).addOnSuccessListener(barcodes -> {
                for(Barcode barcode: barcodes){
                    if(barcode.getRawValue() == null){
                        continue;
                    }
                    qrScanComplete = true;
                    String barcodeContent = QrCodec.decodeQRString(barcode.getRawValue());
                    // now write the link
                    db.qr_codes.set(barcodeContent, linkToWrite).addOnSuccessListener(ret -> {
                        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to reuse the barcode, please try again later", Toast.LENGTH_SHORT).show();
                    });
                }

            });
        }

    }

    private void createScanResultDialog(QRDatabaseEventLink link){
        if(link == null){
            createFailureFetchDialog();
            return;
        }
        switch(link.getDirectionType()){
            case QRDatabaseEventLink.DIRECT_CHECK_IN:
                Log.d("QR Analyzer", "spawning a check in dialog");
                createCheckInDialog(link.getDirectedEventID());
                break;
            case QRDatabaseEventLink.DIRECT_SEE_DETAILS:
                Log.d("QR Analyzer", "spawning a sign up interest dialog");
                createSignUpInterestDialog(link.getDirectedEventID());
                break;
        }
    }

    private void createFailureFetchDialog(){
        Dialog failure = new Dialog(context);
        failure.setContentView(R.layout.fragment_event_sign_in);
        failure.setCancelable(true);
        ((TextView) failure.findViewById(R.id.sign_in_event_name)).setText("Error");
        ((TextView) failure.findViewById(R.id.sign_in_event_description)).setText("This event code doesn't exist in our system. Please ask the organizer of this event for a new code");
        ((Button) failure.findViewById(R.id.sign_in_sign_in_button)).setVisibility(GONE);
        failure.show();
    }

    /**
     * Creates a check-in dialog for the event.
     *
     * @param eventID       The ID of the event.
     */
    private void createCheckInDialog(String eventID){
        Dialog eventSignIn = new Dialog(context);
        eventSignIn.setContentView(R.layout.fragment_event_sign_in);
        eventSignIn.setCancelable(true);
        Log.d("QR Analyzer", "creating Checkin dialog for event " + eventID);
        if(selfAttendee == null){
            // a little bit goofy
            selfAttendee = new Attendee();
            selfAttendee.setDeviceID(DeviceID.getDeviceID(context));
        }
        Database.getInstance().events.get(eventID)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Event event = task.getResult();
                        TextView dialogTitle = eventSignIn.findViewById(R.id.sign_in_event_name);
                        TextView dialogDescription = eventSignIn.findViewById(R.id.sign_in_event_description);
                        Button dialogButton = eventSignIn.findViewById(R.id.sign_in_sign_in_button);
                        dialogTitle.setText(event.getName());
                        dialogDescription.setText(event.getDesc());
                        Log.d("QR SCAN","completed, successful");

                        dialogButton.setVisibility(View.VISIBLE);
                        // set the onclick of the button to sign you up
                        dialogButton.setOnClickListener(v -> {
                            if (event.getCheckedInAttendeesList().size() >= event.getAttendeeLimit() && event.getAttendeeLimit() != -1) {
                                // Event is full, show a toast message and return
                                Toast.makeText(context, "Event is full. No more attendees can check in.", Toast.LENGTH_SHORT).show();
                            } else {
                                event.checkInAttendee(selfAttendee);
                                Task<?> checkInTask;
                                Log.d("GeolocationHandler", String.valueOf(GeolocationHandler.getLocationEnabled()));
                                if (GeolocationHandler.getLocationEnabled()) {
                                    checkInTask = db.events.checkInAttendee(event, selfAttendee, GeolocationHandler.getGeoPoint());
                                } else {
                                    checkInTask = db.events.checkInAttendee(event, selfAttendee);
                                }

                                checkInTask.addOnCompleteListener(task1 -> {
                                    // check-in is done, make sure it was successful
                                    if (task1.isSuccessful()) {

                                        dialogDescription.setText("Check-in Successful!\n You can now safely go to another screen");
                                        dialogButton.setVisibility(GONE);

                                        dialogButton.setText(R.string.event_sign_up_success);
                                        dialogButton.setEnabled(false);

                                    } else {
                                        Log.e("QR SCAN", Database.getTaskException(task1).toString());
                                        dialogDescription.setText(R.string.event_sign_up_failure_description);
                                        dialogButton.setText(R.string.event_sign_up_failure_exit);
                                        dialogButton.setOnClickListener(v1 -> {
                                            // https://stackoverflow.com/questions/17719634/how-to-exit-an-android-app-programmatically
                                            System.exit(1);
                                        });
                                    }
                                });
                            }
                        });
                    } else {
                        Log.e("QR SCAN", "Event "+eventID+" not found in firebase");
                        Log.e("QR SCAN", Database.getTaskException(task).toString());
                        ((TextView) eventSignIn.findViewById(R.id.sign_in_event_name)).setText("Event "+eventID);
                        ((TextView) eventSignIn.findViewById(R.id.sign_in_event_description)).setText("Not found\n(you may be offline)");
                    }
                });


        eventSignIn.show();
    }

    private void createSignUpInterestDialog(String eventID){
        db.events.get(eventID).addOnSuccessListener(event -> {
            ViewEvent viewEventFragment = new ViewEvent();
            // Create a Bundle and put the selected Event information
            Bundle bundle = new Bundle();
            bundle.putSerializable("selectedEvent", event);
            viewEventFragment.setArguments(bundle);
            // Show the DeleteEvent fragment
            viewEventFragment.show(parentFragmentManager, "ViewEventFragment");

        }).addOnFailureListener(e -> {
            createFailureFetchDialog();
        });
    }

    public void setReuseEventLink(QRDatabaseEventLink link){
        this.reuseEventLink = link;
    }

    /**
     * Retrieves the image analysis use case.
     *
     * @return The image analysis use case.
     */
    public UseCase getUseCase(){
        //https://beakutis.medium.com/using-googles-mlkit-and-camerax-for-lightweight-barcode-scanning-bb2038164cdc
        //https://developers.google.com/ml-kit/vision/barcode-scanning/android
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        if(!this.scanReadOrScanForReuse) {
            imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    (ImageProxy imageProxy) -> analyze(imageProxy)
            );
        } else {
            imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    (ImageProxy imageProxy) -> analyzeForReuse(imageProxy, reuseEventLink)
            );
        }
        return imageAnalysis;
    }
}
