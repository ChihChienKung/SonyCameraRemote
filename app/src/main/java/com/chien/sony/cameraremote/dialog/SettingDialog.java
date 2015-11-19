
package com.chien.sony.cameraremote.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chien.sony.cameraremote.CameraEventObserver;
import com.chien.sony.cameraremote.R;
import com.chien.sony.cameraremote.utils.CameraCandidates;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chien on 2015/11/4.
 */
public class SettingDialog extends ListDialog {
    private final String TAG = getClass().getSimpleName();

    private ListDialog mShowDialog;

    @Override
    protected void init() {
        setAdapter(new ItemAdapter(getActivity()));

        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                ItemAdapter adapter = (ItemAdapter) getAdapter();
                String selected = adapter.getItem(position);
                if("ShootMode".equals(selected)){
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

    private static class ItemAdapter extends BaseAdapter {
        private final List<String> mItemList;

        private final LayoutInflater mInflater;

        public ItemAdapter(Context context) {
            CameraCandidates cameraCandidates = CameraCandidates.getInstance();

            mItemList = new ArrayList<String>();
            if (cameraCandidates.ShootMode.size() > 0) {
                mItemList.add("ShootMode");
            }

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
