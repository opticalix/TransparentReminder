package com.opticalix.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class GlobalUtils {
    public static final String SP_NAME = "WidgetDemo";
    public static final String KEY = "key";
    public static final String DIVIDER = "%%";

    public static void saveToSp(Context cxt, String value) {
        SharedPreferences sp = cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, value).commit();
    }

    public static void addToSp(Context cxt, String value) {
        SharedPreferences sp = cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, value + DIVIDER + sp.getString(KEY, "")).commit();
    }

    public static String resotreFromSp(Context cxt) {
        SharedPreferences sp = cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY, "");
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context)
    {

        int statusHeight = -1;
        try
        {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return statusHeight;
    }


}
