package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class ActTrackingFocusTask extends ApiTask<Double, Void, Void> {
    boolean mAFResult;
    String mAFType;

    ActTrackingFocusTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected Void doInBackground(Double... params) {
        double x = params[0];
        double y = params[1];

        try {
            JSONObject responseObj = mRemoteApi.actTrackingFocus(x, y);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mListener != null)
            mListener.actTrackingFocusResult(id);
    }
}
