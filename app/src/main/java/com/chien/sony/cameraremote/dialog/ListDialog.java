package com.chien.sony.cameraremote.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.chien.sony.cameraremote.R;

/**
 * Created by apple on 2015/11/4.
 */
public abstract class ListDialog extends DialogFragment {

    private ListView mListView;

    private ListAdapter mAdapter;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    private DialogInterface.OnDismissListener mOnDismissListener;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);
//        dialog.setCanceledOnTouchOutside(false);
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_list, null);
        init(view);
        dialog.setContentView(view);
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    protected abstract void init();

    private void init(final View view) {
        mListView = (ListView) view.findViewById(R.id.dialog_list_view);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mOnItemClickListener);

        init();
    }

    public void setAdapter(ListAdapter adapter) {
        mAdapter = adapter;
        if (mListView != null)
            mListView.setAdapter(adapter);
    }

    public ListAdapter getAdapter() {
        return mAdapter;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
        if (mListView != null)
            mListView.setOnItemClickListener(listener);
    }

    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null)
            mOnDismissListener.onDismiss(dialog);
    }
}
