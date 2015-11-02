/*
 * Copyright 2014 Sony Corporation
 */

package com.chien.sony.cameraremote;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chien.sony.cameraremote.receiver.WifiBroadcastReceiver;
import com.chien.sony.cameraremote.widget.Device;
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
                ListView listView = (ListView) parent;
                ServerDevice device = (ServerDevice) listView.getAdapter().getItem(position);
                launchCameraActivity(device);
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
    private void searchDevices() {
        mListAdapter.clearDevices();
        setProgressBarIndeterminateVisibility(true);
        mSsdpClient.search(new SsdpClient.SearchResultHandler() {

            @Override
            public void onDeviceFound(final ServerDevice device) {
                // Called by non-UI thread.
                Log.d(TAG, ">> Search device found: " + device.getFriendlyName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        mListAdapter.addDevice(device);
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
                        setProgressBarIndeterminateVisibility(false);
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
                        setProgressBarIndeterminateVisibility(false);
                        if (mActivityActive) {
                            Toast.makeText(CameraConnectActivity.this, //
                                    R.string.msg_error_device_searching, //
                                    Toast.LENGTH_SHORT).show(); //
                        }
                    }
                });
            }
        });
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
    }

    /**
     * Adapter class for DeviceList
     */
    private static class DeviceListAdapter extends BaseAdapter {

        private final List<Device> mDeviceList;

        private final LayoutInflater mInflater;

        public DeviceListAdapter(Context context) {
            mDeviceList = new ArrayList<Device>();
            mInflater = LayoutInflater.from(context);
        }

        public void addDevice(Device device) {
            mDeviceList.add(device);
            notifyDataSetChanged();
        }

        public void addDevice(List<Device> devices) {
            mDeviceList.clear();
            for (final Device device : devices)
                mDeviceList.add(device);
            notifyDataSetChanged();
        }

        public void clearDevices() {
            mDeviceList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Device getItem(int position) {
            return mDeviceList.get(position);
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
            Device device = getItem(position);
            textView.setText(device.getSSID());
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
        public void scanResults(final List<Device> devices) {
            mListAdapter.addDevice(devices);
        }

    };
}
