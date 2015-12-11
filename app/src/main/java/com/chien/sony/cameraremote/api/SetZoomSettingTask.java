package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class SetZoomSettingTask extends ApiTask<String, Void, Integer> {

    SetZoomSettingTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected Integer doInBackground(String... params) {
        String zoom = params[0];
        int result = -1;
        try {
            JSONObject responseObj = mRemoteApi.setZoomSetting(zoom);
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
    protected void onPostExecute(Integer resultCode) {
        super.onPostExecute(resultCode);
        if (mListener != null)
            mListener.setZoomSettingResult(resultCode, id);
    }
}
