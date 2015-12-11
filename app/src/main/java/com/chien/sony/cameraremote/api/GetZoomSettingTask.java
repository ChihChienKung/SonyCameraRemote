package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetZoomSettingTask extends ApiTask<Void, Void, String> {

    GetZoomSettingTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected String doInBackground(Void... params) {
        String zoom = null;
        try {
            JSONObject responseObj = mRemoteApi.getZoomSetting();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            JSONObject zoomObject = resultArray.getJSONObject(0);
            zoom = zoomObject.getString(RemoteApi.API_GET_ZOOM_SETTING__KEY_ZOOM);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return zoom;
    }

    @Override
    protected void onPostExecute(String zoom) {
        super.onPostExecute(zoom);
        if (mListener != null)
            mListener.getZoomSettingResult(zoom, id);
    }
}
