package com.example.mytraveldiary.utils.helpers;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Helper class for view animations
 * Demonstrates: Animations, Custom Views, UI Enhancement
 */
public class AnimationHelper {

    /**
     * Fade in animation
     */
    public static void fadeIn(View view, long duration) {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(duration);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Fade out animation
     */
    public static void fadeOut(View view, long duration) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(animation);
    }

    /**
     * Slide in from right
     */
    public static void slideInFromRight(View view, long duration) {
        TranslateAnimation animation = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        );
        animation.setDuration(duration);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Slide out to left
     */
    public static void slideOutToLeft(View view, long duration) {
        TranslateAnimation animation = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, -1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        );
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(animation);
    }

    /**
     * Scale animation (zoom in/out)
     */
    public static void scaleView(View view, float fromScale, float toScale, long duration) {
        ScaleAnimation animation = new ScaleAnimation(
            fromScale, toScale,
            fromScale, toScale,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    /**
     * Pulse animation (for attention)
     */
    public static void pulse(View view) {
        ScaleAnimation scaleUp = new ScaleAnimation(
            1.0f, 1.1f,
            1.0f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(200);

        ScaleAnimation scaleDown = new ScaleAnimation(
            1.1f, 1.0f,
            1.1f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleDown.setDuration(200);
        scaleDown.setStartOffset(200);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(scaleUp);
        animationSet.addAnimation(scaleDown);

        view.startAnimation(animationSet);
    }

    /**
     * Bounce animation for FAB
     */
    public static void bounce(View view) {
        ScaleAnimation animation = new ScaleAnimation(
            1.0f, 1.2f,
            1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        animation.setDuration(300);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(1);
        view.startAnimation(animation);
    }
}
