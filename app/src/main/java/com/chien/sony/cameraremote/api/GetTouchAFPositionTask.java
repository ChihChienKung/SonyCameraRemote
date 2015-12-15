package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
class GetTouchAFPositionTask extends ApiTask<Void, Void, Void> {
    boolean mSet;
    double[] mTouchCoordinates;

    GetTouchAFPositionTask(RemoteApi remoteApi, IApiResultListener listener) {
        super(remoteApi, listener);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            JSONObject responseObj = mRemoteApi.getTouchAFPosition();
            JSONArray resultArray = responseObj.getJSONArray(RemoteApi.API_RESULT);
            JSONObject touchAFObject = resultArray.getJSONObject(0);
            mSet = touchAFObject.getBoolean(RemoteApi.API_GET_TOUCH_AF_POSITION__SET);

            JSONArray touchCoordinatesArray = touchAFObject.getJSONArray(RemoteApi.API_GET_TOUCH_AF_POSITION__TOUCH_COORDINATES);
            int size = touchCoordinatesArray.length();
            mTouchCoordinates = new double[size];
            for (int i = 0; i < size; i++) {
                mTouchCoordinates[i] = touchCoordinatesArray.getDouble(i);
            }
            id = responseObj.getInt(RemoteApi.API_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void resultCode) {
        super.onPostExecute(resultCode);
        if (mListener != null)
            mListener.getTouchAFPositionResult(mSet, mTouchCoordinates, id);
    }
}
