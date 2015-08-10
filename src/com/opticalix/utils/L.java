package com.opticalix.utils;

import android.util.Log;

/**
 * Created by Felix on 2015/8/1.
 */
public class L {
    private L() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static final String TAG = "Opticalix-Reminder";
    private static String prefix = "-=-  ";

    // 下面四个是默认tag的函数
    public static void i(String msg) {
        if (isDebug)
            Log.i(TAG, prefix + msg);
    }

    public static void d(String msg) {
        if (isDebug)
            Log.d(TAG, prefix + msg);
    }

    public static void e(String msg) {
        if (isDebug)
            Log.e(TAG, prefix + msg);
    }

    public static void w(String msg) {
        if (isDebug)
            Log.w(TAG, prefix + msg);
    }

    public static void v(String msg) {
        if (isDebug)
            Log.v(TAG, prefix + msg);
    }

    // 下面是传入自定义tag的函数
    public static void i(Object object, String msg) {
        if (isDebug)
            Log.i(object.getClass().getSimpleName(), prefix + msg);
    }

    public static void d(Object object, String msg) {
        if (isDebug)
            Log.d(object.getClass().getSimpleName(), prefix + msg);
    }

    public static void e(Object object, String msg) {
        if (isDebug)
            Log.e(object.getClass().getSimpleName(), prefix + msg);
    }

    public static void w(Object object, String msg) {
        if (isDebug)
            Log.w(object.getClass().getSimpleName(), prefix + msg);
    }

    public static void v(Object object, String msg) {
        if (isDebug)
            Log.v(object.getClass().getSimpleName(), prefix + msg);
    }
}
