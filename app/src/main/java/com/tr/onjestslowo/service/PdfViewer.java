package com.tr.onjestslowo.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;

/**
 * Created by bpl2111 on 2016-04-29.
 */
public class PdfViewer {
    private static final String MIME_TYPE_PDF = "application/pdf";

    Activity mActivity;

    public PdfViewer(Activity activity) {
        mActivity = activity;
    }

    private static PdfViewer mInstance;

    public static PdfViewer getInstance(Activity activity) {
        if ((mInstance == null) || (mInstance.mActivity != activity))
            mInstance = new PdfViewer(activity);

        return mInstance;
    }

    public void showFileIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, MIME_TYPE_PDF);

            mActivity.startActivity(intent);
        }
    }

    public boolean canDisplayPdf() {

        PackageManager packageManager = mActivity.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType(MIME_TYPE_PDF);
        return packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

}
