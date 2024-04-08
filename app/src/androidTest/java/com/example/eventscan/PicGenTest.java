package com.example.eventscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

import com.example.eventscan.Entities.PicGen;

/**
 * Testing class for Picture Generation
 */
public class PicGenTest {

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @After
    public void tearDown() {
        // Delete profile picture file after each test
        File file = new File(context.getFilesDir(), PicGen.PROFILE_PIC_FILENAME);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void generateProfilePicture() {
        String name = "John";
        int size = 200;
        Bitmap bitmap = PicGen.generateProfilePicture(name, size);

        assertNotNull(bitmap);
        assertEquals(size, bitmap.getWidth());
        assertEquals(size, bitmap.getHeight());
    }
}
