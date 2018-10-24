package com.tr.tools;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

/**
 * Created by bpl2111 on 2014-06-04.
 */
public class UIHelper {

    public static void showToast(Context context, String text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void showToast(Context context, int resId, int duration) {
        Toast toast = Toast.makeText(context, resId, duration);
        toast.show();
    }

    public static String setThemeColorForHtml(View rootView, int colorResId, String html) {
        String color = "white";
        if (rootView.getContext() != null) {
            // here we retrieve a color, defined in the app them by custom attr. webView_textColor
            Context context = rootView.getContext();
            TypedValue typedValue = new TypedValue();

            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(colorResId, typedValue, true);
            int webviewTextColor = typedValue.data;

            //now convert the int color to hex string
            color = String.format("#%06X", (0xFFFFFF & webviewTextColor));
        }
        String coloredContentHtml = "<font color=\"" +
                color +
                "\">" + html + "</font>";

        return coloredContentHtml;
    }
}
