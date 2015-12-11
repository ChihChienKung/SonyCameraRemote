package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetSupportedZoomSettingTask extends ApiTask<Void, Void, String[]> {

    GetSupportedZoomSettingTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected String[] doInBackground(Void... params) {
        String direction = null;
        String movement = null;
        try {
            JSONObject responseObj = mRemoteApi.getSupportedZoomSetting();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            JSONObject candidateObject = resultArray.getJSONObject(0);
            JSONArray candidateArray = candidateObject.getJSONArray(RemoteApi.API_GET_SUPPORTED_ZOOM_SETTING__KEY_CANDIDATE);
            direction = candidateArray.getString(0);
            movement = candidateArray.getString(1);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[]{direction, movement};
    }

    @Override
    protected void onPostExecute(String[] candidate) {
        super.onPostExecute(candidate);
        if (mListener != null)
            mListener.getSupportedZoomSettingResult(candidate[0], candidate[1], id);
    }
}
