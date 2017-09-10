package com.opticalix.component;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.opticalix.storage.MyStorage;
import com.opticalix.storage.bean.Note;
import com.opticalix.ui.RecentTextView;
import com.opticalix.base.BaseActivity;
import com.opticalix.utils.DisplayUtil;
import com.opticalix.utils.GlobalUtils;
import com.opticalix.utils.ToastUtils;
import com.opticalix.widget_reminder.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends BaseActivity implements OnClickListener {
    public static final int REQUEST_CODE = 0x01;
    public static final int RESULT_TXT_COLOR_CODE = 0x02;
    public static final int RESULT_TXT_SIZE_CODE = 0x03;
    public static final String TEXT_COLOR = "text_color";
    public static final String CONTENT = "content";
    public static final String TEXT_SIZE = "text_size";
    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText mEditText;
    private Button mOkBtn;
    private Button mClearBtn;
    private Toolbar mToolBar;
    private final int max = 6;
    private boolean mPostMsg;
    private int mRecycleViewHeight;
    private int mSpacing;
    private MainActivity mContext;
    private RecyclerView mRecycleView;
    private RecycleAdapter mRecycleAdapter;
    private boolean mAllowDelete;

    private Note mLastItem;
    private Menu mMenu;
    private RelativeLayout mContainer;
    private View mEditNoteView;
    private PopupWindow mPopupWindow;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkFirstIn();
        initSystemBar();
        setContentView(R.layout.start_act);
        mContext = this;
        mContainer = (RelativeLayout) findViewById(R.id.root_container);
        mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        mEditText = (EditText) findViewById(R.id.et_content);
        mOkBtn = (Button) findViewById(R.id.btn_ok);
        mClearBtn = (Button) findViewById(R.id.btn_clear);
        mToolBar = (Toolbar) findViewById(R.id.id_toolbar);
        mFab = (FloatingActionButton) findViewById(R.id.fbtn_add);

        mToolBar.setPadding(mToolBar.getPaddingLeft(), mToolBar.getPaddingTop() + GlobalUtils.getStatusHeight(this), mToolBar.getPaddingRight(), mToolBar.getPaddingBottom());
        mToolBar.requestLayout();

        //listeners
        mOkBtn.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);
        mFab.setOnClickListener(this);
        //ToolBar
        setSupportActionBar(mToolBar);
    }

    private void checkFirstIn() {
        boolean firstInFromSp = GlobalUtils.getFirstInFromSp(this);
        if(firstInFromSp){
            int res[] = {R.layout.tip_first_in_dialog, R.id.tv_title, R.id.tv_tip};
            AcknowledgeDialogFragment dialogFragment = AcknowledgeDialogFragment.newInstance(res);
            dialogFragment.show(MainActivity.this.getSupportFragmentManager(), "first_in_tip");
            dialogFragment.setOnOkBtnClickListener(
                    new AcknowledgeDialogFragment.OnOkBtnClickListener() {
                        @Override
                        public void onOkClick() {
                            //can not add widget to home screen programmatically
//                            Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
//                            pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 1);
//                            startActivityForResult(pickIntent, 1);
                            GlobalUtils.setFirstInToSp(MainActivity.this, false);
                        }
                    }
            );
        }
    }

    private void initSystemBar() {
        //利用SystemBarTintManager 实现4.4+的translucent效果
        //HuaWei top padding too big..
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

        this.mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.btn_menu_about:
                Intent intent = new Intent();
                intent.setClass(this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.btn_menu_clear:
                clearRecent();
                return true;
            case R.id.btn_change_text_color:
                startActivityForResult(ColorPicActivity.newIntent(this), REQUEST_CODE);
                return true;
            case R.id.btn_menu_delete:
                Log.d("opticalix", "-=-= delete item isChecked: " + item.isChecked());
//                item.setChecked(!item.isChecked());
//                int[] checkState = new int[] {android.R.attr.state_checked};
//                int[] idleState = new int[] {};
//                item.getIcon().setState(!item.isChecked() ? idleState : checkState);
                switchMode(item);
                return true;
            case R.id.btn_menu_text_size:
                startActivityForResult(TextSizePicActivity.newIntent(this), REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void switchMode(MenuItem item) {
        mAllowDelete = !mAllowDelete;
        item.setIcon(mAllowDelete ? R.drawable.icon_white_delete_checked : R.drawable.icon_white_delete);
        mRecycleAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        mLastItem = null;
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !mPostMsg) {
            mRecycleViewHeight = mRecycleView.getHeight() - getNavBarHeight(this);
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
                break;
            case R.id.btn_clear:
                break;
            case R.id.fbtn_add:
                showEditNodePopupWindow(v.getContext(), null);
                break;
        }
    }

    private void updateWidget(String tag, String value, boolean finish) {
        Intent intent = new Intent(
                ExampleAppWidgetProvider.ACTION_UPDATE_WIDGET);
        intent.putExtra(tag, value);
        MainActivity.this.sendBroadcast(intent);
        if(finish)
            finish();
    }


    private void replaceClickItem(final String content) {
        int res[] = {R.layout.tip_replace_dialog, R.id.tv_title, R.id.tv_tip};
        TipDialogFragment dialogFragment = TipDialogFragment.newInstance(res);
        dialogFragment.show(MainActivity.this.getSupportFragmentManager(), "replace_tip");
        dialogFragment.setOnOkBtnClickListener(new TipDialogFragment.OnOkBtnClickListener() {
            @Override
            public void onOkClick() {
                if (!content.equals(mLastItem.getContent())) {
                    addNoteByDB(content);
                    MyStorage.getInstance(MainActivity.this).removeNote(mLastItem);
                } else {
                    mLastItem.setUpdate_date(new Date(System.currentTimeMillis()));
                    MyStorage.getInstance(MainActivity.this).updateNote(mLastItem);
                }
                updateWidget(CONTENT, content, true);
            }
        });
        dialogFragment.setOnCancelBtnClickListener(new TipDialogFragment.OnCancelBtnClickListener() {
            @Override
            public void onCancelClick() {
                addNoteByDB(content);
                updateWidget(CONTENT, content, true);
            }
        });
    }

    private void clearRecent() {
        int res[] = {R.layout.tip_clear_dialog, R.id.tv_title, R.id.tv_tip};
        final TipDialogFragment dialogFragment = TipDialogFragment.newInstance(res);
        dialogFragment.show(MainActivity.this.getSupportFragmentManager(), "clear_tip");
        dialogFragment.setOnOkBtnClickListener(new TipDialogFragment.OnOkBtnClickListener() {
            @Override
            public void onOkClick() {
                clearNote();
            }
        });
    }

    private void clearNote() {
        int size = mRecycleAdapter.mData.size();
        mRecycleAdapter.mData.clear();
        mRecycleAdapter.notifyItemRangeRemoved(0, size);

        MyStorage.getInstance(this).removeAllNote();
    }

    private List<String> convertNotesToStrings(List<Note> notes){
        ArrayList<String> strings = new ArrayList<>();
        if(notes == null) return strings;
        for (Note note : notes){
            strings.add(note.getContent());
        }
        return strings;
    }


    @Deprecated
    private void addNoteBySp(String content){
        String spContents = GlobalUtils.restoreNoteContentFromSp(this);
        String[] spContentArr = spContents.split(GlobalUtils.DIVIDER);
        List<String> spContentList = Arrays.asList(spContentArr);
        if (spContentList.contains(content)) {
            //元素重复了
            LinkedList<String> listFromSp = new LinkedList<>();
            listFromSp.addAll(spContentList);
            listFromSp.remove(content);
            listFromSp.addFirst(content);
            GlobalUtils.saveNoteContentToSp(this, parseListToString(listFromSp));
        } else {
            //没有重复则保存
            if (spContentArr.length >= max) {
                //到达最大个数
                LinkedList<String> listFromSp = new LinkedList<>();
                listFromSp.addAll(spContentList);
                listFromSp.removeLast();
                listFromSp.addFirst(content);
                GlobalUtils.saveNoteContentToSp(this, parseListToString(listFromSp));
            } else {
                //没有到达最大个数
                GlobalUtils.addNoteContentToSp(this, content);
            }
        }

    }

    private Note addNoteByDB(String content){
        Note note = new Note();
        note.setContent(content);
        Date date = new Date(System.currentTimeMillis());
        note.setCreate_date(date);
        note.setUpdate_date(date);
        MyStorage.getInstance(this).insertNote(note);
        return note;
    }

    @Deprecated
    private void removeNoteBySp(String content){
        String spContents = GlobalUtils.restoreNoteContentFromSp(this);
        String[] spContentArr = spContents.split(GlobalUtils.DIVIDER);
        List<String> spContentList = Arrays.asList(spContentArr);
        if (spContentList.contains(content)) {
            LinkedList<String> listFromSp = new LinkedList<>();
            listFromSp.addAll(spContentList);
            listFromSp.remove(content);
            GlobalUtils.saveNoteContentToSp(this, parseListToString(listFromSp));
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
        if (mAllowDelete) {
            MenuItem item = mToolBar.getMenu().findItem(R.id.btn_menu_delete);
            switchMode(item);
            return;
        }
        super.onBackPressed();
    }

    public void initRecycleView() {
        List<Note> notes = MyStorage.getInstance(this).loadAllNotes();
        if(notes == null){
            notes = new ArrayList<>();
        }

        mRecycleView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(staggeredGridLayoutManager);
        mRecycleAdapter = new RecycleAdapter(notes, mRecycleViewHeight, mSpacing);
        mRecycleAdapter.setOnRecycleItemClickListener(new OnRecycleItemClickListener() {
            @Override
            public void onItemClick(View v) {
                final int position = (int) v.getTag();
                if (v.getId() == R.id.tv_item) {
                    mLastItem = mRecycleAdapter.getData().get(position);
                    showEditNodePopupWindow(v.getContext(), mLastItem);
                } else if (v.getId() == R.id.iv_delete_layer) {
                    if (mAllowDelete) {
                        //dialog ensure
                        int res[] = {R.layout.tip_remove_dialog, R.id.tv_title, R.id.tv_tip};
                        TipDialogFragment dialogFragment = TipDialogFragment.newInstance(res);
                        dialogFragment.show(MainActivity.this.getSupportFragmentManager(), "remove_tip");
                        dialogFragment.setOnOkBtnClickListener(
                                new TipDialogFragment.OnOkBtnClickListener() {
                                    @Override
                                    public void onOkClick() {
                                        MyStorage.getInstance(MainActivity.this).removeNote(mRecycleAdapter.getData().get(position));
                                        mRecycleAdapter.mData.remove(position);
                                        mRecycleAdapter.notifyItemRemoved(position);
                                        mRecycleAdapter.notifyItemRangeChanged(0, mRecycleAdapter.getItemCount() - 1);
                                    }
                                }
                        );
                    }
                }
            }

            @Override
            public void onItemLongClick(View v) {
                //todo
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
            if (mRecentTextView == null || mDeleteLayer == null)
                throw new RuntimeException("viewHolder is empty!");
            if (mRecentTextView.getLineCount() >= 1) {
                mRecentTextView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, MainActivity.this.getResources().getDisplayMetrics()), 1.0f);
            }
        }
    }

    class RecycleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Note> mData;
        private GlobalUtils.HeightUtil<GridLayoutManager.LayoutParams> mHeightUtil;
        private int mRecycleViewHeight;
        private int mSpace;

        public RecycleAdapter(List<Note> data, int recycleViewHeight, int space) {
            if(data == null){
                mData = new ArrayList<>();
            }else{
                mData = data;
            }
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
            viewHolder.mRecentTextView.setText(mData.get(i).getContent());
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

        public List<Note> getData() {
            return mData;
        }

        public void setData(List<Note> data) {
            mData = data;
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

    /**
     * is there exists a nav bar
     * @param c
     * @return
     */
    public int getNavBarHeight(Context c) {
        int result = 0;
        boolean hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if(!hasMenuKey && !hasBackKey) {
            //The device has a navigation bar
            Resources resources = c.getResources();

            int orientation = getResources().getConfiguration().orientation;
            int resourceId;
            if (isTablet(c)){
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            }  else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
            }
        }

        Log.d("MainAct", "hasMenuKey="+hasMenuKey+", hasBackKey="+hasBackKey+" navHeight="+result);
        return result;
    }


    private boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    private void showEditNodePopupWindow(Context c, final Note clickItem) {
        //setup content
        if (mEditNoteView == null) {
            mEditNoteView = LayoutInflater.from(c).inflate(R.layout.view_edit_note, mContainer, false);
        }
        Animation in = AnimationUtils.loadAnimation(this, R.anim.popup_in);
        mEditNoteView.setAnimation(in);

        //set content
        EditText et = (EditText) mEditNoteView.findViewById(R.id.et_content);
        if(clickItem != null){
            et.setText(clickItem.getContent());
        }else{
            et.setText("");
        }

        //listener
        mEditNoteView.findViewById(R.id.btn_save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText) mEditNoteView.findViewById(R.id.et_content);
                if(clickItem!=null){
                    //update
                    clickItem.setContent(et.getText().toString());
                    clickItem.setUpdate_date(new Date(System.currentTimeMillis()));
                    MyStorage.getInstance(MainActivity.this).updateNote(clickItem);
                    for (int i = 0; i < mRecycleAdapter.getData().size(); i++) {
                        if(mRecycleAdapter.getData().get(i).getId().equals(clickItem.getId())){
                            mRecycleAdapter.getData().get(i).setContent(et.getText().toString());
                            mRecycleAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }else{
                    //add new
                    Note note = addNoteByDB(et.getText().toString());
                    mRecycleAdapter.getData().add(0, note);
                    mRecycleAdapter.notifyItemInserted(0);
                    mRecycleView.scrollToPosition(0);
                }
                ToastUtils.showShort(getApplicationContext(), "保存成功");
                mPopupWindow.dismiss();
            }
        });
        mEditNoteView.findViewById(R.id.btn_save_and_pin).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText) mEditNoteView.findViewById(R.id.et_content);
                if(clickItem!=null){
                    //update
                    clickItem.setUpdate_date(new Date(System.currentTimeMillis()));
                    clickItem.setContent(et.getText().toString());
                    MyStorage.getInstance(MainActivity.this).updateNote(clickItem);
                    for (int i = 0; i < mRecycleAdapter.getData().size(); i++) {
                        if(mRecycleAdapter.getData().get(i).getId().equals(clickItem.getId())){
                            mRecycleAdapter.getData().get(i).setContent(et.getText().toString());
                            mRecycleAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    updateWidget(CONTENT, clickItem.getContent(), true);
                }else{
                    //add new
                    Note note = addNoteByDB(et.getText().toString());
                    mRecycleAdapter.getData().add(0, note);
                    mRecycleAdapter.notifyItemInserted(0);
                    mRecycleView.scrollToPosition(0);
                    updateWidget(CONTENT, note.getContent(), true);
                }
                ToastUtils.showShort(getApplicationContext(), "保存成功");
                mPopupWindow.dismiss();
            }
        });

        if (mPopupWindow == null) {
            float[] screenSize = DisplayUtil.getScreenSize();
            mPopupWindow = new PopupWindow(mEditNoteView, Math.round(screenSize[0]), Math.round(screenSize[1]));
            mPopupWindow.setTouchable(true);
            mPopupWindow.setFocusable(true);

            //dismiss when click outside
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setFocusable(true);

            mEditNoteView.findViewById(R.id.root_container).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mPopupWindow!=null && mEditNoteView.isShown()){
                        mPopupWindow.dismiss();
                    }
                }
            });
        }

        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(mContainer, Gravity.TOP | Gravity.LEFT, 0, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (resultCode == RESULT_TXT_COLOR_CODE) {
                updateWidget(TEXT_COLOR, String.valueOf(prefs.getInt(ColorPicActivity.COLOR, 0xFFFFFFFF)), false);
            } else if (resultCode == RESULT_TXT_SIZE_CODE){
                updateWidget(TEXT_SIZE, String.valueOf(prefs.getInt(TextSizePicActivity.TEXT_SIZE, TextSizePicActivity.DEFAULT_TEXT_SIZE)), false);
            } else {
                Log.i(TAG, "onActivityResult canceled");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
