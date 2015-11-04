/*
 * Copyright 2014 Sony Corporation
 */

package com.chien.sony.cameraremote;

import com.chien.sony.cameraremote.ServerDevice.ApiService;
import com.chien.sony.cameraremote.utils.CameraHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Simple Camera Remote API wrapper class. (JSON based API <--> Java API)
 */
public class RemoteApi {

    private static final String TAG = RemoteApi.class.getSimpleName();

    public static final String API_SERVICE_CAMERA = "camera";
    public static final String API_SERVICE_AV_CONTENT = "avContent";
    public static final String API_SERVICE_SYSTEM = "system";

    private static final String KEY_METHOD = "method";
    private static final String KEY_PARAMS = "params";
    private static final String KEY_ID = "id";
    private static final String KEY_VERSION = "version";

    public static final String VERSION_1_0 = "1.0";
    public static final String VERSION_1_1 = "1.1";
    public static final String VERSION_1_2 = "1.2";
    public static final String VERSION_1_3 = "1.3";

    // If you'd like to suppress detailed log output, change this value into
    // false.
    private static final boolean FULL_LOG = true;

    // API server device you want to send requests.
    private ServerDevice mTargetServer;

    // Request ID of API calling. This will be counted up by each API calling.
    private int mRequestId;

    public RemoteApi(ServerDevice target) {
        mTargetServer = target;
        mRequestId = 1;
    }

    private String findActionListUrl(String service) throws IOException {
        List<ApiService> services = mTargetServer.getApiServices();
        for (ApiService apiService : services) {
            if (apiService.getName().equals(service)) {
                return apiService.getActionListUrl();
            }
        }
        throw new IOException("actionUrl not found. service : " + service);
    }

    private int id() {
        return mRequestId++;
    }

    private void log(String msg) {
        if (FULL_LOG) {
            Log.d(TAG, msg);
        }
    }

    // Camera Service APIs
    private JSONObject command(String version, String service, String mothod, Object... params) throws IOException {
        JSONArray jsonArray = new JSONArray();
        if (params.length > 1)
            for (Object obj : params)
                jsonArray.put(obj);
        try {
            JSONObject requestJson =
                    new JSONObject()
                            .put(KEY_METHOD, mothod)
                            .put(KEY_PARAMS, jsonArray)
                            .put(KEY_ID, id())
                            .put(KEY_VERSION, version);
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = CameraHttpClient.httpPost(url, requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private JSONObject command_v1_0(String service, String mothod, Object... params) throws IOException {
        return command(VERSION_1_0, service, mothod, params);
    }

    private JSONObject command_v1_1(String service, String mothod, Object... params) throws IOException {
        return command(VERSION_1_1, service, mothod, params);
    }

    private JSONObject command_v1_2(String service, String mothod, Object... params) throws IOException {
        return command(VERSION_1_2, service, mothod, params);
    }

    private JSONObject command_v1_3(String service, String mothod, Object... params) throws IOException {
        return command(VERSION_1_3, service, mothod, params);
    }

    public JSONObject setShootMode(String shootMode) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getShootMode", shootMode);
    }

    public JSONObject getShootMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getShootMode");
    }

    public JSONObject getSupportedShootMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedShootMode");
    }

