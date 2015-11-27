package com.chien.sony.cameraremote.widget.recyclerView;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.chien.sony.cameraremote.R;

/**
 * Created by Chien on 2015/11/19.
 */
public class RecyclerStringItem extends CardView {
    private TextView mText;

    public RecyclerStringItem(final Context context) {
        super(context);
        init();
    }

    public RecyclerStringItem(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contentView = layoutInflater.inflate(R.layout.recycler_string_item, this, true);
        mText = (TextView) contentView.findViewById(R.id.recycler_item_text);
    }

    public void setText(final CharSequence text) {
        mText.setText(text);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
    }
}