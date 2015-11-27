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
import com.chien.sony.cameraremote.api.RemoteApiHelper;
import com.chien.sony.cameraremote.utils.CameraCandidates;
import com.chien.sony.cameraremote.widget.recyclerView.RecyclerAdapter;

import java.util.List;

/**
 * Created by Chien on 2015/11/4.
 */
public class ShootModeChooseDialog extends RecyclerDialog {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void init() {
        final CameraApplication app = (CameraApplication) getActivity().getApplication();
        final CameraCandidates cameraCandidates = CameraCandidates.getInstance();
        addItem(cameraCandidates.ShootMode);

        setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerAdapter<?> adapter, View view, int position) {
                if (CameraCandidates.STATUS_IDLE.equals(cameraCandidates.getCameraStatus())) {
                    RemoteApiHelper.setShootMode(app.getRemoteApi(), (String) adapter.getItem(position));
                    dismiss();
                }
            }

        });
    }

}
