package com.soku.a24danalysiser.utils;

import android.net.Uri;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FileUtil {
    public static File createImageFile(File storageDir) throws IOException {
        String timeStp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String filename = String.format("JPEG%s", timeStp);
        File image = File.createTempFile(filename, ".jpg", storageDir);
        return image;
    }

    public static byte[] File2Bytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fis.read(bytes);
        fis.close();
        return bytes;
    }

    public static Uri dumpToPhotoUri(byte[] photo, File storageDir) throws IOException {
        String filename = UUID.randomUUID().toString();
        File file = File.createTempFile(filename, ".jpg", storageDir);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(photo);
        String path = file.getPath();
        Uri uri = Uri.parse(path);
        return uri;
    }
}
