package com.tr.onjestslowo.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tr.onjestslowo.model.OnJestPreferences;

/**
 * Created by bpl2111 on 2014-06-20.
 */
public class AppPreferences {

    Context mContext;
    OnJestPreferences mPreferences;
    Boolean mIsValid;

    public AppPreferences(Context context) {
        mContext = context;
        mPreferences = new OnJestPreferences();
        mIsValid = false;
    }

    private static AppPreferences mInstance;

    public static AppPreferences getInstance(Context context) {
        if ((mInstance == null) || (mInstance.mContext != context))
            mInstance = new AppPreferences(context);

        return mInstance;
    }

    //<editor-fold first launch related >
    public static String PREF_IS_APP_FIRST = "IsAppFirstLaunch";

    public Boolean isAppFirstLaunch() {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        return prefStore.getBoolean(PREF_IS_APP_FIRST, true);
    }

    public void setAppFirstLaunch(Boolean isFirst) {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = prefStore.edit();
        editor.putBoolean(PREF_IS_APP_FIRST, isFirst);
        editor.apply(); // persist changes
    }
    //</editor-fold>

    public OnJestPreferences get() {
        if (!mIsValid)
            reload();
        return mPreferences;
    }

    public void invalidate() {
        mIsValid = false;
    }

    private void reload() {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        // remark !!!
        // we use EditTexPreference to enter the preferences, so they ALWAYS are String
        // (even if we use inputType=number
        // (that's why we can't use getInt from prefs)
        String key = mContext.getResources().getString(R.string.pref_reading_store_how_long);
        mPreferences.KeepReadingsHowLong = Integer.parseInt(prefStore.getString(key, "30"));
        if ((mPreferences.KeepReadingsHowLong < 7) || (mPreferences.KeepReadingsHowLong > 300))
            mPreferences.KeepReadingsHowLong = 30;

        key = mContext.getResources().getString(R.string.pref_wifi_proxy_enable);
        mPreferences.UseProxy = prefStore.getBoolean(key, false);

        key = mContext.getResources().getString(R.string.pref_wifi_proxy_host);
        mPreferences.ProxyHost = prefStore.getString(key, "");

        key = mContext.getResources().getString(R.string.pref_wifi_proxy_port);
        mPreferences.ProxyPort = Integer.parseInt(prefStore.getString(key, "8080"));

        key = mContext.getResources().getString(R.string.pref_zoom_on_start);
        mPreferences.ShowZoomOnStart = prefStore.getBoolean(key, true);

        key = mContext.getResources().getString(R.string.pref_keep_screen_on);
        mPreferences.KeepScreenOn = prefStore.getBoolean(key, true);

        key = mContext.getResources().getString(R.string.pref_download_short_contemplation);
        mPreferences.DownloadShortContemplation = prefStore.getBoolean(key, true);

        key = mContext.getResources().getString(R.string.pref_short_contemplation_list_always);
        mPreferences.ShortContemplationsFileListAlways = prefStore.getBoolean(key, false);

        key = mContext.getResources().getString(R.string.pref_short_contemplation_download_path);
        mPreferences.ShortContemplationDownloadPath = prefStore.getString(key, "/storage/sdcard/Download");
        mIsValid = true;
    }
}


