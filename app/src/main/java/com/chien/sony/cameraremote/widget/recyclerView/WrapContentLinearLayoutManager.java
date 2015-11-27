
package com.chien.sony.cameraremote.widget.recyclerView;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class WrapContentLinearLayoutManager extends LinearLayoutManager {

    private final int[] mMeasuredDimension = new int[2];

    private final int[] mDividerSize = new int[2];

    public WrapContentLinearLayoutManager(final Context context) {
        super(context);
    }

    public WrapContentLinearLayoutManager(final Context context, final int orientation, final boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public void setDeviderSize(final int widget, final int height) {
        mDividerSize[0] = widget;
        mDividerSize[1] = height;
    }

    @Override
    public void onMeasure(final RecyclerView.Recycler recycler, final RecyclerView.State state, final int widthSpec, final int heightSpec) {
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        int width = 0;
        int height = 0;
        for (int i = 0; i < getItemCount(); i++) {
            measureScrapChild(recycler, i, View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED), mMeasuredDimension);

            if (getOrientation() == HORIZONTAL) {
                width = width + mMeasuredDimension[0] + mDividerSize[0];
                if (mMeasuredDimension[1] > width) {
                    height = mMeasuredDimension[1];
                }
            } else {
                height = height + mMeasuredDimension[1] + mDividerSize[1];
                if (mMeasuredDimension[0] > width) {
                    width = mMeasuredDimension[0];
                }
            }
        }

        if (getOrientation() == HORIZONTAL) {
            if (width > widthSize)
                width = widthSize;
        } else {
            if (height > heightSize)
                height = heightSize;
        }

        switch (widthMode) {
            case View.MeasureSpec.EXACTLY:
                width = widthSize;
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.UNSPECIFIED:
        }

        switch (heightMode) {
            case View.MeasureSpec.EXACTLY:
                height = heightSize;
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.UNSPECIFIED:
        }

        setMeasuredDimension(width, height);
    }

    private void measureScrapChild(final RecyclerView.Recycler recycler, final int position, final int widthSpec, final int heightSpec, final int[] measuredDimension) {
        final View view = recycler.getViewForPosition(position);
        if (view != null) {
            final RecyclerView.LayoutParams p = (RecyclerView.LayoutParams)view.getLayoutParams();
            final int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, getPaddingLeft() + getPaddingRight(), p.width);
            final int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, getPaddingTop() + getPaddingBottom(), p.height);
            view.measure(childWidthSpec, childHeightSpec);
            measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
            measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
            recycler.recycleView(view);
        }
    }
}
