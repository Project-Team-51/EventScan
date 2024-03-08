package com.example.eventscan.Helpers;

public class QrCodec {
    public static String encodeQRString(String toEncode){
        return "EventScan_event"+toEncode;
    }

    public static boolean verifyQRStringDecodable(String toDecode){
        return toDecode.startsWith("EventScan_event");
    }
    public static String decodeQRString(String toDecode){
        if(!verifyQRStringDecodable(toDecode)){
            throw new RuntimeException("Attempted to decode a non-decodable QR string");
        }
        return toDecode.substring(15); // "EventScan_event" is 15 chars long
    }
}
