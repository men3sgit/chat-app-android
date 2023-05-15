package com.example.chat_app.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.makeramen.roundedimageview.RoundedImageView;

public final class Render {
    final public static void renderingBitmap(RoundedImageView view, String src) {
        byte[] bytes = Base64.decode(src, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        view.setImageBitmap(bitmap);

    }

}
