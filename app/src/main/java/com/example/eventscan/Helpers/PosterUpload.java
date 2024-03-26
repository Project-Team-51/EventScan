package com.example.eventscan.Helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

public class PosterUpload {

    public static Bitmap loadBitmapFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    public static void saveBitmapToFile(Context context, Bitmap bitmap, Uri uri) {
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
