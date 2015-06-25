package com.opticalix;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.TextView;

import de.greenrobot.event.EventBus;

/**
 * Created by Felix on 2015/6/15.
 */
public class RecentTextView extends TextView {
    private boolean mInitFinish;
    private int mWidth;
    private int mHeight;
    private int mGridViewHeight;

    public RecentTextView(Context context) {
        super(context);
        mInitFinish = init();
    }

    public RecentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInitFinish = init();
    }

    public RecentTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInitFinish = init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecentTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mInitFinish = init();
    }

    private boolean init() {
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
