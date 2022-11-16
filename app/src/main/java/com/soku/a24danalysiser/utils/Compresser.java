package com.soku.a24danalysiser.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Compresser {
    public static Bitmap compress(Bitmap image, int imageSize) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        int options = 100;
        while (options >= 0 && os.toByteArray().length / 1024 > imageSize) {
            os.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, os);
            options -= 1;
        }
        Log.d("TAGAAA", "OK");
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, null);

        return bitmap;
    }
}
