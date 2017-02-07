package com.opticalix;

import android.app.Application;

/**
 * Created by Felix on 2015/8/1.
 */
public class ReminderApplication extends Application {
    public static Application ref;
    @Override
    public void onCreate() {
        super.onCreate();
//        Thread.setDefaultUncaughtExceptionHandler();
        ref = this;
    }

}
