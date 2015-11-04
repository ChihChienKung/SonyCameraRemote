/*
 * Copyright 2014 Sony Corporation
 */

package com.chien.sony.cameraremote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chien.sony.cameraremote.receiver.WifiBroadcastReceiver;
import com.chien.sony.cameraremote.widget.ApPoint;
import com.telly.mrvector.MrVector;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity class of Device Discovery screen.
 */
public class CameraConnectActivity extends AppCompatActivity {

    private static final String TAG = CameraConnectActivity.class.getSimpleName();

    private WifiManager mWifiManager;

    private WifiBroadcastReceiver mWifiReceiver;

    private SsdpClient mSsdpClient;

    private DeviceListAdapter mListAdapter;

    private ProgressBar mProgressBar;

    private Toolbar mToolbar;

    private ConnectTask mTask;

    private boolean mActivityActive, mWifiScanActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_camera);

        mProgressBar = (ProgressBar) findViewById(R.id.toolbar_progress);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        mSsdpClient = new SsdpClient();
        mListAdapter = new DeviceListAdapter(this);

        Log.d(TAG, "onCreate() completed.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityActive = true;

        wifiScan();
        ListView listView = (ListView) findViewById(R.id.list_device);
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mTask == null) {
                    synchronized (CameraConnectActivity.class) {
                        if (mTask == null) {
                            ApPoint apPoint = mListAdapter.getItem(position);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect, menu);
        if (Build.VERSION.SDK_INT < 21) {
            MenuItem item = menu.findItem(R.id.menu_refresh);
            item.setIcon(MrVector.inflate(getResources(), R.drawable.refresh));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        switch (item_id) {
            case R.id.menu_refresh:
                wifiScan();
                break;
        }
        return true;
    }

    private void progressBarVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (visibility != mProgressBar.getVisibility())
            mProgressBar.setVisibility(visibility);
    }

    private void menuRefreshVisibility(boolean visible) {
        MenuItem item = mToolbar.getMenu().findItem(R.id.menu_refresh);

        if (item != null && visible != item.isVisible())
            item.setVisible(visible);

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
        progressBarVisibility(scannable);
        menuRefreshVisibility(!scannable);
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

    /**
     * Launch a SampleCameraActivity.
     *
     * @param device
     */
    private void launchCameraActivity(ServerDevice device) {
        Log.d(TAG, "connect to " + device.getFriendlyName());

        // Set target ServerDevice instance to control in Activity.
        CameraApplication app = (CameraApplication) getApplication();
        app.setTargetServerDevice(device);
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Adapter class for DeviceList
     */
    private static class DeviceListAdapter extends BaseAdapter {

        private final List<ApPoint> mApPointList;

        private final LayoutInflater mInflater;

        public DeviceListAdapter(Context context) {
            mApPointList = new ArrayList<ApPoint>();
            mInflater = LayoutInflater.from(context);
        }

        public void addDevice(ApPoint apPoint) {
            mApPointList.add(apPoint);
            notifyDataSetChanged();
        }

        public void addDevice(List<ApPoint> apPoints) {
            mApPointList.clear();
            for (final ApPoint apPoint : apPoints)
                mApPointList.add(apPoint);
            notifyDataSetChanged();
        }

        public void clearDevices() {
            mApPointList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mApPointList.size();
        }

        @Override
        public ApPoint getItem(int position) {
            return mApPointList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0; // not fine
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = (TextView) convertView;
            if (textView == null) {
                textView = (TextView) mInflater.inflate(R.layout.device_list_item, parent, false);
            }
            ApPoint apPoint = getItem(position);
            textView.setText(apPoint.getSSID());
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

            return textView;
        }
    }

    private final WifiBroadcastReceiver.OnWifiScanListener mOnWifiScanListener = new WifiBroadcastReceiver.OnWifiScanListener() {

        @Override
        public void scanResults(final List<ApPoint> apPoints) {
            mListAdapter.addDevice(apPoints);
        }

    };

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
