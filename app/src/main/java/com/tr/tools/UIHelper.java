package com.tr.tools;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String setThemeColorForHtml(View rootView, int colorResId, String htmlContent) {
        final String htmlTag  = "html";

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
        String colorStyle = String.format("<style> body {color: %s;}</style>", color);

        return insertAfterTag(htmlContent, "html", colorStyle);
    }

    public static String insertAfterTag(String content, String htmlTag, String stringToAdd) {
        String tagStart = "<"+htmlTag+">";
        String tagEnd = "</"+htmlTag+">";

        Pattern pattern = Pattern.compile(tagStart, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(content);

        String modifiedString = "";
        if (matcher.find()) {
            modifiedString = matcher.replaceFirst(matcher.group() + stringToAdd);

        } else {
            modifiedString = tagStart + stringToAdd + content + tagEnd;
        }

        return modifiedString;
    }
}