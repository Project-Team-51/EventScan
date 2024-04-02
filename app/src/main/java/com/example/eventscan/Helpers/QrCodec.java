package com.example.eventscan.Helpers;

/*
Helper class that parses the string encoded to and decoded from a QR Code.
 */

import android.graphics.Bitmap;

import com.example.eventscan.Database.Database;
import com.example.eventscan.Entities.Event;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QrCodec {

    /**
     * Encodes a string for QR code.
     *
     * @param toEncode The string to encode.
     * @return The encoded string for QR code.
     */
    public static String encodeQRString(String toEncode){
        return "EventScan_event"+toEncode;
    }

    /**
     * Verifies if the QR string is decodable.
     *
     * @param toDecode The string to decode.
     * @return True if the QR string is decodable, false otherwise.
     */
    public static boolean verifyQRStringDecodable(String toDecode){
        return toDecode.startsWith("EventScan_event");
    }

    /**
     * Decodes the QR string.
     *
     * @param toDecode The string to decode.
     * @return The decoded string from the QR code.
     * @throws RuntimeException if attempting to decode a non-decodable QR string.
     */
    public static String decodeQRString(String toDecode){
        if(!verifyQRStringDecodable(toDecode)){
            throw new RuntimeException("Attempted to decode a non-decodable QR string");
        }
        return toDecode.substring(15); // "EventScan_event" is 15 chars long
    }

    /**
     * Creates a new QR code for an event.
     *
     * @param event The event to create a QR code for.
     * @param linkType the type of link.
     *                 use QRDatabaseEventLink.DIRECT_[XYZ] as the argument
     * @param forceNewCreation if true, force the creation of a new code,
     *                         if false it will try to reuse an existing code for this event and link type
     * @return The task of the QR code bitmap.
     */
    public static Task<Bitmap> createOrGetQR(Event event, int linkType, boolean forceNewCreation) {
        //                            if forcing...       get a unique ID...                                     else check if we can use an existing one
        Task<String> getQRDataTask = (forceNewCreation) ? Database.getInstance().qr_codes.generateUniqueQrID() : getExistingQRDataElseReserve(event, linkType);
        return getQRDataTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw new Exception("No bueno");
                    }
                    // now returns task of type string
                    return Database.getInstance().qr_codes.set(task.getResult(), event, linkType);
                } )
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw new Exception("No bueno");
                    }
                    String qrID = task.getResult();
                    String newlyEncoded = encodeQRString(qrID);
                    return encodeToQrImage(newlyEncoded);
                });
    }


    /**
     * Encodes a string to a QR image.
     *
     * @param encodedString The string to encode.
     * @return The generated QRcode bitmap.
     * @throws RuntimeException if an error occurs during the encoding process
     *
     */
    private static Bitmap encodeToQrImage(String encodedString){
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(encodedString, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get an existing QR Data string that points to this event with this link type,
     * or return a new one if none exist
     * @param event the event to search for
     * @param linkType the link type to search for
     * @return a task which will have a string of QR data that either
     *         already points to this event with this link type,
     *         or is new and freshly reserved to be pointing in the desired way
     */
    private static Task<String> getExistingQRDataElseReserve(Event event, int linkType){
        Database db = Database.getInstance();
        return db.qr_codes
                .getExistingQRsForEvent(event, linkType)
                .continueWithTask(task -> {
                    if(!task.isSuccessful()){
                        return Tasks.forException(Database.getTaskException(task));
                    }
                    if(task.getResult().isEmpty()){
                        // if we can't find any existing ones, get a unique ID and set it
                        return db.qr_codes.generateUniqueQrID()
                                .continueWithTask(task1 -> {
                                    // set it, and return the unique ID all the way back up
                                    return db.qr_codes.set(task1.getResult(), event, linkType)
                                            .continueWith(task2 -> {
                                                return task1.getResult();
                                            });
                                });
                    }
                    // else, something exists, we can use it
                    return Tasks.forResult(task.getResult().get(0));
                });
    }
}
