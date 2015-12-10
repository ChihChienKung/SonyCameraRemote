package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class StartLiveviewWithSizeTask extends ApiTask<String, Void, String> {

    StartLiveviewWithSizeTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected String doInBackground(String... params) {
        String liveViewSize = params[0];
        String url = null;
        try {
            JSONObject responseObj = mRemoteApi.startLiveviewWithSize(liveViewSize);
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            url = resultArray.getString(0);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    protected void onPostExecute(String url) {
        super.onPostExecute(url);
        if (mListener != null)
            mListener.startLiveviewWithSizeResult(url, id);
    }
}
