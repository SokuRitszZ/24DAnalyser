package com.soku.a24danalysiser.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public static void compressPhoto(Uri uri) throws IOException {
        String path = uri.getPath();
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        fis.read(fileBytes);
        fis.close();
        Bitmap bitmap = Compresser.compress(BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length), 50);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
    }
}
