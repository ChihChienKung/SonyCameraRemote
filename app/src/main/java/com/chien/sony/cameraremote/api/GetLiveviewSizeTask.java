package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetLiveviewSizeTask extends ApiTask<Void, Void, String> {

    GetLiveviewSizeTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected String doInBackground(Void... params) {
        String size = null;
        try {
            JSONObject responseObj = mRemoteApi.getLiveviewSize();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            size = resultArray.getString(0);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return size;
    }

    @Override
    protected void onPostExecute(String size) {
        super.onPostExecute(size);
        if (mListener != null)
            mListener.getLiveviewSizeResult(size, id);
    }
}
