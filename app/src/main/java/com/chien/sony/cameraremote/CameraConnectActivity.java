/*
 * Copyright 2014 Sony Corporation
 */

package com.chien.sony.cameraremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.chien.sony.cameraremote.receiver.WifiBroadcastReceiver;
import com.chien.sony.cameraremote.utils.ImageDrawableUtil;
import com.chien.sony.cameraremote.widget.ApPoint;
import com.chien.sony.cameraremote.widget.FloatingActionButtonSelect;
import com.chien.sony.cameraremote.widget.recyclerView.RecyclerStringItem;
import com.chien.sony.cameraremote.widget.recyclerView.RecyclerAdapter;
import com.chien.sony.cameraremote.widget.recyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity class of Device Discovery screen.
 */
public class CameraConnectActivity extends Activity {

    private static final String TAG = CameraConnectActivity.class.getSimpleName();

    private WifiManager mWifiManager;

    private WifiBroadcastReceiver mWifiReceiver;

    private SsdpClient mSsdpClient;

    private RecyclerView mDeviceList;

    private DeviceListAdapter mListAdapter;

    private FloatingActionButton mRefresh;

    private ConnectTask mTask;

    private boolean mActivityActive, mWifiScanActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_camera);

        mDeviceList = (RecyclerView) findViewById(R.id.device_list);
        mRefresh = (FloatingActionButton) findViewById(R.id.btn_refresh);

        mSsdpClient = new SsdpClient();

        loading(false);
        Log.d(TAG, "onCreate() completed.");

        mDeviceList.setLayoutManager(new LinearLayoutManager(this));

        mRefresh.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityActive = true;

        wifiScan();

        mListAdapter = new DeviceListAdapter(this);
        mDeviceList.setAdapter(mListAdapter);
        mListAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerAdapter<?> adapter, View view, int position) {
                if (mTask == null) {
                    synchronized (CameraConnectActivity.class) {
                        if (mTask == null) {
                            ApPoint apPoint = (ApPoint) adapter.getItem(position);
                            Log.v(TAG, "connect " + apPoint.getSSID());
                            mTask = new ConnectTask(apPoint, position);
                            mTask.execute();
                        }
                    }
                }
            }
        });
        //TODO do some thing.
        Log.d(TAG, "onResume() completed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityActive = false;

        wifiScannable(false);
        if (mSsdpClient != null && mSsdpClient.isSearching()) {
            mSsdpClient.cancelSearching();
        }

        Log.d(TAG, "onPause() completed.");
    }

    private void loading(boolean enable) {
        if (enable) {
            mRefresh.setImageResource(android.R.drawable.ic_popup_sync);
            AnimationDrawable animationDrawable = (AnimationDrawable) mRefresh.getDrawable();
            animationDrawable.start();
        } else {
            CameraApplication application = (CameraApplication) getApplication();
            ImageDrawableUtil.setImageDrawable(application, mRefresh, R.drawable.ic_refresh);
        }

    }

    private void wifiScan() {
        if (mWifiScanActive)
            return;
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        if (mWifiReceiver == null)
            mWifiReceiver = new WifiBroadcastReceiver(mWifiManager, mOnWifiScanListener);

        wifiScannable(true);
        mWifiManager.startScan();

        new Thread() {

            @Override
            public void run() {
                super.run();
                try {
                    synchronized (this) {
                        this.wait(15000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wifiScannable(false);
                    }
                });
            }
        }.start();
    }

    private synchronized void wifiScannable(boolean scannable) {
        if (mWifiScanActive == scannable)
            return;
        mWifiScanActive = scannable;
        loading(scannable);
        if (scannable)
            registerReceiver();
        else
            unregisterReceiver();
    }

    private void registerReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(mWifiReceiver);
    }

    /**
     * Start searching supported devices.
     */
    private void searchDevices(final int position) {
        SsdpClient.SearchResultHandler handler = new SsdpClient.SearchResultHandler() {

            @Override
            public void onDeviceFound(final ServerDevice device) {
                // Called by non-UI thread.
                Log.d(TAG, ">> Search device found: " + device.getFriendlyName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        launchCameraActivity(device);
                    }
                });
            }

            @Override
            public void onFinished() {
                // Called by non-UI thread.
                Log.d(TAG, ">> Search finished.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActivityActive) {
                            Toast.makeText(CameraConnectActivity.this, //
                                    R.string.msg_device_search_finish, //
                                    Toast.LENGTH_SHORT).show(); //
                        }
                    }
                });
            }

            @Override
            public void onErrorFinished() {
                // Called by non-UI thread.
                Log.d(TAG, ">> Search Error finished.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActivityActive) {
                            Toast.makeText(CameraConnectActivity.this, //
                                    R.string.msg_error_device_searching, //
                                    Toast.LENGTH_SHORT).show(); //
                        }
                    }
                });
            }
        };

        mSsdpClient.search(handler);
    }

    private void connectSuccess(int position) {
        Log.i(TAG, "connectSuccess");
        searchDevices(position);
    }

    private void connectFail() {
        Log.i(TAG, "connectFail");
        Toast.makeText(this, R.string.wifi_connect_fail, Toast.LENGTH_SHORT).show();
    }

    private WifiConfiguration getWifiConfiguration(final ApPoint apPoint, final String password) {
        final WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + apPoint.getSSID() + "\"";
        Log.w(TAG, "apPoint.getSecurity()=" + apPoint.getSecurity());
        switch (apPoint.getSecurity()) {
            case ApPoint.SECURITY_NONE:
                incompetence(config);
                break;
            case ApPoint.SECURITY_WEP:
                cipherWEP(config, password);
                break;
            case ApPoint.SECURITY_PSK:
                config.BSSID = apPoint.getBSSID();
                cipherPSK(config, password);
                break;
        }
        return config;
    }

    private void incompetence(final WifiConfiguration config) {
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    }

    private void cipherWEP(final WifiConfiguration config, final String password) {
        if (password.length() != 0) {
            final int length = password.length();
            if ((length == 10 || length == 26 || length == 58) && password.matches("[0-9A-Fa-f]*")) {
                config.wepKeys[0] = password;
            } else {
                config.wepKeys[0] = "\"" + password + "\"";
            }
        }
        config.allowedKeyManagement.set(KeyMgmt.NONE);
        config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
        config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
    }

    private void cipherPSK(final WifiConfiguration config, final String password) {
        if (password.length() != 0) {
            if (password.matches("[0-9A-Fa-f]{64}")) {
                config.preSharedKey = password;
            } else {
                config.preSharedKey = "\"" + password + "\"";
            }
        }
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
    }

    private void taskNotify() {
        synchronized (mTask) {
            mTask.notify();
        }
    }

    private void taskWait(final long millis) {
        try {
            synchronized (mTask) {
                if (millis <= 0)
                    mTask.wait();
                else
                    mTask.wait(millis);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void launchCameraActivity(ServerDevice device) {
        Log.d(TAG, "connect to " + device.getFriendlyName());

        CameraApplication app = (CameraApplication) getApplication();
        app.setTargetServerDevice(device);
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            wifiScan();
        }
    };

    private final WifiBroadcastReceiver.OnWifiScanListener mOnWifiScanListener = new WifiBroadcastReceiver.OnWifiScanListener() {

        @Override
        public void scanResults(final List<ApPoint> apPoints) {
            mListAdapter.addDevice(apPoints);
        }

    };

    private static class DeviceListAdapter extends RecyclerAdapter<ViewHolder> {

        private final List<ApPoint> mApPointList;

        private final Context mContext;

        public DeviceListAdapter(Context context) {
            mApPointList = new ArrayList<ApPoint>();
            mContext = context;
        }

        public void addDevice(ApPoint apPoint) {
            mApPointList.add(apPoint);
            notifyItemInserted(getItemCount());
        }

        public void addDevice(List<ApPoint> apPoints) {
//            mApPointList.clear();
            clearDevices();
            mApPointList.addAll(apPoints);
            notifyItemRangeInserted(0, apPoints.size());
        }

        public void clearDevices() {
            mApPointList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mApPointList.size();
        }

        @Override
        public ApPoint getItem(int position) {
            return mApPointList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(new RecyclerStringItem(mContext));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            super.onBindViewHolder(viewHolder, position);
            RecyclerStringItem item = (RecyclerStringItem) viewHolder.itemView;
            ApPoint apPoint = getItem(position);
            item.setText(apPoint.getSSID());
//            ServerDevice device =  (ServerDevice)getItem(position);
//            ServerDevice.ApiService apiService = device.getApiService("camera");
//            String endpointUrl = null;
//            if (apiService != null) {
//                endpointUrl = apiService.getEndpointUrl();
//            }
//
//            // Label
//            String htmlLabel =
//                    String.format("%s ", device.getFriendlyName()) //
//                            + String.format(//
//                            "<br><small>Endpoint URL:  <font color=\"blue\">%s</font></small>", //
//                            endpointUrl);
//            textView.setText(Html.fromHtml(htmlLabel));
        }

    }

    private class ConnectTask extends AsyncTask<Void, Integer, String> {
        private final ApPoint mApPoint;

        private String mPassword;

        private int mPosition;

        public ConnectTask(final ApPoint apPointPreference, int position) {
            mApPoint = apPointPreference;
            mPosition = position;
        }

        @Override
        protected String doInBackground(final Void... v) {
            final String SSID = mApPoint.getSSID();
            int networkId = mApPoint.getNetworkId();
            if (networkId == -1) {
                checkPassword();
                if (mPassword == null)
                    return null;
                networkId = mWifiManager.addNetwork(getWifiConfiguration(mApPoint, mPassword));
                Log.e(TAG, "addNetwork=" + networkId);
                publishProgress(networkId);
            }

            final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            final int connectId = wifiInfo.getNetworkId();
            Log.e(TAG, "connectId=" + connectId);

            boolean isConnect = false;
            if (connectId != -1 && connectId != networkId) {
                mWifiManager.disableNetwork(connectId);
                mWifiManager.disconnect();
                taskWait(1500);
                mWifiManager.enableNetwork(networkId, true);
                mWifiManager.saveConfiguration();
                isConnect = mWifiManager.reconnect();
            } else if (connectId != -1 && connectId == networkId) {
                return SSID;
            } else if (connectId == -1 && connectId != networkId) {
                mWifiManager.enableNetwork(networkId, true);
                mWifiManager.saveConfiguration();
                isConnect = mWifiManager.reconnect();
            }

            Log.e(TAG, "networkId=" + networkId);
            if (!isConnect)
                return null;
            taskWait(2500);
            return SSID;
        }

        @Override
        protected void onProgressUpdate(final Integer... networkIds) {
            super.onProgressUpdate(networkIds);
            if (networkIds.length == 0) {
                showDialog();
                return;
            }
            mApPoint.setNetworkId(networkIds[0]);
        }

        @Override
        protected void onPostExecute(final String connectSSID) {
            super.onPostExecute(connectSSID);
            mTask = null;

            final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            Log.e(TAG, wifiInfo.getSSID() + " " + connectSSID);
            if (connectSSID != null && wifiInfo.getSSID() != null && wifiInfo.getSSID().equals(connectSSID)) {
                connectSuccess(mPosition);
            } else {
                connectFail();
            }
        }

        private void checkPassword() {
            switch (mApPoint.getSecurity()) {
                case ApPoint.SECURITY_WEP:
                case ApPoint.SECURITY_PSK:
                    publishProgress();
                    taskWait(0);
                    break;
            }
        }

        private void showDialog() {
            final EditText editText = new EditText(CameraConnectActivity.this);
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            final AlertDialog.Builder dialog = new AlertDialog.Builder(CameraConnectActivity.this);
            dialog.setTitle(R.string.input_ap_password);
            dialog.setView(editText);
            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    mPassword = editText.getText().toString();
                    taskNotify();
                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(final DialogInterface dialog) {
                    taskNotify();
                }
            });
            dialog.show();
        }
    }
}
