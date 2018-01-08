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

    //<editor-fold app stats info related >
    public static String PREF_IS_STATSINFO_SHOWN = "IsStatsInfoShown";

    public Boolean isStatsInfoShown() {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        return prefStore.getBoolean(PREF_IS_STATSINFO_SHOWN, false);
    }

    public void setStatsInfoShown(Boolean isShown) {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = prefStore.edit();
        editor.putBoolean(PREF_IS_STATSINFO_SHOWN, isShown);
        editor.apply(); // persist changes
    }
    //</editor-fold>

    //<editor-fold last theme related >
    public static String PREF_IS_LAST_THEME_NIGHT= "IsLastThemeNight";

    public Boolean isLastThemeNight() {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        return prefStore.getBoolean(PREF_IS_LAST_THEME_NIGHT, false);
    }

    public void setLastThemeNight(Boolean isShown) {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = prefStore.edit();
        editor.putBoolean(PREF_IS_LAST_THEME_NIGHT, isShown);
        editor.apply(); // persist changes
    }
    //</editor-fold>

    public void setShortContemplationDownloadPath(String path) {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = prefStore.edit();
        String key = mContext.getResources().getString(R.string.pref_short_contemplation_download_path);
        editor.putString(key, path);
        editor.apply(); // persist changes
    }

    public void setStatsInfoEnabled(Boolean isEnabled) {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = prefStore.edit();
        editor.putBoolean(mContext.getResources().getString(R.string.pref_send_app_stats), isEnabled);
        editor.apply(); // persist changes
    }

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
        mPreferences.DownloadShortContemplation = prefStore.getBoolean(key, false);

        key = mContext.getResources().getString(R.string.pref_short_contemplation_list_always);
        mPreferences.ShortContemplationsFileListAlways = prefStore.getBoolean(key, false);

        key = mContext.getResources().getString(R.string.pref_short_contemplation_download_path);
        mPreferences.ShortContemplationDownloadPath = prefStore.getString(key, "");

        key = mContext.getResources().getString(R.string.pref_send_app_stats);
        mPreferences.SendAppStats = prefStore.getBoolean(key, true);
        mIsValid = true;
    }
}


