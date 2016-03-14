package com.tr.tools;

import android.util.Log;

import com.tr.onjestslowo.app.BuildConfig;

/**
 * Created by bpl2111 on 2014-06-12.
 */
public class Logger {
    public static void debug(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg);
    }

    public static void info (String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.i(tag, msg);
    }
    public static void warning(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.w(tag, msg);
    }

    public static void error(String tag, String msg, Throwable ex) {
            Log.e(tag, msg, ex);
    }

    public static void error(String tag, String msg) {
        Log.e(tag, msg);
    }
}
