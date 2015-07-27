package com.opticalix.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

public class GlobalUtils {
    public static final String SP_NAME = "WidgetDemo";
    public static final String KEY = "key";
    public static final String DIVIDER = "%%";

    public static void saveToSp(Context cxt, String value) {
        SharedPreferences sp = cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, value).apply();
    }

    public static void addToSp(Context cxt, String value) {
        SharedPreferences sp = cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, value + DIVIDER + sp.getString(KEY, "")).apply();
    }

    public static String restoreFromSp(Context cxt) {
        SharedPreferences sp = cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY, "");
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public static class HeightUtil<T extends ViewGroup.LayoutParams> {
        /**
         * 重新定制宽高 layoutParams是为了以防layoutParams为空
         * @param view
         * @param width
         * @param height
         * @param layoutParams
         */
        public void resizeHeight(View view, int width, int height, T layoutParams) {
            T temp = (T) view.getLayoutParams();
            if (temp == null) {
                temp = layoutParams;
            }
            temp.width = width;
            temp.height = height;
            view.setLayoutParams(temp);
            view.requestLayout();
        }

        public int calcItemHeight(int total, int space, int rowCount){
            int hei =  (total - space * (rowCount - 1)) / rowCount;
            Log.d("opticalix", "calcItemHeight height:" + hei);
            return hei;
        }
    }

}
