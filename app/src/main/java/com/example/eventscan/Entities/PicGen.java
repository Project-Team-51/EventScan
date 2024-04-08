package com.example.eventscan.Entities;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
/**
 * Provides methods for generating, saving, loading, and deleting profile pictures.
 */
public class PicGen {
    public static Bitmap generateProfilePicture(String name, int size) {
        // Generate random color
        int color = generateRandomColor();

        // Get first letter of the name
        String initial = name.substring(0, 1).toUpperCase();

        // Create bitmap with specified size and ARGB_8888 format
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        // Create canvas for drawing
        Canvas canvas = new Canvas(bitmap);

        // Paint for drawing
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        // Draw rectangle with random color
        canvas.drawRect(0, 0, size, size, paint);

        // Paint for text
        paint.setColor(Color.WHITE);
        paint.setTextSize(size * 0.6f);
        paint.setTextAlign(Paint.Align.CENTER);

        // Calculate text bounds
        Rect bounds = new Rect();
        paint.getTextBounds(initial, 0, initial.length(), bounds);

        // Draw initial in the center
        float x = canvas.getWidth() / 2f;
        float y = (canvas.getHeight() + bounds.height()) / 2f;
        canvas.drawText(initial, x, y, paint);

        return bitmap;
    }

    private static int generateRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public static final String PROFILE_PIC_FILENAME = "profile_pic.jpg";

    /**
     * Save the profile picture bitmap to internal storage.
     */
    public static void saveProfilePicture(Context context, Bitmap profileBitmap) {
        try {
            FileOutputStream fos = context.openFileOutput(PROFILE_PIC_FILENAME, Context.MODE_PRIVATE);
            profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Log.e("ProfilePictureManager", "Error saving profile picture", e);
        }
    }

    /**
     * Load the profile picture bitmap from internal storage.
     */
    public static Bitmap loadProfilePicture(Context context) {
        Bitmap profileBitmap = null;
        try {
            FileInputStream fis = context.openFileInput(PROFILE_PIC_FILENAME);
            profileBitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (IOException e) {
            Log.e("ProfilePictureManager", "Error loading profile picture", e);
        }
        return profileBitmap;
    }


    /**
     * Check if the profile picture file exists in internal storage.
     */
    public static boolean isProfilePictureExists(Context context) {
        File file = new File(context.getFilesDir(), PROFILE_PIC_FILENAME);
        return file.exists();
    }

    public static void deleteProfilePicture(Context context) {
        File file = new File(context.getFilesDir(), PROFILE_PIC_FILENAME);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d("ProfilePictureManager", "Profile picture deleted successfully");
            } else {
                Log.e("ProfilePictureManager", "Failed to delete profile picture");
            }
        } else {
            Log.d("ProfilePictureManager", "Profile picture does not exist");
        }
    }
}
