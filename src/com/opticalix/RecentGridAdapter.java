package com.opticalix;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opticalix.widget_reminder.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Felix on 2015/6/15.
 */
public class RecentGridAdapter extends BaseAdapter {
    private Context mContext;
    private int mResource;
    private List<String> mList;
    private int mGridHeight = 0;
    private int mGridSpacing = 0;
    private final int line = 3;

    public RecentGridAdapter(Context context, int resource, List<String> contentList, int gridHeight, int spacing) {
        this.mContext = context;
        this.mResource = resource;
        this.mList = contentList;
        this.mGridHeight = gridHeight;
        this.mGridSpacing = spacing;
    }

    public RecentGridAdapter(Context context, int resource, String[] contentArr, int gridHeight, int spacing) {
        this.mContext = context;
        this.mResource = resource;
        this.mList = Arrays.asList(contentArr);
        this.mGridHeight = gridHeight;
        this.mGridSpacing = spacing;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(mResource, null);
        if(mList == null || mList.size() == 0 || (mList.size() == 1 && mList.get(0).equals(""))){
            view.setBackgroundColor(0x00ffffff);
            view.setVisibility(View.GONE);
            return view;
        }
        if (mList.size() > 0) {
            view.setBackgroundResource(R.drawable.selector_grid_item_bg);
            view.setVisibility(View.VISIBLE);
            AbsListView.LayoutParams layoutParams = (AbsListView.LayoutParams) view.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new AbsListView.LayoutParams(-1, getHeight());
            }
            layoutParams.height = getHeight();
            view.setLayoutParams(layoutParams);
            view.requestLayout();

            RecentTextView tv = (RecentTextView) view.findViewById(R.id.tv_item);
            tv.setText(mList.get(position));
        }
        return view;
    }

    private int getHeight() {
        return (mGridHeight - mGridSpacing * (line - 1)) / line;
    }

}
