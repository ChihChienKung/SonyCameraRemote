package com.chien.sony.cameraremote.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jean.Gong on 2015/11/5.
 */
public class CameraCandidates {
    private static CameraCandidates mCameraCandidates;

    public static final String STATUS_ERROR = "Error";
    public static final String STATUS_NOT_READY = "NotReady";

    public static final String STATUS_IDLE = "IDLE";
    public static final String STATUS_STILL_CAPTURING = "StillCapturing";
    public static final String STATUS_STILL_SAVING = "StillSaving";
    public static final String STATUS_MOVIE_WAIT_REC_START = "MovieWaitRecStart";
    public static final String STATUS_MOVIE_RECORDING = "MovieRecording";
    public static final String STATUS_MOVIE_WAIT_REC_STOP = "MovieWaitRecStop";
    public static final String STATUS_MOVIE_SAVING = "MovieSaving";
    public static final String STATUS_AUDIO_WAIT_REC_START = "AudioWaitRecStart";
    public static final String STATUS_AUDIO_RECORDING = "AudioRecording";
    public static final String STATUS_AUDIO_WAIT_REC_STOP = "AudioWaitRecStop";
    public static final String STATUS_AUDIO_SAVING = "AudioSaving";
    public static final String STATUS_INTERVAL_WAIT_REC_START = "IntervalWaitRecStart";
    public static final String STATUS_INTERVAL_RECORDING = "IntervalRecording";
    public static final String STATUS_INTERVAL_WAIT_REC_STOP = "IntervalWaitRecStop";

    public static final String STATUS_LOOP_WAIT_REC_START = "LoopWaitRecStart";
    public static final String STATUS_LOOP_RECORDING = "LoopRecording";
    public static final String STATUS_LOOP_WAIT_REC_STOP = "LoopWaitRecStop";
    public static final String STATUS_LOOP_SAVING = "LoopSaving";
    public static final String STATUS_WHITE_BALANCE_ONE_PUSH_CAPTURING = "WhiteBalanceOnePushCapturing";
    public static final String STATUS_CONTENTS_TRANSFER = "ContentsTransfer";
    public static final String STATUS_STREAMING = "Streaming";
    public static final String STATUS_DELETING = "Deleting";

    public static final String TYPE_AVAILABLE_API_LIST = "availableApiList";
    public static final String TYPE_CAMERA_STATUS = "cameraStatus";
    public static final String TYPE_ZOOM_INFORMATION = "zoomInformation";
    public static final String TYPE_LIVEVIEW_STATUS = "liveviewStatus";
    public static final String TYPE_LIVEVIEW_ORIENTATION = "liveviewOrientation";
    public static final String TYPE_TAKE_PICTURE = "takePicture";
    public static final String TYPE_STORAGE_INFORMATION = "storageInformation";
    public static final String TYPE_BEEP_MODE = "beepMode";
    public static final String TYPE_CAMERA_FUNCTION = "cameraFunction";
    public static final String TYPE_MOVE_QUALITY = "movieQuality";
    public static final String TYPE_STILL_SIZE = "stillSize";
    public static final String TYPE_CAMERA_FUNCTION_RESULT = "cameraFunctionResult";
    public static final String TYPE_STEADY_MODE = "steadyMode";
    public static final String TYPE_VIEW_ANGLE = "viewAngle";
    public static final String TYPE_EXPOSURE_MODE = "exposureMode";
    public static final String TYPE_POSTVIEW_IMAGE_SIZE = "postviewImageSize";
    public static final String TYPE_SELF_TIMER = "selfTimer";
    public static final String TYPE_SHOOT_MODE = "shootMode";
    public static final String TYPE_EXPOSURE_COMPENSATION = "exposureCompensation";
    public static final String TYPE_FLASH_MODE = "flashMode";
    public static final String TYPE_F_NUMBER = "fNumber";
    public static final String TYPE_FOCUS_MODE = "focusMode";
    public static final String TYPE_ISO_SPEED_RATE = "isoSpeedRate";
    public static final String TYPE_PROGRAM_SHIFT = "programShift";
    public static final String TYPE_SHUTTER_SPEED = "shutterSpeed";
    public static final String TYPE_WHITE_BALANCE = "whiteBalance";
    public static final String TYPE_TOUCH_AF_POSITION = "touchAFPosition";

    private static Set<String> mShootingStatus = new HashSet<String>();

    static {
        mShootingStatus.add(STATUS_IDLE);
        mShootingStatus.add(STATUS_STILL_CAPTURING);
        mShootingStatus.add(STATUS_STILL_SAVING);
        mShootingStatus.add(STATUS_MOVIE_WAIT_REC_START);
        mShootingStatus.add(STATUS_MOVIE_RECORDING);
        mShootingStatus.add(STATUS_MOVIE_WAIT_REC_STOP);
        mShootingStatus.add(STATUS_MOVIE_SAVING);
        mShootingStatus.add(STATUS_INTERVAL_WAIT_REC_START);
        mShootingStatus.add(STATUS_INTERVAL_RECORDING);
        mShootingStatus.add(STATUS_INTERVAL_WAIT_REC_STOP);
        mShootingStatus.add(STATUS_AUDIO_WAIT_REC_START);
        mShootingStatus.add(STATUS_AUDIO_RECORDING);
        mShootingStatus.add(STATUS_AUDIO_WAIT_REC_STOP);
        mShootingStatus.add(STATUS_AUDIO_SAVING);
    }

    public static boolean isShootingStatus(String currentStatus) {
        return mShootingStatus.contains(currentStatus);
    }

    boolean mLiveviewStatus;

    String mShootMode;

    List<String> mShootModeList;

    int mZoomPosition;

    String mStorageId;

    String mCameraStatus;

    public static CameraCandidates getInstance() {
        if (mCameraCandidates == null) {
            synchronized (CameraCandidates.class) {
                if (mCameraCandidates == null) {
                    mCameraCandidates = new CameraCandidates();
                }
            }
        }
        return mCameraCandidates;
    }

    public static void clear() {
        if (mCameraCandidates != null) {
            synchronized (CameraCandidates.class) {
                if (mCameraCandidates != null) {
                    mCameraCandidates = null;
                }
            }
        }
    }

    public boolean getLiveviewStatus() {
        return mLiveviewStatus;
    }

    public String getShootMode() {
        return mShootMode;
    }

    public List<String> getShootModeList(){
        return mShootModeList;
    }

    public int getZoomPosition() {
        return mZoomPosition;
    }

    public String getStorageId() {
        return mStorageId;
    }

    public String getCameraStatus(){
        return mCameraStatus;
    }

    public List<String> getControlledList() {
        List<String> controlledList = new ArrayList<String>();

        return controlledList;
    }
}
