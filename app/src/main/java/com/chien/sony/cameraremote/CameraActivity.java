/*
 * Copyright 2014 Sony Corporation
 */

package com.chien.sony.cameraremote;

import com.chien.sony.cameraremote.api.RemoteApi;
import com.chien.sony.cameraremote.api.RemoteApiHelper;
import com.chien.sony.cameraremote.dialog.SettingDialog;
import com.chien.sony.cameraremote.utils.CameraCandidates;
import com.chien.sony.cameraremote.utils.CameraEventObserver;
import com.chien.sony.cameraremote.utils.DisplayHelper;
import com.chien.sony.cameraremote.utils.ImageDrawableUtil;
import com.chien.sony.cameraremote.utils.ViewAnimation;
import com.chien.sony.cameraremote.widget.FloatingActionButtonSpinner;
import com.chien.sony.cameraremote.widget.StreamSurfaceView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Chien on 2015/10/29.
 */
public class CameraActivity extends AppCompatActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();

    private ImageButton mCapture;

    private FloatingActionButton mSettings;

    private FloatingActionButtonSpinner mShootModeSpinner;

    private ImageView mImagePictureWipe;


    private FloatingActionButton mButtonZoomIn;

    private FloatingActionButton mButtonZoomOut;

    private TextView mTextCameraStatus;

    private ServerDevice mTargetServer;

    private RemoteApi mRemoteApi;

    private RemoteApiHelper mRemoteApiHelper;

    private StreamSurfaceView mLiveviewSurface;

    private CameraEventObserver mEventObserver;

    private final Set<String> mAvailableCameraApiSet = new HashSet<String>();

    private final Set<String> mSupportedApiSet = new HashSet<String>();

    private CameraCandidates mCameraCandidates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        CameraApplication app = (CameraApplication) getApplication();
        mTargetServer = app.getTargetServerDevice();
        mRemoteApi = app.getRemoteApi();
        mEventObserver = new CameraEventObserver(getApplicationContext(), mRemoteApi);

        //TODO Check widget.
        mLiveviewSurface = (StreamSurfaceView) findViewById(R.id.surfaceview_liveview);
        mCapture = (ImageButton) findViewById(R.id.btn_capture);
        mSettings = (FloatingActionButton) findViewById(R.id.btn_settings);
        mShootModeSpinner = (FloatingActionButtonSpinner) findViewById(R.id.btn_shoot_mode_spinner);
        mImagePictureWipe = (ImageView) findViewById(R.id.image_picture_wipe);

        //TODO Not check widget.
        mButtonZoomIn = (FloatingActionButton) findViewById(R.id.btn_zoom_in);
        mButtonZoomOut = (FloatingActionButton) findViewById(R.id.btn_zoom_out);
        mTextCameraStatus = (TextView) findViewById(R.id.text_camera_status);

        ImageDrawableUtil.setImageDrawable(app, mButtonZoomIn, R.drawable.ic_zoom_in);
        ImageDrawableUtil.setImageDrawable(app, mButtonZoomOut, R.drawable.ic_zoom_out);

        mCapture.setOnClickListener(mCaptureCLickListener);
        mSettings.setOnClickListener(mSettingsClickListener);
        mShootModeSpinner.setOnSelectListener(mShootModeSelectListener);
        mImagePictureWipe.setOnClickListener(mPictureWipeClickListener);

        mButtonZoomIn.setOnClickListener(mZoomClickListener);
        mButtonZoomOut.setOnClickListener(mZoomClickListener);
        mButtonZoomIn.setOnLongClickListener(mZoomLongClickListener);
        mButtonZoomOut.setOnLongClickListener(mZoomLongClickListener);
        mButtonZoomIn.setOnTouchListener(mZoomTouchListener);
        mButtonZoomOut.setOnTouchListener(mZoomTouchListener);
        Log.d(TAG, "onCreate() completed.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraCandidates = CameraCandidates.getInstance();
        mRemoteApiHelper = RemoteApiHelper.getInserts(mRemoteApi);
        mEventObserver.activate();



        prepareOpenConnection();

        Log.d(TAG, "onResume() completed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeConnection();
        CameraCandidates.clear();

        Log.d(TAG, "onPause() completed.");
    }

    private void prepareOpenConnection() {
        Log.w(TAG, "prepareToOpenConection() exec");

        setProgressBarIndeterminateVisibility(true);

        new Thread() {

            @Override
            public void run() {
                try {
                    // Get supported API list (Camera API)
                    JSONObject replyJsonCamera = mRemoteApi.getMethodTypes(RemoteApi.API_SERVICE_CAMERA, RemoteApi.VERSION_1_0);
                    loadSupportedApiList(replyJsonCamera);

                    try {
                        // Get supported API list (AvContent API)
                        JSONObject replyJsonAvcontent = mRemoteApi.getMethodTypes(RemoteApi.API_SERVICE_AV_CONTENT, RemoteApi.VERSION_1_0);
                        loadSupportedApiList(replyJsonAvcontent);
                    } catch (IOException e) {
                        Log.d(TAG, "AvContent is not support.");
                    }

                    CameraApplication app = (CameraApplication) getApplication();
                    app.setSupportedApiList(mSupportedApiSet);

                    if (!app.isApiSupported("setCameraFunction")) {

                        // this device does not support setCameraFunction.
                        // No need to check camera status.

                        openConnection();

                    } else {

                        // this device supports setCameraFunction.
                        // after confirmation of camera state, open connection.
                        Log.d(TAG, "this device support set camera function");

                        if (!app.isApiSupported("getEvent")) {
                            Log.e(TAG, "this device is not support getEvent");
                            openConnection();
                            return;
                        }

                        // confirm current camera status
                        String cameraStatus = null;
                        JSONObject replyJson = mRemoteApi.getEvent_v1_0(false);
                        JSONArray resultsObj = replyJson.getJSONArray(RemoteApi.API_RESULT);
                        JSONObject cameraStatusObj = resultsObj.getJSONObject(1);
                        String type = cameraStatusObj.getString(RemoteApi.API_GET_EVENT_TYPE);
                        if (RemoteApi.API_GET_EVENT_CAMERA_STATUS.equals(type)) {
                            cameraStatus = cameraStatusObj.getString(RemoteApi.API_GET_EVENT_CAMERA_STATUS);
                        } else {
                            throw new IOException();
                        }

                        if (CameraCandidates.isShootingStatus(cameraStatus)) {
                            Log.d(TAG, "camera function is Remote Shooting.");
                            openConnection();
                        } else {
                            // set Listener
                            startOpenConnectionAfterChangeCameraState();

                            // set Camera function to Remote Shooting
                            replyJson = mRemoteApi.setCameraFunction(RemoteApi.API_SET_CAMERA_FUNCTION_REMOTE_SHOOTING);
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: IOException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(CameraActivity.this, false);
                } catch (JSONException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: JSONException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(CameraActivity.this, false);
                }
            }
        }.start();
    }

    private void startOpenConnectionAfterChangeCameraState() {
        Log.d(TAG, "startOpenConectiontAfterChangeCameraState() exec");

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mEventObserver.setEventChangeListener(new CameraEventObserver.ChangeListenerTmpl() {

                    @Override
                    public void onCameraStatusChanged(String status) {
                        Log.d(TAG, "onCameraStatusChanged:" + status);
                        if (CameraCandidates.STATUS_IDLE.equals(status)) {
                            openConnection();
                        }
                        refreshUi();
                    }

                    @Override
                    public void onShootModeChanged(String shootMode) {
                        refreshUi();
                    }

                    @Override
                    public void onShootModeListChanged(List<String> shootModeList) {
                        refreshUi();
                    }

                    @Override
                    public void onStorageIdChanged(String storageId) {
                        refreshUi();
                    }
                });

                mEventObserver.start();
            }
        });
    }

    /**
     * Open connection to the camera device to start monitoring Camera events
     * and showing liveview.
     */
    private void openConnection() {
        Log.w(TAG, "openConnection()");
        mEventObserver.setEventChangeListener(mEventListener);
        new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "openConnection(): exec.");

                try {
                    JSONObject replyJson = null;

                    // getAvailableApiList
                    replyJson = mRemoteApi.getAvailableApiList();
                    loadAvailableCameraApiList(replyJson);

                    // check version of the server device
                    if (isCameraApiAvailable("getApplicationInfo")) {
                        Log.d(TAG, "openConnection(): getApplicationInfo()");
                        replyJson = mRemoteApi.getApplicationInfo();
                        if (!isSupportedServerVersion(replyJson)) {
                            DisplayHelper.toast(getApplicationContext(), //
                                    R.string.msg_error_non_supported_device);
                            CameraActivity.this.finish();
                            return;
                        }
                    } else {
                        // never happens;
                        return;
                    }

                    // startRecMode if necessary.
                    if (isCameraApiAvailable("startRecMode")) {
                        Log.d(TAG, "openConnection(): startRecMode()");
                        replyJson = mRemoteApi.startRecMode();

                        // Call again.
                        replyJson = mRemoteApi.getAvailableApiList();
                        loadAvailableCameraApiList(replyJson);
                    }

                    // getEvent start
                    if (isCameraApiAvailable("getEvent")) {
                        Log.d(TAG, "openConnection(): EventObserver.start()");
                        mEventObserver.start();
                    }

                    // Liveview start
                    if (isCameraApiAvailable("startLiveview")) {
                        Log.d(TAG, "openConnection(): LiveviewSurface.start()");
                        startLiveview();
                    }

                    // prepare UIs
                    if (isCameraApiAvailable("actZoom")) {
                        Log.d(TAG, "openConnection(): prepareActZoomButtons()");
                        prepareActZoomButtons(true);
                    } else {
                        prepareActZoomButtons(false);
                    }

                    Log.d(TAG, "openConnection(): completed.");
                } catch (IOException e) {
                    Log.w(TAG, "openConnection : IOException: " + e.getMessage());
                    DisplayHelper.setProgressIndicator(CameraActivity.this, false);
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_connection);
                }
            }
        }.start();

    }

    /**
     * Stop monitoring Camera events and close liveview connection.
     */
    private void closeConnection() {

        Log.d(TAG, "closeConnection(): exec.");
        // Liveview stop
        Log.d(TAG, "closeConnection(): LiveviewSurface.stop()");
        if (mLiveviewSurface != null) {
            mLiveviewSurface.stop();
//            mLiveviewSurface = null;
            stopLiveview();
        }

        // getEvent stop
        Log.d(TAG, "closeConnection(): EventObserver.release()");
        mEventObserver.release();

        // stopRecMode if necessary.
        if (isCameraApiAvailable("stopRecMode")) {
            new Thread() {

                @Override
                public void run() {
                    Log.d(TAG, "closeConnection(): stopRecMode()");
                    try {
                        mRemoteApi.stopRecMode();
                    } catch (IOException e) {
                        Log.w(TAG, "closeConnection: IOException: " + e.getMessage());
                    }
                }
            }.start();
        }
        mRemoteApiHelper.clear();
        Log.d(TAG, "closeConnection(): completed.");
    }

    private void refreshUi() {
        if (!mEventObserver.isStarted()) {
            mEventObserver.start();
        }

//        CameraApplication application = (CameraApplication) getApplication();
        String cameraStatus = mCameraCandidates.getCameraStatus();
        String shootMode = mCameraCandidates.getShootMode();

        // CameraStatus TextView
        mTextCameraStatus.setText(cameraStatus);
        try {
            refreshShootModeICon();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (CameraEventObserver.SHOOT_MODE_STILL.equals(shootMode)) {
            if (CameraCandidates.STATUS_IDLE.equals(cameraStatus)) {
                mCapture.setEnabled(true);
                mCapture.setImageResource(R.drawable.btn_capture_still);
            } else {
                mCapture.setEnabled(false);
            }
        } else if (CameraEventObserver.SHOOT_MODE_MOVIE.equals(shootMode) || CameraEventObserver.SHOOT_MODE_AUDIO.equals(shootMode) || CameraEventObserver.SHOOT_MODE_INTERVALSTILL.equals(shootMode) || CameraEventObserver.SHOOT_MODE_LOOPREC.equals(shootMode)) {
            if (CameraCandidates.STATUS_MOVIE_RECORDING.equals(cameraStatus) || CameraCandidates.STATUS_AUDIO_RECORDING.equals(cameraStatus) || CameraCandidates.STATUS_INTERVAL_RECORDING.equals(cameraStatus) || CameraCandidates.STATUS_LOOP_RECORDING.equals(cameraStatus)) {
                mCapture.setEnabled(true);
                mCapture.setImageResource(R.drawable.btn_capture_rec_stop);
            } else if (CameraCandidates.STATUS_IDLE.equals(cameraStatus)) {
                mCapture.setEnabled(true);
                mCapture.setImageResource(R.drawable.btn_capture_rec_start);
            } else {
                mCapture.setEnabled(false);
            }
        }


        // Picture wipe Image
        if (!CameraEventObserver.SHOOT_MODE_STILL.equals(shootMode)) {
//            mImagePictureWipe.setVisibility(View.INVISIBLE);
            ViewAnimation.hide(mImagePictureWipe);
        }

        // Shoot Mode Buttons
        if (CameraCandidates.STATUS_IDLE.equals(cameraStatus) || CameraCandidates.STATUS_MOVIE_RECORDING.equals(cameraStatus)) {
//            mSpinnerShootMode.setEnabled(true);
//            selectionShootModeSpinner(mSpinnerShootMode, shootMode);
        } else {
//            mSpinnerShootMode.setEnabled(false);
        }

        CameraApplication app = (CameraApplication) getApplication();

        // Contents List Button
        if (app.isApiSupported("getContentList") && app.isApiSupported("getSchemeList") && app.isApiSupported("getSourceList")) {
            String storageId = mCameraCandidates.getStorageId();
            if (storageId == null) {
                Log.d(TAG, "not update ContentsList button ");
            } else if ("No Media".equals(storageId)) {
//                mButtonContentsListMode.setEnabled(false);
            } else {
//                mButtonContentsListMode.setEnabled(true);
            }
        }
    }

    private void refreshShootModeICon() {
        String shootMode = mCameraCandidates.getShootMode();
        mShootModeSpinner.select(shootMode);
    }

    private void refreshShootMode() {
        mShootModeSpinner.removeAllChild();
        String shootMode = mCameraCandidates.getShootMode();
        List<String> shootModeList = mCameraCandidates.getShootModeList();
        for (String tag : shootModeList) {
            int resId;
            if (CameraEventObserver.SHOOT_MODE_STILL.equals(tag)) {
                resId = R.drawable.ic_still;
            } else if (CameraEventObserver.SHOOT_MODE_MOVIE.equals(tag)) {
                resId = R.drawable.ic_movie;
            } else if (CameraEventObserver.SHOOT_MODE_AUDIO.equals(tag)) {
                resId = R.drawable.ic_audio;
            } else if (CameraEventObserver.SHOOT_MODE_INTERVALSTILL.equals(tag)) {
                resId = R.drawable.ic_intervalstill;
            } else if (CameraEventObserver.SHOOT_MODE_LOOPREC.equals(tag)) {
                resId = R.drawable.ic_looprec;
            } else {
                throw new NullPointerException("No have shoot mode. shootMode=" + tag);
            }
            mShootModeSpinner.addChild(new FloatingActionButtonSpinner.ChildData(tag, resId));
        }
        mShootModeSpinner.select(shootMode);
    }


    /**
     * Retrieve a list of APIs that are available at present.
     *
     * @param replyJson
     */
    private void loadAvailableCameraApiList(JSONObject replyJson) {
        synchronized (mAvailableCameraApiSet) {
            mAvailableCameraApiSet.clear();
            try {
                JSONArray resultArrayJson = replyJson.getJSONArray(RemoteApi.API_RESULT);
                JSONArray apiListJson = resultArrayJson.getJSONArray(0);
                for (int i = 0; i < apiListJson.length(); i++) {
                    mAvailableCameraApiSet.add(apiListJson.getString(i));
                }
            } catch (JSONException e) {
                Log.w(TAG, "loadAvailableCameraApiList: JSON format error.");
            }
        }
    }

    /**
     * Retrieve a list of APIs that are supported by the target device.
     *
     * @param replyJson
     */
    private void loadSupportedApiList(JSONObject replyJson) {
        synchronized (mSupportedApiSet) {
            try {
                JSONArray resultArrayJson = replyJson.getJSONArray(RemoteApi.API_RESULTS);
                for (int i = 0; i < resultArrayJson.length(); i++) {
                    mSupportedApiSet.add(resultArrayJson.getJSONArray(i).getString(0));
                }
            } catch (JSONException e) {
                Log.w(TAG, "loadSupportedApiList: JSON format error.");
            }
        }
    }

    /**
     * Check if the specified API is available at present. This works correctly
     * only for Camera API.
     *
     * @param apiName
     * @return
     */
    private boolean isCameraApiAvailable(String apiName) {
        boolean isAvailable = false;
        synchronized (mAvailableCameraApiSet) {
            isAvailable = mAvailableCameraApiSet.contains(apiName);
        }
        return isAvailable;
    }


    /**
     * Check if the version of the server is supported in this application.
     *
     * @param replyJson
     * @return
     */
    private boolean isSupportedServerVersion(JSONObject replyJson) {
        try {
            JSONArray resultArrayJson = replyJson.getJSONArray("result");
            String version = resultArrayJson.getString(1);
            String[] separated = version.split("\\.");
            int major = Integer.valueOf(separated[0]);
            if (2 <= major) {
                return true;
            }
        } catch (JSONException e) {
            Log.w(TAG, "isSupportedServerVersion: JSON format error.");
        } catch (NumberFormatException e) {
            Log.w(TAG, "isSupportedServerVersion: Number format error.");
        }
        return false;
    }

    /**
     * Prepare for Button to select "actZoom" by user.
     *
     * @param flag
     */
    private void prepareActZoomButtons(final boolean flag) {
        Log.d(TAG, "prepareActZoomButtons(): exec.");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                prepareActZoomButtonsUi(flag);
            }
        });

    }

    /**
     * Prepare for ActZoom Button UI.
     *
     * @param flag
     */
    private void prepareActZoomButtonsUi(boolean flag) {
        if (flag) {
            mButtonZoomOut.setVisibility(View.VISIBLE);
            mButtonZoomIn.setVisibility(View.VISIBLE);
        } else {
            mButtonZoomOut.setVisibility(View.GONE);
            mButtonZoomIn.setVisibility(View.GONE);
        }
    }

    /**
     * Take a picture and retrieve the image data.
     */
    private void takeAndFetchPicture() {
        if (mLiveviewSurface == null || !mLiveviewSurface.isStarted()) {
            DisplayHelper.toast(getApplicationContext(), R.string.msg_error_take_picture);
            return;
        }

        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.actTakePicture();
                    JSONArray resultsObj = replyJson.getJSONArray(RemoteApi.API_RESULT);
                    JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                    String postImageUrl = null;
                    if (1 <= imageUrlsObj.length()) {
                        postImageUrl = imageUrlsObj.getString(0);
                    }
                    if (postImageUrl == null) {
                        Log.w(TAG, "takeAndFetchPicture: post image URL is null.");
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_error_take_picture);
                        return;
                    }
                    // Show progress indicator
                    DisplayHelper.setProgressIndicator(CameraActivity.this, true);

                    URL url = new URL(postImageUrl);
                    InputStream istream = new BufferedInputStream(url.openStream());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4; // irresponsible value
                    final Drawable pictureDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeStream(istream, null, options));
                    istream.close();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            ViewAnimation.show(mImagePictureWipe);
