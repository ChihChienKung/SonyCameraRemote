package com.chien.sony.cameraremote.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class CancelHalfPressShutterTask extends ApiTask<Void, Void, Void> {

    CancelHalfPressShutterTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            JSONObject responseObj = mRemoteApi.cancelHalfPressShutter();
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
            mListener.cancelHalfPressShutterResult(id);
    }
}
