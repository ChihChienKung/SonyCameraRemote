
package com.chien.sony.cameraremote.receiver;

import com.chien.sony.cameraremote.widget.ApPoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jean.Gong on 2015/10/29.
 */
public class WifiBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = getClass().getSimpleName();

    private final WifiManager mWifiManager;

    private final static String SONY_CAMERA = "DIRECT-";

    private final OnWifiScanListener mOnWifiScanListener;

    List<ApPoint> mApPointList = new ArrayList<ApPoint>();

    public WifiBroadcastReceiver(final WifiManager wifiManager, final OnWifiScanListener onWifiScanListener) {
        mWifiManager = wifiManager;
        mOnWifiScanListener = onWifiScanListener;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            updateAccessPoints(context);
            // Log.e(TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            // Log.e(TAG, "SUPPLICANT_STATE_CHANGED_ACTION");
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            // Log.e(TAG, "NETWORK_STATE_CHANGED_ACTION");
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            // Log.e(TAG, "RSSI_CHANGED_ACTION");
            updateAccessPoints(context);
        }

        // final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        // if (wifiInfo == null)
        // Log.w(TAG, "wifiInfo == null");
        // else
        // Log.w(TAG, wifiInfo.getSSID() + " !");
    }

    private void updateAccessPoints(final Context context) {
        mApPointList.clear();
        final List<ScanResult> wifiList = mWifiManager.getScanResults();
        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();

        // Log.e(TAG, wifiList.toString());
        // Log.e(TAG, configs.toString());
        final String currentSSID = mWifiManager.getConnectionInfo().getSSID();
        for (final ScanResult scanResult : wifiList) {
            final String saveSSID = "\"" + scanResult.SSID + "\"";
//            if (!saveSSID.contains(SONY_CAMERA))
//                continue;

            final ApPoint ap = new ApPoint(context, scanResult);

            for (final WifiConfiguration config : configs) {
                if (config.SSID.equals(saveSSID)) {
                    ap.setNetworkId(config.networkId);
                    if (scanResult.SSID.equals(currentSSID))
//                        ap.setSummary("已連線");
                    break;
                }
            }
            mApPointList.add(ap);
        }

        Collections.sort(mApPointList);

        mOnWifiScanListener.scanResults(mApPointList);
    }

    public interface OnWifiScanListener {
        public void scanResults(List<ApPoint> apPointList);
    }
}
