package com.opticalix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.opticalix.base.BaseActivity;
import com.opticalix.download.DownloadManagerDemo;
import com.opticalix.utils.GlobalUtils;
import com.opticalix.widget_reminder.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends BaseActivity implements OnClickListener {
    private EditText mEditText;
    private Button mOkBtn;
    private Button mClearBtn;
    private Toolbar mToolBar;
    private final int max = 6;
    private String[] mSpContentArr;
    private boolean mPostMsg;
    private int mGridViewHeight;
    private int mGridViewVerticalSpacing;
    private MainActivity mContext;
    private RecyclerView mRecycleView;
    private List<String> mSpContentList;
    private RecycleAdapter mRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemBar();
        setContentView(R.layout.start_act);
        mContext = this;
        mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        mEditText = (EditText) findViewById(R.id.et_content);
        mOkBtn = (Button) findViewById(R.id.btn_ok);
        mClearBtn = (Button) findViewById(R.id.btn_clear);
        mToolBar = (Toolbar) findViewById(R.id.id_toolbar);

        mToolBar.setPadding(mToolBar.getPaddingLeft(), mToolBar.getPaddingTop() + GlobalUtils.getStatusHeight(this), mToolBar.getPaddingRight(), mToolBar.getPaddingBottom());
        mToolBar.requestLayout();

        String spContents = GlobalUtils.resotreFromSp(this);
        mSpContentArr = spContents.split(GlobalUtils.DIVIDER);
        mOkBtn.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);

        //ToolBar
        setSupportActionBar(mToolBar);
    }

    private void initSystemBar() {
        //利用SystemBarTintManager 实现4.4+的translucent效果
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getStatusBarColor());
            tintManager.setStatusBarTintEnabled(true);
        }else{
            //FIXME padding
        }
    }

    private int getStatusBarColor() {
        TypedValue typedValue = new TypedValue();
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

                //FIXME
                startActivity(new Intent(this, DownloadManagerDemo.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !mPostMsg) {
            mPostMsg = true;

            //init RecycleView
            initRecycleView();
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
                    addToSp(trimContent);
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
                mRecycleAdapter = new RecycleAdapter(Arrays.asList(mSpContentArr));
                GlobalUtils.saveToSp(MainActivity.this, "");
            }
        });
    }

    private void addToSp(String trimContent) {
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

    public void removeFromSp(String trimContent) {
        String spContents = GlobalUtils.resotreFromSp(this);
        String[] spContentArr = spContents.split(GlobalUtils.DIVIDER);
        List<String> spContentList = Arrays.asList(spContentArr);
        if (spContentList.contains(trimContent)) {
            LinkedList<String> listFromSp = new LinkedList<>();
            listFromSp.addAll(spContentList);
            listFromSp.remove(trimContent);
            GlobalUtils.saveToSp(this, parseListToString(listFromSp));
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
    public void onBackPressed() {
        popSoftKeyboard(this, mEditText, false);
        super.onBackPressed();
    }

    public void initRecycleView() {
        mSpContentList = new ArrayList<>();
        mSpContentList.addAll(Arrays.asList(mSpContentArr));

        mRecycleView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(staggeredGridLayoutManager);
        mRecycleAdapter = new RecycleAdapter(mSpContentList);
        mRecycleAdapter.setOnRecycleItemClickListener(new OnRecycleItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mEditText.setText(mSpContentList.get(position));
            }

            @Override
            public void onItemLongClick(int position) {
                mSpContentList.remove(position);
                removeFromSp(mSpContentList.get(position));
                mRecycleAdapter.notifyItemRemoved(position);
            }
        });
        mRecycleView.setAdapter(mRecycleAdapter);
        mRecycleView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.list_divider)));
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        RecentTextView mRecentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mRecentTextView = (RecentTextView) itemView.findViewById(R.id.tv_item);
            if(mRecentTextView.getLineCount() >= 1){
                mRecentTextView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7.0f,  MainActivity.this.getResources().getDisplayMetrics()), 1.0f);
            }
        }
    }

    class RecycleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<String> mData;

        public RecycleAdapter(List<String> data) {
            mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_grid, viewGroup, false);
            if(mData == null || mData.size() == 0 || (mData.size() == 1 && mData.get(0).equals(""))){
                view.setBackgroundColor(0x00ffffff);
                view.setVisibility(View.GONE);
            }
            ViewHolder viewHolder = new ViewHolder(view);

            view.setMinimumHeight(200);
            //listener
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnRecycleItemClickListener != null) {
                        mOnRecycleItemClickListener.onItemClick(i);
                    }
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnRecycleItemClickListener != null) {
                        mOnRecycleItemClickListener.onItemLongClick(i);
                    }
                    return false;
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.mRecentTextView.setText(mData.get(i));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        private OnRecycleItemClickListener mOnRecycleItemClickListener;

        public OnRecycleItemClickListener getOnRecycleItemClickListener() {
            return mOnRecycleItemClickListener;
        }

        public void setOnRecycleItemClickListener(OnRecycleItemClickListener onRecycleItemClickListener) {
            mOnRecycleItemClickListener = onRecycleItemClickListener;
        }
    }

    interface OnRecycleItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration {
        public DividerItemDecoration(Drawable drawable) {
            mDivider = drawable;
        }

        private Drawable mDivider;

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            drawHorizontal(c, parent);
            drawVertical(c, parent);
        }

        /**
         * 得到列数
         *
         * @param parent
         * @return
         */
        private int getSpanCount(RecyclerView parent) {
            int spanCount = -1;
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {

                spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                spanCount = ((StaggeredGridLayoutManager) layoutManager)
                        .getSpanCount();
            }
            return spanCount;
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin
                        + mDivider.getIntrinsicWidth();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getTop() - params.topMargin;
                final int bottom = child.getBottom() + params.bottomMargin;
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicWidth();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition,
                                   RecyclerView parent) {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(),
                    mDivider.getIntrinsicHeight());
        }
    }
}
