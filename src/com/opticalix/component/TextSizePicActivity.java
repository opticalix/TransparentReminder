package com.opticalix.component;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.opticalix.base.BaseActivity;
import com.opticalix.widget_reminder.R;

/**
 * Created by opticalix@gmail.com on 17/2/7.
 */

public class TextSizePicActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final String TEXT_SIZE = "current_pick_text_size";
    public static final int DEFAULT_TEXT_SIZE = 14;//sp
    private static final int[] TEXT_SIZE_CANDIDATES = new int[]{10, 12, 14, 18, 24, 32};
    private static final String TAG = TextSizePicActivity.class.getSimpleName();

    private Button mOkButton;
    private Button mCancelButton;
    private TextView mTvDemo;
    private SeekBar mSbTextSize;
    private int mSp;

    public static Intent newIntent(Context c) {
        return new Intent(c, TextSizePicActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text_size_picker);

        init();

    }

    private void init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mOkButton = (Button) findViewById(R.id.okButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mTvDemo = (TextView) findViewById(R.id.tv_demo);
        mSbTextSize = (SeekBar) findViewById(R.id.sb_text_size);

        //restore
        mSp = prefs.getInt(TEXT_SIZE, DEFAULT_TEXT_SIZE);
        mTvDemo.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSp);
        mSbTextSize.setProgress(restoreProgress(mSp, mSbTextSize.getProgress()));

        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mSbTextSize.setOnSeekBarChangeListener(this);
    }

    private int restoreProgress(int sp, int def) {
        for (int i=0; i<TEXT_SIZE_CANDIDATES.length; i++) {
            if (TEXT_SIZE_CANDIDATES[i] == sp) {
                return i;
            }
        }
        return def;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.okButton:
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
                edit.putInt(TEXT_SIZE, mSp);
                edit.apply();

                setResult(MainActivity.RESULT_TXT_SIZE_CODE);
                finish();
                break;
            case R.id.cancelButton:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int max = seekBar.getMax();
        mSp = TEXT_SIZE_CANDIDATES[i];
        mTvDemo.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSp);
        Log.d(TAG, "onProgressChanged, i="+i + ", max="+max + ", mSp="+mSp);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}