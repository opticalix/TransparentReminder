package com.opticalix.component;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.opticalix.base.BaseActivity;
import com.opticalix.widget_reminder.R;

/**
 * Created by opticalix@gmail.com on 17/2/7.
 */

public class ColorPicActivity extends BaseActivity implements ColorPickerView.OnColorChangedListener, View.OnClickListener {

    public static final String COLOR = "current_pick_color";
    private ColorPickerView mColorPickerView;
    private ColorPanelView mOldColorPanelView;
    private ColorPanelView mNewColorPanelView;

    private Button mOkButton;
    private Button mCancelButton;

    public static Intent newIntent(Context c) {
        return new Intent(c, ColorPicActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);

        setContentView(R.layout.activity_color_picker);

        init();

    }

    private void init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int initialColor = prefs.getInt(COLOR, 0xFFFFFFFF);

        mColorPickerView = (ColorPickerView) findViewById(R.id.colorpickerview__color_picker_view);
        mOldColorPanelView = (ColorPanelView) findViewById(R.id.colorpickerview__color_panel_old);
        mNewColorPanelView = (ColorPanelView) findViewById(R.id.colorpickerview__color_panel_new);

        mOkButton = (Button) findViewById(R.id.okButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);


        ((LinearLayout) mOldColorPanelView.getParent()).setPadding(
                mColorPickerView.getPaddingLeft(), 0,
                mColorPickerView.getPaddingRight(), 0);


        mColorPickerView.setOnColorChangedListener(this);
        mColorPickerView.setColor(initialColor, true);
        mOldColorPanelView.setColor(initialColor);

        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

    }

    @Override
    public void onColorChanged(int newColor) {
        mNewColorPanelView.setColor(mColorPickerView.getColor());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.okButton:
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
                edit.putInt(COLOR, mColorPickerView.getColor());
                edit.apply();

                finish();
                break;
            case R.id.cancelButton:
                finish();
                break;
        }

    }


}