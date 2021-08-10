package com.antiless.support.design.raylayout

import android.graphics.PointF
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

/**
 * 射线 drawable 扩展
 *
 * @author lixindong
 */

fun RayDrawable.addRay(overlayView: View, fromView: View, toView: View): RayInfoHolder {
    val fromCircle = fromView.getInnerCircle(overlayView)
    val toCircle = toView.getInnerCircle(overlayView)
    val info = addRay(fromCircle, toCircle)
    val fromViewPreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            return if (hasRay(info)) {
                val center = fromView.getCenterPoint(overlayView)
                info.fromCircle.center.set(center)
                invalidateSelf()
                true
            } else {
                fromView.viewTreeObserver.removeOnPreDrawListener(this)
                false
            }
        }
    }
    val toViewPreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            return if (hasRay(info)) {
                val center = toView.getCenterPoint(overlayView)
                info.toCircle.center.set(center)
                invalidateSelf()
                true
            } else {
                toView.viewTreeObserver.removeOnPreDrawListener(this)
                false
            }
        }
    }
    fromView.viewTreeObserver.addOnPreDrawListener(fromViewPreDrawListener)
    toView.viewTreeObserver.addOnPreDrawListener(toViewPreDrawListener)
    return RayInfoHolder(fromView, toView, fromViewPreDrawListener, toViewPreDrawListener, info)
}

fun View.getInnerCircle(containerView: View): RayDrawable.Circle {
    val bounds = getBoundsInContainer(containerView)
    return RayDrawable.Circle(
        PointF(bounds.centerX().toFloat(), bounds.centerY().toFloat()),
        min(bounds.width(), bounds.height()) / 2f
    )
}

fun View.getOuterCircle(containerView: View): RayDrawable.Circle {
    val bounds = getBoundsInContainer(containerView)
    return RayDrawable.Circle(
        PointF(bounds.centerX().toFloat(), bounds.centerY().toFloat()),
        max(bounds.width(), bounds.height()) / 2f
    )
}

fun View.getBoundsInContainer(containerView: View): Rect {
    return getBoundsInScreen().apply {
        val containerBounds = containerView.getBoundsInScreen()
        offset(-containerBounds.left, -containerBounds.top)
    }
}

fun View.getCenterPoint(containerView: View): PointF {
    val bounds = getBoundsInScreen()
    return PointF(bounds.centerX().toFloat(), bounds.centerY().toFloat()).apply {
        val containerBounds = containerView.getBoundsInScreen()
        offset(-containerBounds.left.toFloat(), -containerBounds.top.toFloat())
    }
}

fun View.getBoundsInScreen(): Rect {
    return Rect(0, 0, measuredWidth, measuredHeight).apply {
        val location = IntArray(2)
        getLocationOnScreen(location)
        offset(location[0], location[1])
    }
}

class RayInfoHolder(
    fromView: View,
    toView: View,
    fromViewPreDrawListener: ViewTreeObserver.OnPreDrawListener,
    toViewPreDrawListener: ViewTreeObserver.OnPreDrawListener,
    val info: RayDrawable.RayAnimationInfo
) {
    private val weakFromView = WeakReference(fromView)
    private val weakToView = WeakReference(toView)
    private val weakFromListener = WeakReference(fromViewPreDrawListener)
    private val weakToListener = WeakReference(toViewPreDrawListener)

    fun destroy() {
        weakFromListener.get()?.let {
            weakFromView.get()?.viewTreeObserver?.removeOnPreDrawListener(it)
        }
        weakToListener.get()?.let {
            weakToView.get()?.viewTreeObserver?.removeOnPreDrawListener(it)
        }
    }
}
