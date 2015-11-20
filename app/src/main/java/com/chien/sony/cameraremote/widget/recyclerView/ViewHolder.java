
package com.chien.sony.cameraremote.widget.recyclerView;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;

public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnFocusChangeListener, OnKeyListener {
    private RecyclerAdapter.OnItemAction mOnItemAction;

    public ViewHolder(final View itemLayoutView) {
        super(itemLayoutView);
        itemLayoutView.setOnClickListener(this);
        itemLayoutView.setOnFocusChangeListener(this);
        itemLayoutView.setOnKeyListener(this);
    }

    @Override
    public void onClick(final View v) {
        if (mOnItemAction != null)
            mOnItemAction.onItemClick(v, getAdapterPosition());
    }

    @Override
    public void onFocusChange(final View v, final boolean hasFocus) {
        if (mOnItemAction != null)
            mOnItemAction.onItemFocusChange(v, getAdapterPosition(), hasFocus);
    }

    @Override
    public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
        if (mOnItemAction != null)
            return mOnItemAction.onItemKey(v, getAdapterPosition(), keyCode, event);
        return false;
    }

    public RecyclerAdapter.OnItemAction getOnItemAction() {
        return mOnItemAction;
    }

    public void setOnItemAction(final RecyclerAdapter.OnItemAction onItemAction) {
        this.mOnItemAction = onItemAction;
    }

}
