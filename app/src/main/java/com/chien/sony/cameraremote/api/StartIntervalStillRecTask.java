package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class StartIntervalStillRecTask extends ApiTask<Void, Void, Integer> {

    StartIntervalStillRecTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = -1;
        try {
            JSONObject responseObj = mRemoteApi.startIntervalStillRec();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            result = resultArray.getInt(0);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (mListener != null)
            mListener.startIntervalStillRecResult(result, id);
    }
}
