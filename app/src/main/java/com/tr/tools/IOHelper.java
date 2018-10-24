package com.tr.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Created by bpl2111 on 2016-05-11.
 */
public class IOHelper {

    public static String getExternalPublicDownloadDir(Context context) {

        if (Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState()))
            return android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        else
            return null;
    }

    public static String getSystemDefaultDownloadDir(Context context) {
        String filePath = createDeleteTempAppFile(context);
        if (filePath != "") {
            File file = new File(filePath);
            return file.getParent();
        } else
            return "";
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private static String createDeleteTempAppFile(Context context) {
        String tempFileName = DateHelper.toString("yyyyMMdd_HHmmssSSS'.tmp'", new Date());
        File file = new File(context.getFilesDir(), tempFileName);
        OutputStream o = null;
        try {
            o = new FileOutputStream(file);
            o.write(0);

            return file.getAbsolutePath();
        } catch (Exception ex) {
            return "";

        } finally {
            try {
                if (o != null)
                    o.close();
                if (file.exists())
                    file.delete();
            } catch (Exception ignored) {
            }
        }
    }
}
