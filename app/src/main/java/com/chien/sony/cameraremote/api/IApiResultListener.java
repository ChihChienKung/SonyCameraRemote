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
    public void startMovieRecResult(int id);
    public void stopMovieRecResult(int id);
    public void startAudioRecResult(int resultCode, int id);
    public void stopAudioRecResult(int resultCode, int id);
    public void startIntervalStillRecResult(int resultCode, int id);
    public void stopIntervalStillRecResult(int resultCode, int id);
    public void startLoopRecResult(int id);
    public void stopLoopRecResult(int id);
    public void startLiveviewResult(String url, int id);
    public void stopLiveviewResult(int resultCode, int id);
    public void startLiveviewWithSizeResult(String url, int id);
    public void getLiveviewSizeResult(String liveviewSize, int id);
    public void getSupportedLiveviewSizeResult(String[] supportLiveviewSize, int id);
    public void getAvailableLiveviewSizeResult(String currentLiveviewSize, String[] supportLiveviewSize, int id);
}
