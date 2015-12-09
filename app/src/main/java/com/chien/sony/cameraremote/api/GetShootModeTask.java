package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetShootModeTask extends ApiTask<Void, Void, String> {

    GetShootModeTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected String doInBackground(Void... params) {
        String result = null;
        try {
            JSONObject responseObj = mRemoteApi.getShootMode();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            result = resultArray.getString(0);
            id = responseObj.getInt(RemoteApi.API_ID);
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(mListener != null)
            mListener.getShootModeResult(result, id);
    }
}
