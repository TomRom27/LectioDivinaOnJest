package com.tr.onjestslowo.service;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.lang.ref.WeakReference;

public class PdfViewer {
    private static final String MIME_TYPE_PDF = "application/pdf";

    private final WeakReference<Activity> mActivityRef;

    public PdfViewer(Activity activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    private static PdfViewer mInstance;

    public static PdfViewer getInstance(Activity activity) {
        if ((mInstance == null) || (mInstance.mActivityRef.get() != activity))
            mInstance = new PdfViewer(activity);

        return mInstance;
    }

    public void showFileIfExists(String filePath) {
        Activity activity = mActivityRef.get();
        if (activity == null) return;

        File file = new File(filePath);
        if (file.exists()) {
            Uri path = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, MIME_TYPE_PDF);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            activity.startActivity(intent);
        }
    }


    public boolean canDisplayPdf() {
        Activity activity = mActivityRef.get();
        if (activity == null) return false;

        final PackageManager packageManager = activity.getPackageManager();

        final Intent intent = new Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setDataAndType(Uri.parse("content://com.example.dummy/document.pdf"), MIME_TYPE_PDF);

        final int flagsLegacy = PackageManager.MATCH_DEFAULT_ONLY;
        ResolveInfo ri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ri = packageManager.resolveActivity(intent,
                    PackageManager.ResolveInfoFlags.of(flagsLegacy));
        } else {
            ri = packageManager.resolveActivity(intent, flagsLegacy);
        }
        return ri != null;
    }
}