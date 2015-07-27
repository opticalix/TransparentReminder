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
import android.util.Log;
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
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.opticalix.base.BaseActivity;
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
    private int mRecycleViewHeight;
    private int mSpacing;
    private MainActivity mContext;
    private RecyclerView mRecycleView;
    private List<String> mSpContentList;
    private RecycleAdapter mRecycleAdapter;
    private boolean mAllowDelete;
    private int mLastItemPos = -1;

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

        String spContents = GlobalUtils.restoreFromSp(this);
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
        } else {
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
                return true;
            case R.id.btn_menu_delete:
                Log.d("opticalix", "-=-= delete item isChecked: " + item.isChecked());
//                item.setChecked(!item.isChecked());
//                int[] checkState = new int[] {android.R.attr.state_checked};
//                int[] idleState = new int[] {};
//                item.getIcon().setState(!item.isChecked() ? idleState : checkState);
                mAllowDelete = !mAllowDelete;
                item.setIcon(mAllowDelete ? R.drawable.icon_white_delete_checked : R.drawable.icon_white_delete);
                mRecycleAdapter.notifyDataSetChanged();
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
            mRecycleViewHeight = mRecycleView.getHeight();
            mSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, MainActivity.this.getResources().getDisplayMetrics());
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
                    //TODO 询问是否替换
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
                mRecycleAdapter = new RecycleAdapter(Arrays.asList(mSpContentArr), mRecycleViewHeight, mSpacing);
                GlobalUtils.saveToSp(MainActivity.this, "");
//                mRecycleAdapter.notifyItemMoved(0, max - 1);
                mRecycleView.setAdapter(mRecycleAdapter);
            }
        });
    }

    private void addToSp(String trimContent) {
        String spContents = GlobalUtils.restoreFromSp(this);
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
        String spContents = GlobalUtils.restoreFromSp(this);
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
        if(mAllowDelete){
            mAllowDelete = !mAllowDelete;
            MenuItem item = mToolBar.getMenu().getItem(0);
            if(item.getItemId() != R.id.btn_menu_delete){
                throw new RuntimeException("get the wrong menu btn!");
            }
            item.setIcon(mAllowDelete ? R.drawable.icon_white_delete_checked : R.drawable.icon_white_delete);
            mRecycleAdapter.notifyDataSetChanged();
            return;
        }
        super.onBackPressed();
    }

    public void initRecycleView() {
        mSpContentList = new ArrayList<>();
        mSpContentList.addAll(Arrays.asList(mSpContentArr));

        mRecycleView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(staggeredGridLayoutManager);
        mRecycleAdapter = new RecycleAdapter(mSpContentList, mRecycleViewHeight, mSpacing);
        mRecycleAdapter.setOnRecycleItemClickListener(new OnRecycleItemClickListener() {
            @Override
            public void onItemClick(View v) {
                int position = (int) v.getTag();
                if(v.getId() == R.id.tv_item){
                    mLastItemPos = position;
                    mEditText.setText(mSpContentList.get(position));
                    mEditText.setSelection(mEditText.getText().toString().length());
                }else if(v.getId() == R.id.iv_delete_layer){
                    if(mAllowDelete){
                        removeFromSp(mSpContentList.get(position));
                        mSpContentList.remove(position);
                        mRecycleAdapter.notifyItemRemoved(position);
//                        mRecycleAdapter.notifyDataSetChanged();
                        mRecycleAdapter.notifyItemRangeChanged(0 ,max-1);
                    }
                }
            }

            @Override
            public void onItemLongClick(View v) {

            }
        });
        mRecycleView.setAdapter(mRecycleAdapter);
        mRecycleView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.list_divider)));
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        RecentTextView mRecentTextView;
        ImageView mDeleteLayer;

        public ViewHolder(View itemView) {
            super(itemView);
            mRecentTextView = (RecentTextView) itemView.findViewById(R.id.tv_item);
            mDeleteLayer = (ImageView) itemView.findViewById(R.id.iv_delete_layer);
            if(mRecentTextView == null || mDeleteLayer == null)
                throw new RuntimeException("viewHolder is empty!");
            if (mRecentTextView.getLineCount() >= 1) {
                mRecentTextView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, MainActivity.this.getResources().getDisplayMetrics()), 1.0f);
            }
        }
    }

    class RecycleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<String> mData;
        private GlobalUtils.HeightUtil<GridLayoutManager.LayoutParams> mHeightUtil;
        private int mRecycleViewHeight;
        private int mSpace;

        public RecycleAdapter(List<String> data, int recycleViewHeight, int space) {
            mData = data;
            this.mRecycleViewHeight = recycleViewHeight;
            this.mSpace = space;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_grid, viewGroup, false);
            if (mData == null || mData.size() == 0 || (mData.size() == 1 && mData.get(0).equals(""))) {
                view.setBackgroundColor(0x00ffffff);
                view.setVisibility(View.GONE);
            }
            ViewHolder viewHolder = new ViewHolder(view);

            //resize height
            mHeightUtil = new GlobalUtils.HeightUtil<>();
            int itemHeight = mHeightUtil.calcItemHeight(mRecycleViewHeight, mSpace, 3);
            mHeightUtil.resizeHeight(view, -1, itemHeight, new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            //listener
            Log.d("opticalix", "-=-= onCreateViewHolder");

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int i) {
            Log.d("opticalix", "-=-= onBindViewHolder");
            viewHolder.mRecentTextView.setTag(i);
            viewHolder.mDeleteLayer.setTag(i);
            viewHolder.mRecentTextView.setText(mData.get(i));
            viewHolder.mDeleteLayer.setVisibility(mAllowDelete ? View.VISIBLE : View.GONE);

            viewHolder.mRecentTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnRecycleItemClickListener != null) {
                        mOnRecycleItemClickListener.onItemClick(viewHolder.mRecentTextView);
                    }
                }
            });
            viewHolder.mDeleteLayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnRecycleItemClickListener != null) {
                        mOnRecycleItemClickListener.onItemClick(viewHolder.mDeleteLayer);
                    }
                }
            });
            viewHolder.mRecentTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnRecycleItemClickListener != null) {
                        mOnRecycleItemClickListener.onItemLongClick(viewHolder.mRecentTextView);
                    }
                    return false;
                }
            });

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
        void onItemClick(View v);

        void onItemLongClick(View v);
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
