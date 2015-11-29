package com.chien.sony.cameraremote.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chien.sony.cameraremote.R;
import com.chien.sony.cameraremote.widget.recyclerView.RecyclerStringItem;
import com.chien.sony.cameraremote.widget.recyclerView.RecyclerAdapter;
import com.chien.sony.cameraremote.widget.recyclerView.ViewHolder;
import com.chien.sony.cameraremote.widget.recyclerView.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 2015/11/4.
 */
public abstract class RecyclerDialog extends DialogFragment {

    private RecyclerView mRecyclerView;

    private ItemAdapter mAdapter;

    private DialogInterface.OnDismissListener mOnDismissListener;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);
//        dialog.setCanceledOnTouchOutside(false);
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_list, null);
        init(view);
        dialog.setContentView(view);
        return dialog;
    }

    protected abstract void init();

    private void init(final View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.dialog_recycler_view);
        mAdapter = new ItemAdapter(getActivity());

        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(getOnItemClickListener());

        init();
    }

    public void addItem(List<String> itemList) {
        mAdapter.addItem(itemList);
    }

    public void addItem(String item) {
        mAdapter.addItem(item);
    }

    public void remove(String item) {
        mAdapter.remove(item);
    }

    public abstract RecyclerAdapter.OnItemClickListener getOnItemClickListener();

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null)
            mOnDismissListener.onDismiss(dialog);
    }

    private static class ItemAdapter extends RecyclerAdapter {

        private final List<String> mItemList = new ArrayList<String>();

        private final Context mContext;

        public ItemAdapter(Context context) {
            mContext = context;
        }

        public void addItem(List<String> itemList) {
            int oldCount = getItemCount();
            for (String s : itemList)
                if (!mItemList.contains(s))
                    mItemList.add(s);
//            notifyItemRangeInserted(oldCount, itemList.size());
            notifyDataSetChanged();
        }

        public void addItem(String item) {
            if (!mItemList.contains(item))
                mItemList.add(item);
            notifyItemInserted(getItemCount() -1);
        }

        public void remove(String item) {
            if (mItemList.contains(item))
                mItemList.remove(item);
            notifyItemRemoved(getItemCount());
        }

        @Override
        public int getItemCount() {
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
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(new RecyclerStringItem(mContext));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            RecyclerStringItem item = (RecyclerStringItem) holder.itemView;
            String text = getItem(position);
            item.setText(text);
        }

    }
}
