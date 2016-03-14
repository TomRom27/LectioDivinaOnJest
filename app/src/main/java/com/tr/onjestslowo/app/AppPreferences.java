package com.tr.onjestslowo.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by bpl2111 on 2014-06-20.
 */
public class AppPreferences {

    public static String PREF_IS_APP_FIRST = "IsAppFirstLaunch";
    public static Boolean getIsAppFirstLaunch(Context context) {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean isFirstLaunch = prefStore.getBoolean(PREF_IS_APP_FIRST, true);
        return isFirstLaunch;
    }

    public static void setIsAppFirstLaunch(Context context, Boolean isFirst) {
        SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefStore.edit();
        editor.putBoolean(PREF_IS_APP_FIRST, isFirst);
        editor.apply(); // persist changes
    }
}
