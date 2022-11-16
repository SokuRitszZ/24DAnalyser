package com.soku.a24danalysiser.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
    public static File createImageFile(File storageDir) throws IOException {
        String timeStp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String filename = String.format("JPEG%s", timeStp);
        File image = File.createTempFile(filename, ".jpg", storageDir);
        return image;
    }
}
