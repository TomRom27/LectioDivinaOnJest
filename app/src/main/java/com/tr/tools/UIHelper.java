package com.tr.tools;

import android.content.Context;
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
}
