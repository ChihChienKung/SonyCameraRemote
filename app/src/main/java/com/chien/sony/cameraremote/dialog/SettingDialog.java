
package com.chien.sony.cameraremote.dialog;

import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import com.chien.sony.cameraremote.utils.CameraCandidates;
import com.chien.sony.cameraremote.widget.recyclerView.RecyclerAdapter;

/**
 * Created by Chien on 2015/11/4.
 */
public class SettingDialog extends RecyclerDialog {
    private final String TAG = getClass().getSimpleName();

    private RecyclerDialog mShowDialog;

    @Override
    protected void init() {
        CameraCandidates cameraCandidates = CameraCandidates.getInstance();
        addItem(cameraCandidates.getControlledList());
    }

    @Override
    public RecyclerAdapter.OnItemClickListener getOnItemClickListener() {
        return new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerAdapter<?> adapter, View view, int position) {
                String selected = (String) adapter.getItem(position);
                Log.e(TAG, CameraCandidates.SHOOT_MODE+"  --  "+selected);
                if (CameraCandidates.SHOOT_MODE.equals(selected)) {
                    if (!isDialogExist()) {
                        mShowDialog = new ShootModeChooseDialog();
                        showDialog();
                    }
                }
            }

        };
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