    public JSONObject getAvailableShootMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableShootMode");
    }

    public JSONObject actTakePicture() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "actTakePicture");
    }

    public JSONObject awaitTakePicture() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "awaitTakePicture");
    }

    public JSONObject startContShooting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "startContShooting");
    }

    public JSONObject stopContShooting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "stopContShooting");
    }

    public JSONObject startMovieRec() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "startMovieRec");
    }

    public JSONObject stopMovieRec() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "stopMovieRec");
    }

    public JSONObject startAudioRec() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "startAudioRec");
    }

    public JSONObject stopAudioRec() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "stopAudioRec");
    }

    public JSONObject startIntervalStillRec() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "startIntervalStillRec");
    }

    public JSONObject stopIntervalStillRec() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "stopIntervalStillRec");
    }

    public JSONObject startLoopRec() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "startLoopRec");
    }

    public JSONObject stopLoopRec() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "stopLoopRec");
    }

    public JSONObject startLiveview() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "startLiveview");
    }

    public JSONObject stopLiveview() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "stopLiveview");
    }

    public JSONObject startLiveviewWithSize(String liveviewSize) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "startLiveviewWithSize", liveviewSize);
    }

    public JSONObject getLiveviewSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getLiveviewSize");
    }

    public JSONObject getSupportedLiveviewSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedLiveviewSize");
    }

    public JSONObject getAvailableLiveviewSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableLiveviewSize");
    }

    public JSONObject setLiveviewFrameInfo(boolean frameInfo) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("frameInfo", frameInfo);

            return command_v1_0(API_SERVICE_CAMERA, "getAvailableLiveviewSize", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getLiveviewFrameInfo() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getLiveviewFrameInfo");
    }

    public JSONObject actZoom(String direction, String movement) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "actZoom", direction, movement);
    }

    public JSONObject setZoomSetting(String zoom) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("zoom", zoom);

            return command_v1_0(API_SERVICE_CAMERA, "setZoomSetting", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getZoomSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getZoomSetting");
    }

    public JSONObject getSupportedZoomSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedZoomSetting");
    }

    public JSONObject getAvailableZoomSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableZoomSetting");
    }

    public JSONObject actHalfPressShutter() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "actHalfPressShutter");
    }

    public JSONObject cancelHalfPressShutter() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "cancelHalfPressShutter");
    }

    public JSONObject setTouchAFPosition(float x, float y) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setTouchAFPosition", x, y);
    }

    public JSONObject getTouchAFPosition() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getTouchAFPosition");
    }

    public JSONObject cancelTouchAFPosition() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "cancelTouchAFPosition");
    }

    public JSONObject actTrackingFocus(float x, float y) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("xPosition", x).put("yPosition", y);

            return command_v1_0(API_SERVICE_CAMERA, "actTrackingFocus", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject cancelTrackingFocus() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "cancelTrackingFocus");
    }

    public JSONObject setTrackingFocus(String trackingFocus) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("trackingFocus", trackingFocus);

            return command_v1_0(API_SERVICE_CAMERA, "actTrackingFocus", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getTrackingFocus() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getTrackingFocus");
    }

    public JSONObject getSupportedTrackingFocus() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedTrackingFocus");
    }

    public JSONObject getAvailableTrackingFocus() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableTrackingFocus");
    }

    public JSONObject setContShootingMode(String contShootingMode) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("contShootingMode", contShootingMode);

            return command_v1_0(API_SERVICE_CAMERA, "setContShootingMode", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getContShootingMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getContShootingMode");
    }

    public JSONObject getSupportedContShootingMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedContShootingMode");
    }

    public JSONObject getAvailableContShootingMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableContShootingMode");
    }

    public JSONObject setContShootingSpeed(String contShootingSpeed) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("contShootingSpeed", contShootingSpeed);

            return command_v1_0(API_SERVICE_CAMERA, "setContShootingSpeed", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getContShootingSpeed() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getContShootingSpeed");
    }

    public JSONObject getSupportedContShootingSpeed() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedContShootingSpeed");
    }

    public JSONObject getAvailableContShootingSpeed() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableContShootingSpeed");
    }

    public JSONObject setSelfTimer(int selfTimer) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setSelfTimer", selfTimer);
    }

    public JSONObject getSelfTimer() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSelfTimer");
    }

    public JSONObject getSupportedSelfTimer() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedSelfTimer");
    }

    public JSONObject getAvailableSelfTimer() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableSelfTimer");
    }

    public JSONObject setExposureMode(String exposureMode) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setExposureMode", exposureMode);
    }

    public JSONObject getExposureMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getExposureMode");
    }

    public JSONObject getSupportedExposureMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedExposureMode");
    }

    public JSONObject getAvailableExposureMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableExposureMode");
    }

    public JSONObject setFocusMode(String focusMode) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setFocusMode", focusMode);
    }

    public JSONObject getFocusMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getFocusMode");
    }

    public JSONObject getSupportedFocusMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedFocusMode");
    }

    public JSONObject getAvailableFocusMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableFocusMode");
    }

    public JSONObject setExposureCompensation(int exposureCompensation) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setExposureCompensation", exposureCompensation);
    }

    public JSONObject getExposureCompensation() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getExposureCompensation");
    }

    public JSONObject getSupportedExposureCompensation() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedExposureCompensation");
    }

    public JSONObject getAvailableExposureCompensation() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableExposureCompensation");
    }

    public JSONObject setFNumber(String fNumber) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setFNumber", fNumber);
    }

    public JSONObject getFNumber() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getFNumber");
    }

    public JSONObject getSupportedFNumber() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedFNumber");
    }

    public JSONObject getAvailableFNumber() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableFNumber");
    }

    public JSONObject setShutterSpeed(String shutterSpeed) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setShutterSpeed", shutterSpeed);
    }

    public JSONObject getShutterSpeed() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getShutterSpeed");
    }

    public JSONObject getSupportedShutterSpeed() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedShutterSpeed");
    }

    public JSONObject getAvailableShutterSpeed() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableShutterSpeed");
    }

    public JSONObject setIsoSpeedRate(String isoSpeedRate) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setIsoSpeedRate", isoSpeedRate);
    }

    public JSONObject getIsoSpeedRate() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getIsoSpeedRate");
    }

    public JSONObject getSupportedIsoSpeedRate() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedIsoSpeedRate");
    }

    public JSONObject getAvailableIsoSpeedRate() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableIsoSpeedRate");
    }

    public JSONObject setWhiteBalance(String whiteBalance) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setWhiteBalance", whiteBalance);
    }

    public JSONObject getWhiteBalance() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getWhiteBalance");
    }

    public JSONObject getSupportedWhiteBalance() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedWhiteBalance");
    }

    public JSONObject getAvailableWhiteBalance() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableWhiteBalance");
    }

    public JSONObject actWhiteBalanceOnePushCustom() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "actWhiteBalanceOnePushCustom");
    }

    public JSONObject setProgramShift(int programShift) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setWhiteBalance", programShift);
    }

    public JSONObject getSupportedProgramShift() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedProgramShift");
    }

    public JSONObject setFlashMode(String flashMode) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setWhiteBalance", flashMode);
    }

    public JSONObject getFlashMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getFlashMode");
    }

    public JSONObject getSupportedFlashMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedFlashMode");
    }

    public JSONObject getAvailableFlashMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableFlashMode");
    }

    public JSONObject setStillSize(String stillAspect, String stillSize) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setWhiteBalance", stillAspect, stillSize);
    }

    public JSONObject getStillSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getStillSize");
    }

    public JSONObject getSupportedStillSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedStillSize");
    }

    public JSONObject getAvailableStillSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableStillSize");
    }

    public JSONObject setStillQuality(String stillQuality) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("stillQuality", stillQuality);

            return command_v1_0(API_SERVICE_CAMERA, "setStillQuality", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getStillQuality() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getStillQuality");
    }

    public JSONObject getSupportedStillQuality() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedStillQuality");
    }

    public JSONObject getAvailableStillQuality() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableStillQuality");
    }

    public JSONObject setPostviewImageSize(String imageSize) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setPostviewImageSize", imageSize);
    }

    public JSONObject getPostviewImageSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getPostviewImageSize");
    }

    public JSONObject getSupportedPostviewImageSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedPostviewImageSize");
    }

    public JSONObject getAvailablePostviewImageSize() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailablePostviewImageSize");
    }

    public JSONObject setMovieFileFormat(String movieFileFormat) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("movieFileFormat", movieFileFormat);

            return command_v1_0(API_SERVICE_CAMERA, "setMovieFileFormat", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getMovieFileFormat() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getMovieFileFormat");
    }

    public JSONObject getSupportedMovieFileFormat() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedMovieFileFormat");
    }

    public JSONObject getAvailableMovieFileFormat() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableMovieFileFormat");
    }

    public JSONObject setMovieQuality(String movieQuality) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setMovieQuality", movieQuality);
    }

    public JSONObject getMovieQuality() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getMovieQuality");
    }

    public JSONObject getSupportedMovieQuality() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedMovieQuality");
    }

    public JSONObject getAvailableMovieQuality() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableMovieQuality");
    }

    public JSONObject setSteadyMode(String steadyMode) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setSteadyMode", steadyMode);
    }

    public JSONObject getSteadyMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSteadyMode");
    }

    public JSONObject getSupportedSteadyMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedSteadyMode");
    }

    public JSONObject getAvailableSteadyMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableSteadyMode");
    }

    public JSONObject setViewAngle(int angle) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setSteadyMode", angle);
    }

    public JSONObject getViewAngle() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getViewAngle");
    }

    public JSONObject getSupportedViewAngle() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedViewAngle");
    }

    public JSONObject getAvailableViewAngle() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableViewAngle");
    }

    public JSONObject setSceneSelection(String sceneSelection) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("scene", sceneSelection);

            return command_v1_0(API_SERVICE_CAMERA, "setSceneSelection", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getSceneSelection() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSceneSelection");
    }

    public JSONObject getSupportedSceneSelection() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedSceneSelection");
    }

    public JSONObject getAvailableSceneSelection() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableSceneSelection");
    }

    public JSONObject setColorSetting(String colorSetting) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("colorSetting", colorSetting);

            return command_v1_0(API_SERVICE_CAMERA, "setColorSetting", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getColorSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getColorSetting");
    }

    public JSONObject getSupportedColorSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedColorSetting");
    }

    public JSONObject getAvailableColorSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableColorSetting");
    }

    public JSONObject setIntervalTime(String intervalTimeSec) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("intervalTimeSec", intervalTimeSec);

            return command_v1_0(API_SERVICE_CAMERA, "setIntervalTime", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getIntervalTime() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getIntervalTime");
    }

    public JSONObject getSupportedIntervalTime() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedIntervalTime");
    }

    public JSONObject getAvailableIntervalTime() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableIntervalTime");
    }

    public JSONObject setLoopRecTime(String loopRecTimeMin) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("loopRecTimeMin", loopRecTimeMin);

            return command_v1_0(API_SERVICE_CAMERA, "setLoopRecTime", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getLoopRecTime() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getLoopRecTime");
    }

    public JSONObject getSupportedLoopRecTime() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedLoopRecTime");
    }

    public JSONObject getAvailableLoopRecTime() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableLoopRecTime");
    }

    public JSONObject setWindNoiseReduction(boolean windNoiseReduction) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("windNoiseReduction", windNoiseReduction);

            return command_v1_0(API_SERVICE_CAMERA, "setWindNoiseReduction", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getWindNoiseReduction() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getWindNoiseReduction");
    }

    public JSONObject getSupportedWindNoiseReduction() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedWindNoiseReduction");
    }

    public JSONObject getAvailableWindNoiseReduction() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableWindNoiseReduction");
    }

    public JSONObject setAudioRecording(String audioRecording) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("audioRecording", audioRecording);

            return command_v1_0(API_SERVICE_CAMERA, "setAudioRecording", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getAudioRecording() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAudioRecording");
    }

    public JSONObject getSupportedAudioRecording() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedAudioRecording");
    }

    public JSONObject getAvailableAudioRecording() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableAudioRecording");
    }

    public JSONObject setFlipSetting(String flip) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("flip", flip);

            return command_v1_0(API_SERVICE_CAMERA, "setFlipSetting", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getFlipSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getFlipSetting");
    }

    public JSONObject getSupportedFlipSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedFlipSetting");
    }

    public JSONObject getAvailableFlipSetting() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableFlipSetting");
    }

    public JSONObject setTvColorSystem(String tvColorSystem) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("tvColorSystem", tvColorSystem);

            return command_v1_0(API_SERVICE_CAMERA, "setTvColorSystem", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getTvColorSystem() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getTvColorSystem");
    }

    public JSONObject getSupportedTvColorSystem() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedTvColorSystem");
    }

    public JSONObject getAvailableTvColorSystem() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableTvColorSystem");
    }

    public JSONObject startRecMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "startRecMode");
    }

    public JSONObject stopRecMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "stopRecMode");
    }

    public JSONObject setCameraFunction(String cameraFunction) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "setCameraFunction", cameraFunction);
    }

    public JSONObject getCameraFunction() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getCameraFunction");
    }

    public JSONObject getSupportedCameraFunction() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedCameraFunction");
    }

    public JSONObject getAvailableCameraFunction() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableCameraFunction");
    }

    public JSONObject getSchemeList() throws IOException {
        return command_v1_0(API_SERVICE_AV_CONTENT, "getSchemeList");
    }

    public JSONObject getSourceList(String scheme) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("scheme", scheme);

            return command_v1_0(API_SERVICE_AV_CONTENT, "getSourceList", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getContentCount(String uri, String target, String view, String... type) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("uri", uri).put("target", target).put("view", view);
            if (type != null) {
                JSONArray jsonArray = new JSONArray();
                for (String s : type)
                    jsonArray.put(s);
                object.put("type", jsonArray);
            }
            return command_v1_2(API_SERVICE_AV_CONTENT, "getContentCount", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getContentList(String uri, int stIdx, int cnt, String view, String sort, String... type) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("uri", uri).put("stIdx", stIdx).put("cnt", cnt).put("view", view).put("sort", sort);
            if (type != null) {
                JSONArray jsonArray = new JSONArray();
                for (String s : type)
                    jsonArray.put(s);
                object.put("type", jsonArray);
            }
            return command_v1_3(API_SERVICE_AV_CONTENT, "getContentList", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject setStreamingContent(String remotePlayType, String uri) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("remotePlayType", remotePlayType).put("uri", uri);

            return command_v1_0(API_SERVICE_AV_CONTENT, "setStreamingContent", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject startStreaming() throws IOException {
        return command_v1_0(API_SERVICE_AV_CONTENT, "startStreaming");
    }

    public JSONObject pauseStreaming() throws IOException {
        return command_v1_0(API_SERVICE_AV_CONTENT, "pauseStreaming");
    }

    public JSONObject seekStreamingPosition(String positionMsec) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("positionMsec", positionMsec);

            return command_v1_0(API_SERVICE_AV_CONTENT, "seekStreamingPosition", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject stopStreaming() throws IOException {
        return command_v1_0(API_SERVICE_AV_CONTENT, "stopStreaming");
    }

    public JSONObject requestToNotifyStreamingStatus(String polling) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("polling", polling);

            return command_v1_0(API_SERVICE_AV_CONTENT, "requestToNotifyStreamingStatus", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject deleteContent(String... uris) throws IOException {
        try {
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            for (String s : uris)
                array.put(s);
            object.put("uri", array);

            return command_v1_1(API_SERVICE_AV_CONTENT, "deleteContent", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject setInfraredRemoteControl(String infraredRemoteControl) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("infraredRemoteControl", infraredRemoteControl);

            return command_v1_0(API_SERVICE_CAMERA, "setInfraredRemoteControl", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getInfraredRemoteControl() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getInfraredRemoteControl");
    }

    public JSONObject getSupportedInfraredRemoteControl() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedInfraredRemoteControl");
    }

    public JSONObject getAvailableInfraredRemoteControl() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableInfraredRemoteControl");
    }

    public JSONObject setAutoPowerOff(int autoPowerOff) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("autoPowerOff", autoPowerOff);

            return command_v1_0(API_SERVICE_CAMERA, "setAutoPowerOff", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getAutoPowerOff() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAutoPowerOff");
    }

    public JSONObject getSupportedAutoPowerOff() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedAutoPowerOff");
    }

    public JSONObject getAvailableAutoPowerOff() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableAutoPowerOff");
    }

    public JSONObject setBeepMode(String beepMode) throws IOException {
            return command_v1_0(API_SERVICE_CAMERA, "setBeepMode", beepMode);
    }

    public JSONObject getBeepMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getBeepMode");
    }

    public JSONObject getSupportedBeepMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getSupportedBeepMode");
    }

    public JSONObject getAvailableBeepMode() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableBeepMode");
    }

    public JSONObject setCurrentTime(String dateTime, int timeZoneOffsetMinute, int dstOffsetMinute) throws IOException {
        try {
            JSONObject object = new JSONObject();
            object.put("dateTime", dateTime).put("timeZoneOffsetMinute", timeZoneOffsetMinute).put("dstOffsetMinute", dstOffsetMinute);

            return command_v1_0(API_SERVICE_SYSTEM, "setCurrentTime", object);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject getStorageInformation() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getStorageInformation");
    }

    public JSONObject getEvent_v1_0(boolean longPollingFlag) throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getEvent", longPollingFlag);
    }

    public JSONObject getEvent_v1_1(boolean longPollingFlag) throws IOException {
        return command_v1_1(API_SERVICE_CAMERA, "getEvent", longPollingFlag);
    }

    public JSONObject getEvent_v1_2(boolean longPollingFlag) throws IOException {
        return command_v1_2(API_SERVICE_CAMERA, "getEvent", longPollingFlag);
    }

    public JSONObject getEvent_v1_3(boolean longPollingFlag) throws IOException {
        return command_v1_3(API_SERVICE_CAMERA, "getEvent", longPollingFlag);
    }

    public JSONObject getAvailableApiList() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getAvailableApiList");
    }

    public JSONObject getApplicationInfo() throws IOException {
        return command_v1_0(API_SERVICE_CAMERA, "getApplicationInfo");
    }

    public JSONObject getVersions(String service) throws IOException {
        return command_v1_0(service, "getVersions");
    }

    public JSONObject getMethodTypes(String service, String version) throws IOException {
        return command_v1_0(service, "getMethodTypes", version);
    }

    // static method

    /**
     * Parse JSON and return whether it has error or not.
     *
     * @param replyJson JSON object to check
     * @return return true if JSON has error. otherwise return false.
     */
    public static boolean isErrorReply(JSONObject replyJson) {
        boolean hasError = (replyJson != null && replyJson.has("error"));
        return hasError;
    }
}
