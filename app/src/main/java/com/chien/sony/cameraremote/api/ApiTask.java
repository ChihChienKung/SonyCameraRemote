package com.chien.sony.cameraremote.api;

import android.os.AsyncTask;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
public abstract class ApiTask<X, Y, Z> extends AsyncTask<X, Y, Z> {
    protected final IApiResultListener mListener;
    protected final RemoteApi mRemoteApi;
    protected int id = -1;

    ApiTask(RemoteApi remoteApi, IApiResultListener listener) {
        mRemoteApi = remoteApi;
        mListener = listener;
    }

}
