package com.example.eventscan.Helpers;

/*
Helper class that parses the string encoded to and decoded from a QR Code.
 */

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
}
