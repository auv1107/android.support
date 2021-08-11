package com.antiless.support.business.animxml

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation

/**
 *
 *
 * @author lixindong
 */

fun applyThreeBodyAnimation(topView: View, middleView: View, bottomView: View) {
    val rotateAnimation = getRotateAnimation()
    val scaleAnimation = getScaleAnimation()
    val alphaAnimation = getAlphaAnimation()

    val topAnimationSet = AnimationSet(true).apply {
        interpolator = LinearInterpolator()
        addAnimation(rotateAnimation)
    }
    val middleAnimationSet = AnimationSet(true).apply {
        interpolator = LinearInterpolator()
        addAnimation(rotateAnimation)
        addAnimation(scaleAnimation)
    }
    val bottomAnimationSet = AnimationSet(true).apply {
        interpolator = LinearInterpolator()
        addAnimation(rotateAnimation)
        addAnimation(scaleAnimation)
        addAnimation(alphaAnimation)
    }
    topView.startAnimation(topAnimationSet)
    middleView.startAnimation(middleAnimationSet)
    bottomView.startAnimation(bottomAnimationSet)
}

private fun getRotateAnimation(): RotateAnimation {
    return RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
        duration = 60000
        repeatCount = -1
    }
}

private fun getScaleAnimation(): ScaleAnimation {
    return ScaleAnimation(
        0.8f,
        1f,
        0.8f,
        1f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    ).apply {
        duration = 720
        repeatCount = -1
        repeatMode = Animation.REVERSE
    }
}

private fun getAlphaAnimation(): AlphaAnimation {
    return AlphaAnimation(0.5f, 1f).apply {
        duration = 1000
        repeatCount = -1
        repeatMode = Animation.REVERSE
    }
}
