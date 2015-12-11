package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetAvailableZoomSettingTask extends ApiTask<Void, Void, String[]> {

    GetAvailableZoomSettingTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected String[] doInBackground(Void... params) {
        String zoom = null;
        String direction = null;
        String movement = null;
        try {
            JSONObject responseObj = mRemoteApi.getAvailableZoomSetting();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            JSONObject candidateObject = resultArray.getJSONObject(0);
            zoom = candidateObject.getString(RemoteApi.API_GET_ZOOM_SETTING__KEY_ZOOM);
            JSONArray candidateArray = candidateObject.getJSONArray(RemoteApi.API_GET_SUPPORTED_ZOOM_SETTING__KEY_CANDIDATE);
            direction = candidateArray.getString(0);
            movement = candidateArray.getString(1);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[]{zoom, direction, movement};
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        if (mListener != null)
            mListener.getAvailableZoomSettingResult(result[0], result[1], result[2], id);
    }
}
