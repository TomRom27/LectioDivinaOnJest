package com.tr.onjestslowo.service;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by bpl2111 on 2016-04-19.
 */
public class ShortContemplationDataSource {
    private Context context;

    public ShortContemplationDataSource(Context context) {
        this.context = context;
    }

    public String defaultDestinationFolder() {
        return android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    }

    public void saveFromStream(String fileName, InputStream input) throws IOException {
        String destinationPath = defaultDestinationFolder();

        saveFromStream(fileName, destinationPath, input);
    }

    public void saveFromStream(String fileName, String destinationPath, InputStream input) throws IOException {

        OutputStream output = null;
        try {
            output = new FileOutputStream(combine(destinationPath, fileName));

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (Exception ignored) {
            }

        }
    }

    private String combine(String path, String fileName) {
        return new File(path, fileName).toString();
    }
}
