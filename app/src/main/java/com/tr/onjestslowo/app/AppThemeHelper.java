package com.tr.onjestslowo.app;

import android.os.Build;

/**
 * Created by bpl2111 on 2016-04-07.
 */
public class AppThemeHelper {

    public static int GetDayThemeId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)  // < 5.0
            return R.style.AppTheme_Classics;
        else // >= 5.0
            return R.style.AppThemeLight;

    }

    public static int GetNightThemeId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)  // < 5.0
            return R.style.AppTheme_Classics_Dark;
        else // >= 5.0
            return R.style.AppThemeDark;

    }
}
