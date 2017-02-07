package com.opticalix.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.opticalix.ReminderApplication;

public class DisplayUtil {

    public static float dp2px(int dp) {
        return getDisplayMetrics().density * dp;
    }

    public static DisplayMetrics getDisplayMetrics() {
        return getResources().getDisplayMetrics();
    }

    public static Resources getResources() {
        return ReminderApplication.ref.getResources();
    }

    public static float px2dp(int px) {
        return px / getDisplayMetrics().density;
    }

    public static float[] getScreenSize() {
        float[] size = new float[2];
        DisplayMetrics metrics = getDisplayMetrics();
        size[0] = metrics.widthPixels;
        size[1] = metrics.heightPixels;
        return size;
    }
}
