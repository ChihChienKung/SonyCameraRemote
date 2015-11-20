package com.chien.sony.cameraremote.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.chien.sony.cameraremote.R;

/**
 * Created by Jean.Gong on 2015/11/19.
 */
public class DeviceItem extends CardView {
    private TextView mText;

    public DeviceItem(final Context context) {
        super(context);
        init();
    }

    public DeviceItem(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contentView = layoutInflater.inflate(R.layout.item_device, this, true);
        mText = (TextView) contentView.findViewById(R.id.device_name);
    }

    public void setDeviceName(final CharSequence text) {
        mText.setText(text);
    }


}