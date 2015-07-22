package com.opticalix;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.opticalix.base.BaseActivity;
import com.opticalix.widget_reminder.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felix on 2015/6/24.
 */
public class RecycleViewActivity extends BaseActivity {

    private RecyclerView mRecycleView;
    private Context mContext;
    private ArrayList<String> mList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.recycle_act);
        mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);

        initRecycleView();
    }

    public void initRecycleView() {
        mList = new ArrayList<>();
        mList.add("initRecycleView");
        mList.add("ArrayListArrayList");
        mList.add("setHasFixedSize");
        mList.add("StaggeredGridLayoutManager");
        mList.add("11312321");
        mList.add("StaggeredGridLayoutManagerStaggeredGridLayoutManagerStaggeredGridLayoutManager");
        mList.add("setHasFixedSize");
        mList.add("11312321");
        mList.add("StaggeredGridLayoutManagerStaggeredGridLayoutManagerStaggeredGridLayoutManager");

        mRecycleView.setHasFixedSize(false);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(staggeredGridLayoutManager);
        final RecycleAdapter recycleAdapter = new RecycleAdapter(mList);
        recycleAdapter.setOnRecycleItemClickListener(new OnRecycleItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(getApplicationContext(), "onItemClick " + position, Toast.LENGTH_SHORT);
            }

            @Override
            public void onItemLongClick(int position) {
                mList.remove(position);
                recycleAdapter.notifyItemRemoved(position);
            }
        });
        mRecycleView.setAdapter(recycleAdapter);
        mRecycleView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.list_divider)));
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        RecentTextView mRecentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mRecentTextView = (RecentTextView) itemView.findViewById(R.id.tv_item);
            mRecentTextView.setTextSize(45);
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
            ViewHolder viewHolder = new ViewHolder(view);
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
