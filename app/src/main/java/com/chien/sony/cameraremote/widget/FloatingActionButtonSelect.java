package com.chien.sony.cameraremote.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chien.sony.cameraremote.CameraApplication;
import com.chien.sony.cameraremote.R;
import com.chien.sony.cameraremote.utils.ImageDrawableUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jean.Gong on 2015/11/30.
 */
public class FloatingActionButtonSelect extends RelativeLayout {
    private static final String TAG = FloatingActionButtonSelect.class.getSimpleName();

    private RelativeLayout mChildFrame;

    private FloatingActionButton mMain;

    private Map<String, Offset> mOffsetList = new HashMap<String, Offset>();

    private boolean mIsExpand = false;

    private int mDeviceWidth, mDeviceHeight, mFabMargin;

    public FloatingActionButtonSelect(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contextView = layoutInflater.inflate(R.layout.widget_floating_action_button_select, this, true);
        mChildFrame = (RelativeLayout) contextView.findViewById(R.id.floating_action_button_group_child_frame);
        mMain = (FloatingActionButton) contextView.findViewById(R.id.floating_action_button_group_main);

        mChildFrame.setOnTouchListener(mOnTouchListener);
        mMain.setOnClickListener(mOnClickListener);

        mFabMargin = getResources().getDimensionPixelOffset(R.dimen.codelab_fab_margin);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDeviceWidth = dm.widthPixels;
        mDeviceHeight = dm.heightPixels;

        mChildFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16)
                    mChildFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mChildFrame.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mChildFrame.post(new Runnable() {
                    @Override
                    public void run() {
                        mChildFrame.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void addChild(String tag, int drawableResId, OnClickListener action) {
        FloatingActionButton fab = (FloatingActionButton) LayoutInflater.from(getContext()).inflate(R.layout.widget_mini_floating_cation_button, null);
        fab.setId(drawableResId);
        fab.setOnClickListener(action);
        ImageDrawableUtil.setImageDrawable((CameraApplication) getContext().getApplicationContext(), fab, drawableResId);
        fab.setTag(tag);
        mChildFrame.addView(fab);
    }

    public void removeChild(String tag) {
        View v = mChildFrame.findViewWithTag(tag);
        mChildFrame.removeView(v);
    }

    private void collapseFab() {
        AnimatorSet animatorSet = new AnimatorSet();

        int count = mChildFrame.getChildCount();
        Animator[] animators = new Animator[count * 4];
        for (int i = 0; i < count; i++) {
            View v = mChildFrame.getChildAt(i);
            Offset offset = mOffsetList.get(v.getTag());
            animators[i] = collapseTranslationAnimator(v, -offset.y);
            animators[i + count] = collapseTranslationXAnimator(v, offset.x);
            animators[i + count * 2] = collapseScaleXAnimator(v);
            animators[i + count * 3] = collapseScaleYAnimator(v);
        }

        animatorSet.playTogether(animators);
        animatorSet.addListener(mAnimatorListener);
        animatorSet.start();
    }

    private void expandFab() {
        AnimatorSet animatorSet = new AnimatorSet();

        int count = mChildFrame.getChildCount();
        Animator[] animators = new Animator[count * 4];
        for (int i = 0; i < count; i++) {
            View v = mChildFrame.getChildAt(i);
            int height = v.getMeasuredHeight();
            int width = v.getMeasuredWidth();
            Offset offset = new Offset();
            if (i == 0) {
                offset.x = mFabMargin;
                offset.y = mMain.getMeasuredHeight() + mFabMargin;
            } else {
//                ((LinearLayout.LayoutParams) v.getLayoutParams()).topMargin

                Offset previousOffset = mOffsetList.get(mChildFrame.getChildAt(i - 1).getTag());
                if (previousOffset.y + height > mDeviceHeight - height) {
                    offset.x = previousOffset.x + width + mFabMargin;
                    offset.y = mMain.getMeasuredHeight() + mFabMargin;
                } else {
                    offset.x = previousOffset.x;
                    offset.y = previousOffset.y + height + mFabMargin;
                }
            }
            mOffsetList.put(v.getTag().toString(), offset);

            animators[i] = expandTranslationAnimator(v, -offset.y);
            animators[i + count] = expandTranslationXAnimator(v, offset.x);
            animators[i + count * 2] = expandScaleXAnimator(v);
            animators[i + count * 3] = expandScaleYAnimator(v);
        }

        animatorSet.playTogether(animators);
        animatorSet.start();
    }


    private Animator collapseTranslationAnimator(View view, float offset) {
        if (Build.VERSION.SDK_INT >= 14)
            return ObjectAnimator.ofFloat(view, TRANSLATION_Y, offset, 0)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        else
            return ObjectAnimator.ofFloat(view, "translationY", offset, 0)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator collapseTranslationXAnimator(View view, float offset) {
        if (Build.VERSION.SDK_INT >= 14)
            return ObjectAnimator.ofFloat(view, TRANSLATION_X, offset, 0)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        else
            return ObjectAnimator.ofFloat(view, "translationX", offset, 0)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator collapseScaleXAnimator(View view) {
        if (Build.VERSION.SDK_INT >= 14)
            return ObjectAnimator.ofFloat(view, SCALE_X, 0f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        else
            return ObjectAnimator.ofFloat(view, "scaleX", 0f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator collapseScaleYAnimator(View view) {
        if (Build.VERSION.SDK_INT >= 14)
            return ObjectAnimator.ofFloat(view, SCALE_Y, 0f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        else
            return ObjectAnimator.ofFloat(view, "scaleY", 0f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }


    private Animator expandTranslationAnimator(View view, float offset) {
        if (Build.VERSION.SDK_INT >= 14)
            return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, offset)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        else
            return ObjectAnimator.ofFloat(view, "translationY", 0, offset)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator expandTranslationXAnimator(View view, float offset) {
        if (Build.VERSION.SDK_INT >= 14)
            return ObjectAnimator.ofFloat(view, TRANSLATION_X, 0, offset)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        else
            return ObjectAnimator.ofFloat(view, "translationX", 0, offset)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator expandScaleXAnimator(View view) {
        if (Build.VERSION.SDK_INT >= 14)
            return ObjectAnimator.ofFloat(view, SCALE_X, 1f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        else
            return ObjectAnimator.ofFloat(view, "scaleX", 1f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator expandScaleYAnimator(View view) {
        if (Build.VERSION.SDK_INT >= 14)
            return ObjectAnimator.ofFloat(view, SCALE_Y, 1f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        else
            return ObjectAnimator.ofFloat(view, "scaleY", 1f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private final OnTouchListener mOnTouchListener = new OnTouchListener() {
        private long downTime;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                downTime = System.currentTimeMillis();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && mIsExpand && System.currentTimeMillis() - downTime < 500) {
                mMain.performClick();
            }
            return true;
        }
    };

    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mChildFrame.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mIsExpand) {
                synchronized (FloatingActionButtonSelect.this) {
                    if (mIsExpand) {
                        collapseFab();
                        mIsExpand = false;
                    }
                }
            } else {
                synchronized (FloatingActionButtonSelect.this) {
                    if (!mIsExpand) {
                        mChildFrame.setVisibility(VISIBLE);
                        expandFab();
                        mIsExpand = true;
                    }
                }
            }
        }
    };

    private class Offset {
        float x, y;
    }
}
