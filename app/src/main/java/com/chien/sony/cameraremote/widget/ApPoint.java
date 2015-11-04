
package com.chien.sony.cameraremote.widget;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class ApPoint implements Comparable<ApPoint>{

    public static final int INVALID_NETWORK_ID = -1;

    private final String BSSID, SSID, capabilities;

    private final int frequency, rssi;

    private int networkId = INVALID_NETWORK_ID;

    private String password = "";

    public static final int SECURITY_NONE = 0;

    public static final int SECURITY_WEP = 1;

    public static final int SECURITY_PSK = 2;

    public static final int SECURITY_EAP = 3;

    public enum PskType {
        UNKNOWN, WPA, WPA2, WPA_WPA2
    }

    public ApPoint(final Context context, final ScanResult scanResult) {

        BSSID = scanResult.BSSID;
        SSID = scanResult.SSID;
        capabilities = scanResult.capabilities;
        frequency = scanResult.frequency;
        rssi = scanResult.level;

    }

    public String getBSSID() {
        return BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setNetworkId(final int networkId) {
        this.networkId = networkId;
    }

    public int getNetworkId() {
        return networkId;
    };

    public int getLevel() {
        if (rssi == Integer.MAX_VALUE) {
            return 0;
        }
        final int level = WifiManager.calculateSignalLevel(rssi, 4) + 1;
        return level;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public int getSecurity() {
        if (capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public PskType getPskType() {
        final boolean wpa = capabilities.contains("WPA-PSK");
        final boolean wpa2 = capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            return PskType.UNKNOWN;
        }
    }

    @Override
    public int compareTo(final ApPoint obj) {
        if (!(obj instanceof ApPoint))
            return 1;

        final ApPoint other = (ApPoint)obj;

        if (rssi != Integer.MAX_VALUE && other.rssi == Integer.MAX_VALUE)
            return -1;
        if (rssi == Integer.MAX_VALUE && other.rssi != Integer.MAX_VALUE)
            return 1;

        if (networkId != INVALID_NETWORK_ID && other.networkId == INVALID_NETWORK_ID)
            return -1;
        if (networkId == INVALID_NETWORK_ID && other.networkId != INVALID_NETWORK_ID)
            return 1;

        // Sort by signal strength.
        final int difference = WifiManager.compareSignalLevel(other.rssi, rssi);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return SSID.compareToIgnoreCase(other.SSID);
    }

}