//                            mImagePictureWipe.setVisibility(View.VISIBLE);
                            mImagePictureWipe.setImageDrawable(pictureDrawable);
                        }
                    });

                } catch (IOException e) {
                    Log.w(TAG, "IOException while closing slicer: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } catch (JSONException e) {
                    Log.w(TAG, "JSONException while closing slicer");
                    DisplayHelper.toast(getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } finally {
                    DisplayHelper.setProgressIndicator(CameraActivity.this, false);
                }
            }
        }.start();
    }

    private void startMovieRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "startMovieRec: exec.");
                    JSONObject replyJson = mRemoteApi.startMovieRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_rec_start);
                    } else {
                        Log.w(TAG, "startMovieRec: error: " + resultCode);
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startMovieRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startMovieRec: JSON format error.");
                }
            }
        }.start();
    }

    private void stopMovieRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "stopMovieRec: exec.");
                    JSONObject replyJson = mRemoteApi.stopMovieRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    String thumbnailUrl = resultsObj.getString(0);
                    if (thumbnailUrl != null) {
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_rec_stop);
                    } else {
                        Log.w(TAG, "stopMovieRec: error");
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "stopMovieRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "stopMovieRec: JSON format error.");
                }
            }
        }.start();
    }

    private void startAudioRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "startAudioRec: exec.");
                    JSONObject replyJson = mRemoteApi.startAudioRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_rec_start);
                    } else {
                        Log.w(TAG, "startAudioRec: error: " + resultCode);
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startAudioRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startAudioRec: JSON format error.");
                }
            }
        }.start();
    }

    private void stopAudioRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "stopAudioRec: exec.");
                    JSONObject replyJson = mRemoteApi.stopAudioRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    String thumbnailUrl = resultsObj.getString(0);
                    if (thumbnailUrl != null) {
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_rec_stop);
                    } else {
                        Log.w(TAG, "stopAudioRec: error");
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "stopAudioRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "stopAudioRec: JSON format error.");
                }
            }
        }.start();
    }

    private void startIntervalStillRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "startIntervalStillRec: exec.");
                    JSONObject replyJson = mRemoteApi.startIntervalStillRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_rec_start);
                    } else {
                        Log.w(TAG, "startIntervalStillRec: error: " + resultCode);
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startIntervalStillRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startIntervalStillRec: JSON format error.");
                }
            }
        }.start();
    }

    private void stopIntervalStillRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "stopIntervalStillRec: exec.");
                    JSONObject replyJson = mRemoteApi.stopIntervalStillRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    String thumbnailUrl = resultsObj.getString(0);
                    if (thumbnailUrl != null) {
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_rec_stop);
                    } else {
                        Log.w(TAG, "stopIntervalStillRec: error");
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "stopIntervalStillRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "stopIntervalStillRec: JSON format error.");
                }
            }
        }.start();
    }

    private void startLoopRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "startLoopRec: exec.");
                    JSONObject replyJson = mRemoteApi.startLoopRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_rec_start);
                    } else {
                        Log.w(TAG, "startLoopRec: error: " + resultCode);
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startLoopRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startLoopRec: JSON format error.");
                }
            }
        }.start();
    }

    private void stopLoopRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "stopLoopRec: exec.");
                    JSONObject replyJson = mRemoteApi.stopLoopRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    String thumbnailUrl = resultsObj.getString(0);
                    if (thumbnailUrl != null) {
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_rec_stop);
                    } else {
                        Log.w(TAG, "stopLoopRec: error");
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "stopLoopRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "stopLoopRec: JSON format error.");
                }
            }
        }.start();
    }

    /**
     * Call actZoom
     *
     * @param direction
     * @param movement
     */
    private void actZoom(final String direction, final String movement) {
        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.actZoom(direction, movement);
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        // Success, but no refresh UI at the point.
                        Log.v(TAG, "actZoom: success");
                    } else {
                        Log.w(TAG, "actZoom: error: " + resultCode);
                        DisplayHelper.toast(getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "actZoom: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "actZoom: JSON format error.");
                }
            }
        }.start();
    }

    private void prepareToStartContentsListMode() {
        Log.d(TAG, "prepareToStartContentsListMode() exec");
        new Thread() {

            @Override
            public void run() {
                try {
                    // set Listener
                    moveToDateListAfterChangeCameraState();

                    // set camera function to Contents Transfer
                    Log.d(TAG, "call setCameraFunction");
                    JSONObject replyJson = mRemoteApi.setCameraFunction("Contents Transfer");
                    if (RemoteApi.isErrorReply(replyJson)) {
                        Log.w(TAG, "prepareToStartContentsListMode: set CameraFunction error: ");
                        DisplayHelper.toast(getApplicationContext(), R.string.msg_error_content);
                        mEventObserver.setEventChangeListener(mEventListener);
                    }

                } catch (IOException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: IOException: " + e.getMessage());
                }
            }
        }.start();

    }

    private void moveToDateListAfterChangeCameraState() {
        Log.w(TAG, "moveToDateListAfterChangeCameraState() exec");

        // set Listener
        mEventObserver.setEventChangeListener(new CameraEventObserver.ChangeListenerTmpl() {

            @Override
            public void onCameraStatusChanged(String status) {
                Log.d(TAG, "onCameraStatusChanged:" + status);
                if (CameraCandidates.STATUS_CONTENTS_TRANSFER.equals(status)) {
                    // start ContentsList mode
                    Intent intent = new Intent(getApplicationContext(), DateListActivity.class);
                    startActivity(intent);
                }

                refreshUi();
            }

            @Override
            public void onShootModeChanged(String shootMode) {
                refreshUi();
            }

            @Override
            public void onShootModeListChanged(List<String> shootModeList) {
                Log.w(TAG, "onShootModeListChanged() called: " + shootModeList);
                refreshShootMode();
            }
        });
    }

    private void startLiveview() {
        if (mLiveviewSurface == null) {
            Log.w(TAG, "startLiveview mLiveviewSurface is null.");
            return;
        }
        new Thread() {
            @Override
            public void run() {

                try {
                    JSONObject replyJson = null;
                    replyJson = mRemoteApi.startLiveview();

                    if (!RemoteApi.isErrorReply(replyJson)) {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        if (1 <= resultsObj.length()) {
                            // Obtain liveview URL from the result.
                            final String liveviewUrl = resultsObj.getString(0);
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mLiveviewSurface.start(liveviewUrl, //
                                            new StreamSurfaceView.StreamErrorListener() {

                                                @Override
                                                public void onError(StreamErrorReason reason) {
                                                    stopLiveview();
                                                }
                                            });
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startLiveview IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startLiveview JSONException: " + e.getMessage());
                }
            }
        }.start();
    }

    private void stopLiveview() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mRemoteApi.stopLiveview();
                } catch (IOException e) {
                    Log.w(TAG, "stopLiveview IOException: " + e.getMessage());
                }
            }
        }.start();
    }

    private final CameraEventObserver.ChangeListener mEventListener = new CameraEventObserver.ChangeListenerTmpl() {

        @Override
        public void onShootModeChanged(String shootMode) {
            Log.d(TAG, "onShootModeChanged() called: " + shootMode);
            refreshUi();
        }

        @Override
        public void onShootModeListChanged(List<String> shootModeList) {
            Log.w(TAG, "onShootModeListChanged() called: " + shootModeList);
            refreshShootMode();
        }

        @Override
        public void onCameraStatusChanged(String status) {
            Log.d(TAG, "onCameraStatusChanged() called: " + status);
            refreshUi();
        }

        @Override
        public void onApiListModified(List<String> apis) {
            Log.d(TAG, "onApiListModified() called");
            synchronized (mAvailableCameraApiSet) {
                mAvailableCameraApiSet.clear();
                for (String api : apis) {
                    mAvailableCameraApiSet.add(api);
                }
                if (!mCameraCandidates.getLiveviewStatus() && isCameraApiAvailable("startLiveview")) {
                    if (mLiveviewSurface != null && !mLiveviewSurface.isStarted()) {
                        startLiveview();
                    }
                }
                if (isCameraApiAvailable("actZoom")) {
                    Log.d(TAG, "onApiListModified(): prepareActZoomButtons()");
                    prepareActZoomButtons(true);
                } else {
                    prepareActZoomButtons(false);
                }
            }
        }

        @Override
        public void onZoomPositionChanged(int zoomPosition) {
            Log.d(TAG, "onZoomPositionChanged() called = " + zoomPosition);
            if (zoomPosition == 0) {
                mButtonZoomIn.setEnabled(true);
                mButtonZoomOut.setEnabled(false);
            } else if (zoomPosition == 100) {
                mButtonZoomIn.setEnabled(false);
                mButtonZoomOut.setEnabled(true);
            } else {
                mButtonZoomIn.setEnabled(true);
                mButtonZoomOut.setEnabled(true);
            }
        }

        @Override
        public void onLiveviewStatusChanged(boolean status) {
            Log.d(TAG, "onLiveviewStatusChanged() called = " + status);
        }

        @Override
        public void onStorageIdChanged(String storageId) {
            Log.d(TAG, "onStorageIdChanged() called: " + storageId);
            refreshUi();
        }
    };

    private final View.OnClickListener mZoomClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_zoom_in:
                    actZoom("in", "1shot");
                    break;
                case R.id.btn_zoom_out:
                    actZoom("out", "1shot");
                    break;
            }
        }
    };

    private final View.OnLongClickListener mZoomLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.btn_zoom_in:
                    actZoom("in", "start");
                    break;
                case R.id.btn_zoom_out:
                    actZoom("out", "start");
                    break;
            }
            return true;
        }
    };

    private final View.OnTouchListener mZoomTouchListener = new View.OnTouchListener() {

        private long zoomInDownTime = -1;
        private long zoomOutDownTime = -1;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                switch (v.getId()) {
                    case R.id.btn_zoom_in:
                        if (System.currentTimeMillis() - zoomInDownTime > 500) {
                            actZoom("in", "stop");
                        }
                        break;
                    case R.id.btn_zoom_out:
                        if (System.currentTimeMillis() - zoomOutDownTime > 500) {
                            actZoom("out", "stop");
                        }
                        break;
                }
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                switch (v.getId()) {
                    case R.id.btn_zoom_in:
                        zoomInDownTime = System.currentTimeMillis();
                        break;
                    case R.id.btn_zoom_out:
                        zoomOutDownTime = System.currentTimeMillis();
                        break;
                }
            }
            return false;
        }
    };

    private final View.OnClickListener mCaptureCLickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String cameraStatus = mCameraCandidates.getCameraStatus();
            String shootMode = mCameraCandidates.getShootMode();

            if (CameraEventObserver.SHOOT_MODE_STILL.equals(shootMode)) {
                takeAndFetchPicture();
            } else if (CameraEventObserver.SHOOT_MODE_MOVIE.equals(shootMode)) {
                if (CameraCandidates.STATUS_MOVIE_RECORDING.equals(cameraStatus)) {
                    stopMovieRec();
                } else if (CameraCandidates.STATUS_IDLE.equals(cameraStatus)) {
                    startMovieRec();
                }
            } else if (CameraEventObserver.SHOOT_MODE_AUDIO.equals(shootMode)) {
                if (CameraCandidates.STATUS_AUDIO_RECORDING.equals(cameraStatus)) {
                    stopAudioRec();
                } else if (CameraCandidates.STATUS_IDLE.equals(cameraStatus)) {
                    startAudioRec();
                }
            } else if (CameraEventObserver.SHOOT_MODE_INTERVALSTILL.equals(shootMode)) {
                if (CameraCandidates.STATUS_INTERVAL_RECORDING.equals(cameraStatus)) {
                    stopIntervalStillRec();
                } else if (CameraCandidates.STATUS_IDLE.equals(cameraStatus)) {
                    startIntervalStillRec();
                }
            } else if (CameraEventObserver.SHOOT_MODE_LOOPREC.equals(shootMode)) {
                if (CameraCandidates.STATUS_LOOP_RECORDING.equals(cameraStatus)) {
                    stopLoopRec();
                } else if (CameraCandidates.STATUS_IDLE.equals(cameraStatus)) {
                    startLoopRec();
                }
            }
        }
    };

    private final View.OnClickListener mPictureWipeClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ViewAnimation.hide(mImagePictureWipe);
        }
    };

    private final View.OnClickListener mSettingsClickListener = new View.OnClickListener() {
        private SettingDialog mSettingDialog;

        @Override
        public void onClick(View view) {
            if (mSettingDialog == null) {
                synchronized (CameraActivity.class) {
                    if (mSettingDialog == null) {
                        mSettingDialog = new SettingDialog();
                        mSettingDialog.setOnDismissListener(mOnDismissListener);
                        mSettingDialog.show(getSupportFragmentManager(), null);
                    }
                }
            }
        }

        private DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mSettingDialog != null) {
                    synchronized (CameraActivity.class) {
                        if (mSettingDialog != null) {
                            mSettingDialog = null;
                        }
                    }
                }
            }
        };
    };

    private final FloatingActionButtonSpinner.OnSelectListener mShootModeSelectListener = new FloatingActionButtonSpinner.OnSelectListener() {
        @Override
        public void onSelect(FloatingActionButtonSpinner parent, String tag) {
            if (CameraCandidates.STATUS_IDLE.equals(mCameraCandidates.getCameraStatus())) {
                mRemoteApiHelper.setShootMode(tag);
            }
        }
    };
}
