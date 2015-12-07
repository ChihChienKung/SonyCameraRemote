package com.chien.sony.cameraremote.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;

/**
 * Created by Chien on 2015/12/4.
 */
public class ViewAnimation {
    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final int SHOW_HIDE_ANIM_DURATION = 200;

    public static void hide(View view) {
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 14)
            hide_api_14(view);
        else
            hide_default(view);

    }

    public static void show(View view) {
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 14)
            show_api_14(view);
        else
            show_default(view);
    }

    @TargetApi(14)
    private static void hide_api_14(final View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }

        if (!ViewCompat.isLaidOut(view) || view.isInEditMode()) {
            view.setVisibility(View.GONE);
        } else {
            view.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            view.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @TargetApi(14)
    private static void show_api_14(final View view) {
        if (view.getVisibility() != View.VISIBLE) {
            if (ViewCompat.isLaidOut(view) && !view.isInEditMode()) {
                view.setAlpha(0f);
                view.setScaleY(0f);
                view.setScaleX(0f);
                view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(SHOW_HIDE_ANIM_DURATION)
                        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                view.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                            }
                        });
            } else {
                view.setVisibility(View.VISIBLE);
                view.setAlpha(1f);
                view.setScaleY(1f);
                view.setScaleX(1f);
            }
        }
    }

    private static void hide_default(final View view) {
        if (view.getVisibility() != View.VISIBLE) {

            return;
        }

        Animation anim = android.view.animation.AnimationUtils.loadAnimation(
                view.getContext(), android.support.design.R.anim.design_fab_out);
        anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        anim.setDuration(SHOW_HIDE_ANIM_DURATION);
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }
        });
        view.startAnimation(anim);
    }

    private static void show_default(final View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.clearAnimation();
            view.setVisibility(View.VISIBLE);
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(
                    view.getContext(), android.support.design.R.anim.design_fab_in);
            anim.setDuration(SHOW_HIDE_ANIM_DURATION);
            anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            view.startAnimation(anim);
        }
    }

}
