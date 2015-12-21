package com.opticalix;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.opticalix.widget_reminder.R;

public class ExampleAppWidgetProvider extends AppWidgetProvider {

	public static final String TAG = "opticalix";
	public static final String ACTION_START_ACTIVITY = "OPTICALIX.ACTION.START.ACTIVITY";
	public static final String ACTION_UPDATE_WIDGET = "OPTICALIX.ACTION.UPDATE_WIDGET";
	private RemoteViews views;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		setListeners(context);
		if (intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
			Log.d(TAG, "onReceive ACTION_UPDATE_WIDGET");
			if (intent.getStringExtra("content") != null) {
				if (views == null) {
					views = new RemoteViews(context.getPackageName(),
							R.layout.example_appwidget);
				}
				Log.d(TAG, "onReceive UPDATE content: "+intent.getStringExtra("content"));
				views.setTextViewText(R.id.tv_widget,
						intent.getStringExtra("content"));
				AppWidgetManager appWidgetManger = AppWidgetManager
						.getInstance(context);
				int[] appIds = appWidgetManger.getAppWidgetIds(new ComponentName(
						context, ExampleAppWidgetProvider.class));
				appWidgetManger.updateAppWidget(appIds, views);
			}
		}
		if (intent.getAction().equals(ACTION_START_ACTIVITY)) {
			Log.d(TAG, "onReceive ACTION_START_ACTIVITY");
			Intent startAct = new Intent(context, MainActivity.class);
			startAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			context.startActivity(startAct);
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

	private void setListeners(Context context) {
		Intent intent = new Intent(context, ExampleAppWidgetProvider.class);
		intent.setAction(ACTION_START_ACTIVITY);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		views = new RemoteViews(context.getPackageName(),
				R.layout.example_appwidget);
		views.setOnClickPendingIntent(R.id.framelayout, pendingIntent);
	}

}
