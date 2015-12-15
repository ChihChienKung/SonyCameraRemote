package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class SetTouchAFPositionTask extends ApiTask<Double, Void, Integer> {
    boolean mAFResult;
    String mAFType;

    SetTouchAFPositionTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected Integer doInBackground(Double... params) {
        double x = params[0];
        double y = params[1];

        int resultCode = -1;
        try {
            JSONObject responseObj = mRemoteApi.setTouchAFPosition(x, y);
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            resultCode = resultArray.getInt(0);
            JSONObject touchAFObject = resultArray.getJSONObject(1);
            mAFResult = touchAFObject.getBoolean(RemoteApi.API_SET_TOUCH_AF_POSITION__AF_RESULT);
            mAFType = touchAFObject.getString(RemoteApi.API_SET_TOUCH_AF_POSITION__AF_TYPE);
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultCode;
    }

    @Override
    protected void onPostExecute(Integer resultCode) {
        super.onPostExecute(resultCode);
        if (mListener != null)
            mListener.setTouchAFPositionResult(resultCode, mAFResult, mAFType, id);
    }
}
