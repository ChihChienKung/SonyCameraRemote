package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetAvailableShootModeTask extends ApiTask<Void, Void, GetAvailableShootModeTask.AvailableShootModeResult> {

    GetAvailableShootModeTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected AvailableShootModeResult doInBackground(Void... params) {
        AvailableShootModeResult result = new AvailableShootModeResult();
        try {
            JSONObject responseObj = mRemoteApi.getAvailableShootMode();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            result.currentShootMode = resultArray.getString(0);
            JSONArray supportedShootArray = resultArray.getJSONArray(1);
            int size = supportedShootArray.length();
            result.suppertShootMode = new String[size];
            for (int i = 0; i < size; i++) {
                result.suppertShootMode[i] = supportedShootArray.getString(i);
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
    protected void onPostExecute(AvailableShootModeResult result) {
        super.onPostExecute(result);
        if (mListener != null)
            mListener.getAvailableShootModeResult(result.currentShootMode, result.suppertShootMode, id);
    }

    static class AvailableShootModeResult{
        String currentShootMode;
        String[] suppertShootMode;
    }
}
