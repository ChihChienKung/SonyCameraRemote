package com.chien.sony.cameraremote.api;

/**
 * Created by Jean.Gong on 2015/12/9.
 */
public interface IApiResultListener {
    public void setShootModeResult(int resultCode, int id);
    public void getShootModeResult(String currentShootMode, int id);
    public void getSupportedShootModeResult(String[] supportShootMode, int id);
    public void getAvailableShootModeResult(String currentShootMode, String[] supportShootMode, int id);
    public void actTakePictureResult(String[] urls, int id);
    public void awaitTakePictureResult(String[] urls, int id);
    public void startContShootingResult(int id);
    public void stopContShootingResult(int id);
}
