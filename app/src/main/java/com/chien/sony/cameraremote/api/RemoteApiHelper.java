/*
 * Copyright 2014 Sony Corporation
 */

package com.chien.sony.cameraremote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RemoteApiHelper {

    private static final String TAG = RemoteApiHelper.class.getSimpleName();

    private RemoteApiHelper() {

    }

    public static void setShootMode(final RemoteApi remoteApi, final String shootMode) {
        new Thread() {
            @Override
            public void run() {
                try {
                    remoteApi.setShootMode(shootMode);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

        }.start();

    }

    /**
     * Prepare request params and calls SimpleRemoteApi method to get date list
     * of storage contents. Request JSON data is such like as below.
     * <p/>
     * <pre>
     * {
     *   "method": "getContentList",
     *   "params": [{
     *      "sort" : "ascending"
     *      "view": "date"
     *      "uri": "storage:memoryCard1"
     *      }],
     *   "id": 2,
     *   "version": "1.3"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this
     *                     Exception.
     */
    public static JSONObject getContentDateList(RemoteApi remoteApi) throws IOException {

        try {
            List<String> uri = getSupportedStorages(remoteApi);

            if (uri == null) {
                Log.w(TAG, "supported Uri is null");
                throw new IOException();
            }

            JSONObject replyJson = remoteApi.getContentList(uri.get(0), 0, 100, "date", "ascending");
            return replyJson;

        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Prepare request params and calls SimpleRemoteApi method to get contents
     * list of storage. Request JSON data is such like as below.
     * <p/>
     * <pre>
     * {
     *   "method": "getContentList",
     *   "params": [{
     *      "sort" : "ascending"
     *      "view": "date"
     *      "type" : [
     *          "still",
     *          "movie_mp4",
     *          "movie_xavcs"
     *       ],
     *      "uri": "storage:memoryCard1?path=2014-03-31"
     *      }],
     *   "id": 2,
     *   "version": "1.3"
     * }
     * </pre>
     *
     * @param remoteApi         object of simpleRemoteApi
     * @param uri               uri of target date
     * @param isStreamSupported set true if target device supported streaming
     *                          playback
     * @throws IOException IOException all errors and exception are wrapped by
     *                     this Exception.
     */
    public static JSONObject getContentListOfDay(RemoteApi remoteApi, String uri, Boolean isStreamSupported) throws IOException {
        JSONObject replyJson = null;
        if (isStreamSupported) {
            // Device supports streaming API.
            // get still and movie contents.
            replyJson = remoteApi.getContentList(uri, 0, 100, "date", "ascending", "still", "movie_mp4", "movie_xavcs");
        } else {
            // Device does not support streaming API.
            // get only still contents.
            replyJson = remoteApi.getContentList(uri, 0, 100, "date", "ascending", "still");
        }
        return replyJson;
    }

    private static List<String> getSupportedStorages(RemoteApi remoteApi) throws IOException, JSONException {

        // Confirm Scheme
        JSONObject replyJsonScheme = remoteApi.getSchemeList();

        if (RemoteApi.isErrorReply(replyJsonScheme)) {
            JSONArray resultsObjScheme = replyJsonScheme.getJSONArray("error");
            int resultCode = resultsObjScheme.getInt(0);
            Log.w(TAG, "getSchemeList Error:" + resultCode);
            throw new IOException();
        }

        Set<String> schemeSet = new HashSet<String>();
        JSONArray resultsObjScheme = replyJsonScheme.getJSONArray("result").getJSONArray(0);

        for (int i = 0; i < resultsObjScheme.length(); i++) {
            schemeSet.add(resultsObjScheme.getJSONObject(i).getString("scheme"));
        }

        if (!schemeSet.contains("storage")) {
            Log.w(TAG, "This device does not support storage.");
            throw new IOException();
        }

        // Confirm Source
        JSONObject replyJsonSource = remoteApi.getSourceList("storage");

        if (RemoteApi.isErrorReply(replyJsonSource)) {
            JSONArray resultsObjSource = replyJsonSource.getJSONArray("error");
            int resultCode = resultsObjSource.getInt(0);
            Log.w(TAG, "getSourceList Error:" + resultCode);
            throw new IOException();
        }

        List<String> sourceList = new ArrayList<String>();
        JSONArray resultsObjSource = replyJsonSource.getJSONArray("result").getJSONArray(0);

        for (int i = 0; i < resultsObjSource.length(); i++) {
            sourceList.add(resultsObjSource.getJSONObject(i).getString("source"));
        }

        return sourceList;
    }

}
