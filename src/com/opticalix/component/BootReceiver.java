package com.opticalix.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.opticalix.utils.GlobalUtils;

/**
 * Created by Felix on 15/7/29.
 */
@Deprecated
public class BootReceiver extends BroadcastReceiver {

    private String[] mSpContentArr;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("opticalix", "BootReceiver onReceive");
        String spContents = GlobalUtils.restoreFromSp(context);
        mSpContentArr = spContents.split(GlobalUtils.DIVIDER);
        Log.d("opticalix", "BootReceiver spArr:"+mSpContentArr);
        if(mSpContentArr.length > 0){
            Intent i = new Intent(
                    ExampleAppWidgetProvider.ACTION_UPDATE_WIDGET);
            intent.putExtra("content", mSpContentArr[0]);
            Log.d("opticalix", "BootReceiver sendBroadcast with content"+mSpContentArr[0]);
            context.sendBroadcast(i);
        }
    }
}
