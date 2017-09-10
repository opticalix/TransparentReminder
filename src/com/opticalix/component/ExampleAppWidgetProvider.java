package com.opticalix.component;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.opticalix.storage.MyStorage;
import com.opticalix.storage.bean.Note;
import com.opticalix.widget_reminder.R;

import java.util.List;

public class ExampleAppWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "opticalix";
    public static final String ACTION_START_ACTIVITY = "OPTICALIX.ACTION.START.ACTIVITY";
    public static final String ACTION_UPDATE_WIDGET = "OPTICALIX.ACTION.UPDATE_WIDGET";
    private RemoteViews views;

    //TODO 有时会出现onReceive正常 但是remoteView不改变
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        setListeners(context);
        if (intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
            Log.d(TAG, "onReceive ACTION_UPDATE_WIDGET");
            String content = intent.getStringExtra(MainActivity.CONTENT);
            String textColor = intent.getStringExtra(MainActivity.TEXT_COLOR);
            String textSize = intent.getStringExtra(MainActivity.TEXT_SIZE);
            if (views == null) {
                views = new RemoteViews(context.getPackageName(),
                        R.layout.example_appwidget);
            }
            if (content != null && !content.isEmpty()) {
                Log.d(TAG, "onReceive UPDATE content: " + content);
                views.setTextViewText(R.id.tv_widget, content);
            }
            if(textColor != null && !textColor.isEmpty()){
                Log.d(TAG, "onReceive UPDATE textColor: " + textColor);
                views.setTextColor(R.id.tv_widget,  Integer.valueOf(textColor));
            }
            if(textSize != null && !textSize.isEmpty()){
                Log.d(TAG, "onReceive UPDATE textSize: " + textSize);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    views.setTextViewTextSize(R.id.tv_widget, TypedValue.COMPLEX_UNIT_SP, Integer.valueOf(textSize));
                } else {
                    Log.w(TAG, "onReceive UPDATE warn, api < 16");
                }
            }

            //没有change的项是不会变的
            AppWidgetManager appWidgetManger = AppWidgetManager
                    .getInstance(context);
            int[] appIds = appWidgetManger.getAppWidgetIds(new ComponentName(context, ExampleAppWidgetProvider.class));
            appWidgetManger.updateAppWidget(appIds, views);
        } else if (intent.getAction().equals(ACTION_START_ACTIVITY)) {
            Log.d(TAG, "onReceive ACTION_START_ACTIVITY");
            Intent startAct = new Intent(context, MainActivity.class);
            startAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(startAct);
        } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(TAG, "onReceive BOOT_COMPLETED");

            List<Note> notes = MyStorage.getInstance(context).loadAllNotes();
            if (notes != null && notes.size() > 0) {
                //todo check if the last one
                sendUpdateBroadcast(context, notes.get(0));
            }
        }
    }

    private void sendUpdateBroadcast(Context context, Note note) {
        Intent i = new Intent(
                ExampleAppWidgetProvider.ACTION_UPDATE_WIDGET);
        i.putExtra("content", note.getContent());
        Log.d("opticalix", "BOOT_COMPLETED sendBroadcast with content" + note.getContent());
        context.sendBroadcast(i);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Log.d(TAG, "onUpdate: " + appWidgetId);
            setListeners(context);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted");
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled");
        super.onDisabled(context);
    }

    private void setListeners(Context context) {
        Intent intent = new Intent(context, ExampleAppWidgetProvider.class);
        intent.setAction(ACTION_START_ACTIVITY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        views = new RemoteViews(context.getPackageName(),
                R.layout.example_appwidget);
        views.setOnClickPendingIntent(R.id.framelayout, pendingIntent);
    }

}
