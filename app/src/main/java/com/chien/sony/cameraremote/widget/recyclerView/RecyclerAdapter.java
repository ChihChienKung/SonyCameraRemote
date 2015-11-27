
package com.chien.sony.cameraremote.widget.recyclerView;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.KeyEvent;
import android.view.View;

public abstract class RecyclerAdapter<T extends ViewHolder> extends Adapter<T> {

    private OnItemClickListener mOnItemClickListener;

    private OnItemFocusChangeListener mOnItemFocusChangeListener;

    private OnItemKeyListener mOnItemKeyListener;

    @Override
    public void onBindViewHolder(final T t, final int position) {
        if (mOnItemAction != t.getOnItemAction()) {
            t.setOnItemAction(mOnItemAction);
        }
    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemFocusChangeListener(final OnItemFocusChangeListener onItemFocusChangeListener) {
        mOnItemFocusChangeListener = onItemFocusChangeListener;
    }

    public void setOnItemKeyListener(final OnItemKeyListener onItemKeyListener) {
        mOnItemKeyListener = onItemKeyListener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public OnItemFocusChangeListener getOnItemFocusChangeListener() {
        return mOnItemFocusChangeListener;
    }

    public OnItemKeyListener getOnItemKeyListener() {
        return mOnItemKeyListener;
    }

    public abstract Object getItem(final int position);

    private final OnItemAction mOnItemAction = new OnItemAction() {

        @Override
        public void onItemClick(final View view, final int position) {
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(RecyclerAdapter.this, view, position);
        }

        @Override
        public void onItemFocusChange(final View view, final int position, final boolean hasFocus) {
            if (mOnItemFocusChangeListener != null)
                mOnItemFocusChangeListener.onItemFocusChange(RecyclerAdapter.this, view, position, hasFocus);
        }

        @Override
        public boolean onItemKey(final View view, final int position, final int keyCode, final KeyEvent event) {
            if (mOnItemKeyListener != null)
                return mOnItemKeyListener.onItemKey(RecyclerAdapter.this, view, position, keyCode, event);
            return false;
        }
    };

    public static interface OnItemClickListener {
        public void onItemClick(RecyclerAdapter<?> adapter, View view, int position);
    }

    public static interface OnItemFocusChangeListener {
        public void onItemFocusChange(RecyclerAdapter<?> adapter, View view, int position, boolean hasFocus);
    }

    public static interface OnItemKeyListener {
        public boolean onItemKey(RecyclerAdapter<?> adapter, View view, int position, final int keyCode, final KeyEvent event);
    }

    protected static interface OnItemAction {
        public void onItemClick(View view, int position);

        public void onItemFocusChange(View view, int position, boolean hasFocus);

        public boolean onItemKey(View view, int position, final int keyCode, final KeyEvent event);
    }

}
