
package com.chien.sony.cameraremote.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chien.sony.cameraremote.R;
import com.chien.sony.cameraremote.utils.CameraCandidates;
import com.chien.sony.cameraremote.widget.recyclerView.RecyclerAdapter;
import com.chien.sony.cameraremote.widget.recyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chien on 2015/11/4.
 */
public class SettingDialog extends RecyclerDialog {
    private final String TAG = getClass().getSimpleName();

    private RecyclerDialog mShowDialog;

    @Override
    protected void init() {
        addItem("ShootMode");

        setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerAdapter<?> adapter, View view, int position) {
                String selected = (String) adapter.getItem(position);
                if ("ShootMode".equals(selected)) {
                    if (!isDialogExist()) {
                        mShowDialog = new ShootModeChooseDialog();
                        showDialog();
                    }
                }
            }

        });
    }

    private boolean isDialogExist() {
        if (mShowDialog == null) {
            synchronized (SettingDialog.class) {
                if (mShowDialog == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showDialog() {
        mShowDialog.setOnDismissListener(mOnDismissListener);
        mShowDialog.show(getFragmentManager(), null);
    }

    private DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            if (mShowDialog != null) {
                synchronized (SettingDialog.class) {
                    if (mShowDialog != null) {
                        mShowDialog = null;
                    }
                }
            }
        }
    };


}
