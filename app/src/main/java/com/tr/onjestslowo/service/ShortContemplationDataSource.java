package com.tr.onjestslowo.service;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
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

    public String destinationFolder() {
        return android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    }

    public String filePath(String fileName) {
        return new File(destinationFolder(), fileName).toString();
    }

    public void saveFromStream(String fileName, InputStream input) {

        OutputStream output = null;
        try {
            output = new FileOutputStream(filePath(fileName));

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            ; // todo how to re-raise the exception???
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (Exception ignored) {
            }

        }
    }
}
