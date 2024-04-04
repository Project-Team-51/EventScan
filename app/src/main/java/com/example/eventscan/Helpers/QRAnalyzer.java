package com.example.eventscan.Helpers;

import static android.view.View.GONE;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.UseCase;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Database.QRDatabaseEventLink;
import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/*
This class handles the opening of the camera, as well as scanning the QR Code and retrieving the relevant information
from it. Will be consolidated with QR Codec in the future.
 */
public class QRAnalyzer{
    //https://developers.google.com/ml-kit/vision/barcode-scanning/android#java

    BarcodeScanner scanner;
    Context context;
    Database db;
    Attendee selfAttendee = null;
    boolean attendeeFetchCompleted; // this will be removed later, just for forcing synchronous code
    public QRAnalyzer(Context context){
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        scanner = BarcodeScanning.getClient(options);
        this.context = context;
        db = Database.getInstance();

        // TODO replace this with a databaseHelper style static method call
        String deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        db.attendees.get(deviceID).addOnSuccessListener(attendee -> {
            selfAttendee = attendee;
        }).addOnFailureListener(e -> {
            Log.e("QR SCAN", "couldn't fetch selfAttendee: "+e.toString());
            attendeeFetchCompleted = true;
        });
    }

    /**
     * Analyzes the QR code obtained from the camera feed.
     *
     * @param imageProxy The image proxy containing the captured image.
     */
    private void analyze(@NonNull ImageProxy imageProxy) {
        @OptIn(markerClass = ExperimentalGetImage.class) Image mediaImage = imageProxy.getImage();
        if(mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            Task<List<Barcode>> result = scanner.process(image).addOnSuccessListener(
                    barcodes -> {
                        ArrayList<Task<QRDatabaseEventLink>> fetchQRTasks = new ArrayList<>();
                        ArrayList<QRDatabaseEventLink> fetchedLinks = new ArrayList<>();
                        AtomicBoolean hasError = new AtomicBoolean(false);
                        for(Barcode barcode: barcodes){
                            if(barcode.getRawValue() == null || !QrCodec.verifyQRStringDecodable(barcode.getRawValue())){
                                continue;
                            }
                            // this QR code is most likely from us (fits the encoding scheme)
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
                                createScanResultDialog(fetchedLinks.get(0));
                            }
                            else if(hasError.get()){
                                Toast.makeText(context, "Error scanning one or more QR codes", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(context, "Unknown error when scanning QR codes", Toast.LENGTH_SHORT).show();
                            }
                        });
                        scanner.close();
                    }
            );
        }
    }

    private void createScanResultDialog(QRDatabaseEventLink link){
        switch(link.getDirectionType()){
            case QRDatabaseEventLink.DIRECT_CHECK_IN:
                //TODO
            case QRDatabaseEventLink.DIRECT_SEE_DETAILS:
                createSignInDialog(link.getDirectedEventID());
        }
    }

    /**
     * Creates a sign-in dialog for the event.
     *
     * @param eventID       The ID of the event.
     */
    private void createSignInDialog(String eventID){
        Dialog eventSignIn = new Dialog(context);
        eventSignIn.setContentView(R.layout.fragment_event_sign_in);
        eventSignIn.setCancelable(true);
        Log.d("QR SCAN", eventID);
        Database.getInstance().events.get(eventID)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Event event = task.getResult();
                        ((TextView) eventSignIn.findViewById(R.id.sign_in_event_name)).setText(event.getName());
                        ((TextView) eventSignIn.findViewById(R.id.sign_in_event_description)).setText(event.getDesc());
                        Log.d("QR SCAN","completed, successful");
                        //TODO set the poster

                        // set the onclick of the button to sign you up
                        if(event.getCheckedInAttendeesList().contains(selfAttendee)){
                            ((Button) eventSignIn.findViewById(R.id.sign_in_sign_in_button)).setText("You've Already signed up");
                        } else {
                            ((Button) eventSignIn.findViewById(R.id.sign_in_sign_in_button)).setOnClickListener(v -> {
                                // make sure the selfAttendee has been returned, quick and dirty code, this will be changed
                                while(!attendeeFetchCompleted){
                                    try {
                                        Thread.sleep(20);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                if(selfAttendee == null){
                                    throw new RuntimeException("Attendee fetch failed :( This will be gracefully handled in the future");
                                }
                                event.checkInAttendee(selfAttendee);
                                db.events.checkInAttendee(event, selfAttendee);
                                eventSignIn.cancel();
                            });
                        }
                    } else {
                        Log.e("QR SCAN", "Event "+eventID+" not found in firebase");
                        Log.e("QR_SCAN", task.getException().toString());
                        ((TextView) eventSignIn.findViewById(R.id.sign_in_event_name)).setText("Event "+eventID);
                        ((TextView) eventSignIn.findViewById(R.id.sign_in_event_description)).setText("Not found\n(you may be offline)");
                        ((Button) eventSignIn.findViewById(R.id.sign_in_sign_in_button)).setVisibility(GONE);
                    }
                });

        eventSignIn.show();
    }

    /**
     * Retrieves the image analysis use case.
     *
     * @return The image analysis use case.
     */
    public UseCase getUseCase(){
        //https://beakutis.medium.com/using-googles-mlkit-and-camerax-for-lightweight-barcode-scanning-bb2038164cdc
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
        imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                        (ImageProxy imageProxy) -> analyze(imageProxy)
        );
        return imageAnalysis;
    }
}
