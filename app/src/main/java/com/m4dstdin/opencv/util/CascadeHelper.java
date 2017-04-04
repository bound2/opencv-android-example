package com.m4dstdin.opencv.util;

import android.content.Context;

import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by thor on 04/04/2017.
 */
public final class CascadeHelper {

    private CascadeHelper() {
    }

    public static CascadeClassifier getCascade(Context context, String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        // Copy the resource into a temp file so OpenCV can load it
        File cascadeDir = context.getDir("cascade", Context.MODE_APPEND);
        File cascadeFile = new File(cascadeDir, fileName);
        FileOutputStream os = new FileOutputStream(cascadeFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();

        // Load the cascade classifier
        CascadeClassifier classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
        classifier.load(cascadeFile.getAbsolutePath());
        return classifier;
    }

}
