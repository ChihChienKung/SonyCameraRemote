package com.chien.sony.cameraremote.utils;

import java.util.ArrayList;
import java.util.List;

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

    public static final String SHOOT_MODE = "shootMode";

    String mCameraStatus;

    public List<String> ShootMode = new ArrayList<String>();

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

//    void setCameraCandidates(String cameraStatus){
//        mCameraStatus = cameraStatus;
//    }

    public String getCameraStatus(){
        return mCameraStatus;
    }

    public List<String> getControlledList() {
        List<String> controlledList = new ArrayList<String>();
        if (ShootMode.size() > 1)
            controlledList.add(SHOOT_MODE);

        return controlledList;
    }
}
