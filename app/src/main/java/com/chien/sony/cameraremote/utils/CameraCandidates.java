package com.chien.sony.cameraremote.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jean.Gong on 2015/11/5.
 */
public class CameraCandidates {
    private static CameraCandidates mCameraCandidates;

    public List<String> ShootMode = new ArrayList<String>();

    public static CameraCandidates getInstance(){
        if(mCameraCandidates == null){
            synchronized (CameraCandidates.class){
                if(mCameraCandidates == null){
                    mCameraCandidates = new CameraCandidates();
                }
            }
        }
        return mCameraCandidates;
    }

    public static void clear(){
        if(mCameraCandidates != null){
            synchronized (CameraCandidates.class){
                if(mCameraCandidates != null){
                    mCameraCandidates = null;
                }
            }
        }
    }
}
