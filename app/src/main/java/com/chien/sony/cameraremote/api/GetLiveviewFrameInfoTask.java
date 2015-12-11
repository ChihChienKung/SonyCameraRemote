package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetLiveviewFrameInfoTask extends ApiTask<Void, Void, Boolean> {

    GetLiveviewFrameInfoTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean frameInfo = false;
        try {
            JSONObject responseObj = mRemoteApi.getLiveviewFrameInfo();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            JSONObject frameInfoObject = resultArray.getJSONObject(0);
            frameInfo = frameInfoObject.getBoolean(RemoteApi.API_GET_LIVEVIEW_FRAME_INFO__KEY_FRAME_INFO);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return frameInfo;
    }

    @Override
    protected void onPostExecute(Boolean frameInfo) {
        super.onPostExecute(frameInfo);
        if (mListener != null)
            mListener.getLiveviewFrameInfoResult(frameInfo, id);
    }
}
