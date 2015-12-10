package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetAvailableLiveviewSizeTask extends ApiTask<Void, Void, GetAvailableLiveviewSizeTask.AvailableLiveviewSizeResult> {

    GetAvailableLiveviewSizeTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected AvailableLiveviewSizeResult doInBackground(Void... params) {
        AvailableLiveviewSizeResult result = new AvailableLiveviewSizeResult();
        try {
            JSONObject responseObj = mRemoteApi.getAvailableLiveviewSize();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            result.currentLiveviewSize = resultArray.getString(0);
            JSONArray supportedShootArray = resultArray.getJSONArray(1);
            int size = supportedShootArray.length();
            result.supportLiveviewSize = new String[size];
            for (int i = 0; i < size; i++) {
                result.supportLiveviewSize[i] = supportedShootArray.getString(i);
            }
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(AvailableLiveviewSizeResult result) {
        super.onPostExecute(result);
        if (mListener != null)
            mListener.getAvailableLiveviewSizeResult(result.currentLiveviewSize, result.supportLiveviewSize, id);
    }

    static class AvailableLiveviewSizeResult{
        String currentLiveviewSize;
        String[] supportLiveviewSize;
    }
}
