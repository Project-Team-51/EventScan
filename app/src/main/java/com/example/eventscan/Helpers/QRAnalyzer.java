package com.example.eventscan.Helpers;

import static android.view.View.GONE;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.UseCase;

import com.example.eventscan.Entities.Attendee;
import com.example.eventscan.Entities.Event;
import com.example.eventscan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
/*
This class handles the opening of the camera, as well as scanning the QR Code and retrieving the relevant information
from it. Will be consolidated with QR Codec in the future.
 */
public class QRAnalyzer{
    //https://developers.google.com/ml-kit/vision/barcode-scanning/android#java

    BarcodeScanner scanner;
    Context context;
    FirebaseFirestore db;
    public QRAnalyzer(Context context){
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        scanner = BarcodeScanning.getClient(options);
        this.context = context;
        db = FirebaseFirestore.getInstance();
    }


    private void analyze(@NonNull ImageProxy imageProxy) {
        @OptIn(markerClass = ExperimentalGetImage.class) Image mediaImage = imageProxy.getImage();
        if(mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            Task<List<Barcode>> result = scanner.process(image).addOnSuccessListener(
                    barcodes -> {
                        for(Barcode bcode: barcodes){
                            if(QrCodec.verifyQRStringDecodable(Objects.requireNonNull(bcode.getRawValue()))){
                                // this QR code most likely fits our encoding scheme
                                String eventID = QrCodec.decodeQRString(Objects.requireNonNull(bcode.getRawValue()));
                                Log.d("QR SCAN", "This should now go to sign up for event "+eventID);
                                Attendee dummyAttendee = new Attendee("James", "123456", "abc@gmail", "hello :)", "deviceID", "profilePictureID");
                                createSignInDialog(eventID, dummyAttendee);
                            }
                        }
                    }
            );
        }
    }

    private void createSignInDialog(String eventID, Attendee selfAttendee){
        Dialog eventSignIn = new Dialog(context);
        eventSignIn.setContentView(R.layout.fragment_event_sign_in);
        eventSignIn.setCancelable(true);
        // get the data loaded in
        CollectionReference eventCollection = db.collection("events");
        //https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        eventCollection.document(eventID).get()

                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Event event = document.toObject(Event.class);
                            assert event != null;
                            ((TextView) eventSignIn.findViewById(R.id.sign_in_event_name)).setText(event.getName());
                            ((TextView) eventSignIn.findViewById(R.id.sign_in_event_description)).setText(event.getDesc());
                            Log.d("QR SCAN","completed, successful");
                            //TODO set the poster

                            // set the onclick of the button to sign you up
                            ((Button) eventSignIn.findViewById(R.id.sign_in_sign_in_button)).setOnClickListener(v -> {
                                event.addAttendee(selfAttendee);
                            });
                        } else {
                            Log.e("QR SCAN", "Event "+eventID+" not found in firebase");
                            Log.e("QR_SCAN", task.getException().toString());
                            ((TextView) eventSignIn.findViewById(R.id.sign_in_event_name)).setText("Event "+eventID);
                            ((TextView) eventSignIn.findViewById(R.id.sign_in_event_description)).setText("Not found\n(you may be offline)");
                            ((Button) eventSignIn.findViewById(R.id.sign_in_sign_in_button)).setVisibility(GONE);
                        }
                    }
                });

        eventSignIn.show();
    }

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
