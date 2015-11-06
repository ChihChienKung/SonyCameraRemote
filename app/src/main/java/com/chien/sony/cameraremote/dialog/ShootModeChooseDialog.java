package com.chien.sony.cameraremote.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chien.sony.cameraremote.CameraApplication;
import com.chien.sony.cameraremote.R;
import com.chien.sony.cameraremote.api.RemoteApi;
import com.chien.sony.cameraremote.api.RemoteApiHelper;
import com.chien.sony.cameraremote.utils.CameraCandidates;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chien on 2015/11/4.
 */
public class ShootModeChooseDialog extends ListDialog {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void init() {
        final CameraApplication app = (CameraApplication) getActivity().getApplication();

        setAdapter(new ModeAdapter(getActivity()));

        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ModeAdapter adapter = (ModeAdapter) getAdapter();
                RemoteApiHelper.setShootMode(app.getRemoteApi(), adapter.getItem(position));
            }
        });
    }

    private static class ModeAdapter extends BaseAdapter {
        private final List<String> mItemList;

        private final LayoutInflater mInflater;

        public ModeAdapter(Context context) {
            CameraCandidates cameraCandidates = CameraCandidates.getInstance();
            mItemList = cameraCandidates.ShootMode;

            mInflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public String getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = (TextView) convertView;
            if (textView == null) {
                textView = (TextView) mInflater.inflate(R.layout.device_list_item, parent, false);
            }
            textView.setText(getItem(position));
            return textView;
        }
    }
}
