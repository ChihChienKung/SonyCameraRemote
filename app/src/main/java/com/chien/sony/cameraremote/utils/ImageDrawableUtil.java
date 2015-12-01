package com.chien.sony.cameraremote.utils;

import android.widget.ImageView;

import com.chien.sony.cameraremote.CameraApplication;

/**
 * Created by Jean.Gong on 2015/11/30.
 */
public class ImageDrawableUtil {

    public static void setImageDrawable(CameraApplication application, ImageView imageView, int resId) {
        if (application.isNeedMrVector()) {
            imageView.setImageDrawable(application.getMrVectorDrawable(resId));
        } else {
            imageView.setImageResource(resId);
        }
    }
}
