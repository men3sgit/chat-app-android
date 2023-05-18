package com.example.chat_app.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;

public final class Render {
    final public static void renderingBitmap(RoundedImageView view, String src) {
        byte[] bytes = Base64.decode(src, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        view.setImageBitmap(bitmap);
    }
    final public static Bitmap renderingBitmap(String src) {
        byte[] decodedString = Base64.decode(src, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
    final public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}
