package com.example.eventscan.Helpers;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ImageUploader {

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageview){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageview);
    }
}
