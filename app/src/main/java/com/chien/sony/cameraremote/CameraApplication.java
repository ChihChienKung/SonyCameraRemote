/*
 * Copyright 2014 Sony Corporation
 */

package com.chien.sony.cameraremote;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.chien.sony.cameraremote.api.RemoteApi;
import com.telly.mrvector.MrVector;

import java.util.Set;

/**
 * Application class for the sample application.
 */
public class CameraApplication extends Application {

    private ServerDevice mTargetDevice;

    private RemoteApi mRemoteApi;

    private Set<String> mSupportedApiSet;

    {
        {
            MrVector.register(
                    R.drawable.ic_refresh,
                    R.drawable.ic_still,
                    R.drawable.ic_movie,
                    R.drawable.ic_intervalstill,
                    R.drawable.ic_audio,
                    R.drawable.ic_looprec,
                    R.drawable.ic_zoom_in,
                    R.drawable.ic_zoom_out
            );
        }
    }

    /**
     * Sets a target ServerDevice object.
     *
     * @param device
     */
    public void setTargetServerDevice(ServerDevice device) {
        mTargetDevice = device;
        mRemoteApi = new RemoteApi(mTargetDevice);
    }

    /**
     * Returns a target ServerDevice object.
     *
     * @return return ServiceDevice
     */
    public ServerDevice getTargetServerDevice() {
        return mTargetDevice;
    }

    /**
     * Returns a SimpleRemoteApi object.
     *
     * @return return SimpleRemoteApi
     */
    public RemoteApi getRemoteApi() {
        return mRemoteApi;
    }

    /**
     * Sets a List of supported APIs.
     *
     * @param apiList
     */
    public void setSupportedApiList(Set<String> apiList) {
        mSupportedApiSet = apiList;
    }

    public boolean isApiSupported(String apiName) {
        boolean isAvailable = false;
        synchronized (mSupportedApiSet) {
            isAvailable = mSupportedApiSet.contains(apiName);
        }
        return isAvailable;
    }

    /**
     * Returns a list of supported APIs.
     *
     * @return Returns a list of supported APIs.
     */
    public Set<String> getSupportedApiList() {
        return mSupportedApiSet;
    }

    public boolean isNeedMrVector() {
        return Build.VERSION.SDK_INT < 21;
    }

    public Drawable getMrVectorDrawable(int resId) {
        return MrVector.inflate(getResources(), resId);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MrVector.wrap(newBase));
    }
}
