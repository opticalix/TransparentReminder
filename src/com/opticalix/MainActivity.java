package com.opticalix;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.opticalix.widget_reminder.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
    private EditText mEditText;
    private Button mOkBtn;
    private Button mClearBtn;
    private GridView mGridView;
    private Toolbar mToolBar;
    private final int max = 6;
    private RecentGridAdapter arrayAdapter;
    private String[] mSpContentArr;
    private boolean mPostMsg;
    private int mGridViewHeight;
    private int mGridViewVerticalSpacing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemBar();
        setContentView(R.layout.start_act);
        EventBus.getDefault().register(this);

        mEditText = (EditText) findViewById(R.id.et_content);
        mOkBtn = (Button) findViewById(R.id.btn_ok);
        mClearBtn = (Button) findViewById(R.id.btn_clear);
        mGridView = (GridView) findViewById(R.id.grid_recent);
        mToolBar = (Toolbar) findViewById(R.id.id_toolbar);
        mToolBar.setPadding(mToolBar.getPaddingLeft(), mToolBar.getPaddingTop() + GlobalUtils.getStatusHeight(this), mToolBar.getPaddingRight(), mToolBar.getPaddingBottom());
        mToolBar.requestLayout();

        String spContents = GlobalUtils.resotreFromSp(this);
        mSpContentArr = spContents.split(GlobalUtils.DIVIDER);
        mGridView.setOnItemClickListener(this);
        mOkBtn.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);

        //ToolBar
        setSupportActionBar(mToolBar);
    }

    private void initSystemBar() {
        //利用SystemBarTintManager 实现4.4+的translucent效果
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getStatusBarColor());
            tintManager.setStatusBarTintEnabled(true);
        }
    }

    private int getStatusBarColor() {
        TypedValue typedValue = new  TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //create menu from main_menu.xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.btn_menu_clear:
                clearRecent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !mPostMsg) {
            mGridViewHeight = mGridView.getHeight();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mGridViewVerticalSpacing = mGridView.getVerticalSpacing();
            } else {
                mGridViewVerticalSpacing = 20;//px
            }
            EventBus.getDefault().post(BaseMessage.g().getMsg(BaseMessage.OnViewMeasured.class).put("height", mGridViewHeight).put("verticalSpacing", mGridViewVerticalSpacing));
            mPostMsg = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                String trimContent = mEditText.getText().toString().trim();
                if (TextUtils.isEmpty(trimContent)) {
                    int res[] = {R.layout.tip_empty_dialog, R.id.tv_title, R.id.tv_tip};
                    TipDialogFragment dialogFragment = TipDialogFragment.newInstance(res);
                    dialogFragment.show(MainActivity.this.getSupportFragmentManager(), "empty_tip");
                    dialogFragment.setOnOkBtnClickListener(new TipDialogFragment.OnOkBtnClickListener() {
                        @Override
                        public void onOkClick() {
                            Intent intent = new Intent(
                                    ExampleAppWidgetProvider.ACTION_UPDATE_WIDGET);
                            intent.putExtra("content", "");
                            MainActivity.this.sendBroadcast(intent);
                            finish();
                        }
                    });
                    return;
                } else {
                    saveToSp(trimContent);
                }
                Intent intent = new Intent(
                        ExampleAppWidgetProvider.ACTION_UPDATE_WIDGET);
                intent.putExtra("content", trimContent);
                this.sendBroadcast(intent);

                finish();
                break;
            case R.id.btn_clear:
                mEditText.setText("");
                break;
        }
    }

    private void clearRecent() {
        int res[] = {R.layout.tip_clear_dialog, R.id.tv_title, R.id.tv_tip};
        final TipDialogFragment dialogFragment = TipDialogFragment.newInstance(res);
        dialogFragment.show(MainActivity.this.getSupportFragmentManager(), "clear_tip");
        dialogFragment.setOnOkBtnClickListener(new TipDialogFragment.OnOkBtnClickListener() {
            @Override
            public void onOkClick() {
                mSpContentArr = new String[]{""};
                arrayAdapter = new RecentGridAdapter(MainActivity.this, R.layout.item_grid, mSpContentArr, mGridViewHeight, mGridViewVerticalSpacing);
                mGridView.setAdapter(arrayAdapter);
                GlobalUtils.saveToSp(MainActivity.this, "");
            }
        });
    }

    private void saveToSp(String trimContent) {
        String spContents = GlobalUtils.resotreFromSp(this);
        String[] spContentArr = spContents.split(GlobalUtils.DIVIDER);
        List<String> spContentList = Arrays.asList(spContentArr);
        if (spContentList.contains(trimContent)) {
            //元素重复了
            LinkedList<String> listFromSp = new LinkedList<>();
            listFromSp.addAll(spContentList);
            listFromSp.remove(trimContent);
            listFromSp.addFirst(trimContent);
            GlobalUtils.saveToSp(this, parseListToString(listFromSp));
        } else {
            //没有重复则保存
            if (spContentArr.length >= max) {
                //到达最大个数
                LinkedList<String> listFromSp = new LinkedList<>();
                listFromSp.addAll(spContentList);
                listFromSp.removeLast();
                listFromSp.addFirst(trimContent);
                GlobalUtils.saveToSp(this, parseListToString(listFromSp));
            } else {
                //没有到达最大个数
                GlobalUtils.addToSp(this, trimContent);
            }
        }
    }

    private String parseListToString(List<String> spList) {
        String lastContent = "";
        if (spList != null && spList.size() > 0) {
            for (int i = 0; i < spList.size() - 1; i++) {
                lastContent = lastContent + spList.get(i) + GlobalUtils.DIVIDER;
            }
            lastContent += spList.get(spList.size() - 1);
        }
        return lastContent;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void popSoftKeyboard(Context ctx, View view, boolean wantPop) {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (wantPop) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mEditText.setText((CharSequence) arrayAdapter.getItem(position));
    }

    public void onEventMainThread(BaseMessage.OnViewMeasured msg) {
        arrayAdapter = new RecentGridAdapter(this, R.layout.item_grid, mSpContentArr, mGridViewHeight, mGridViewVerticalSpacing);
        mGridView.setAdapter(arrayAdapter);
    }

    @Override
    public void onBackPressed() {
        popSoftKeyboard(this, mEditText, false);
        super.onBackPressed();
    }
}
