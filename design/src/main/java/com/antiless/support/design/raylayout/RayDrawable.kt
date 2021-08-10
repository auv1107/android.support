package com.antiless.support.design.raylayout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import java.lang.ref.WeakReference
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 射线 Drawable
 *
 * @author lixindong
 */
class RayDrawable(private val rayBitmap: Bitmap) : Drawable() {

    private val rayAnimationInfoList = mutableListOf<RayAnimationInfo>()
    private val bitmapRect = Rect(0, 0, rayBitmap.width, rayBitmap.height)
    private val listToClear = mutableListOf<RayAnimationInfo>()
    private val clipRect = RectF()
    private val dstRect = Rect()

    override fun draw(canvas: Canvas) {
        rayAnimationInfoList.forEach {
            drawDebugContent(canvas, it)
            drawRay(canvas, it)
        }
        listToClear.forEach {
            removeRay(it)
        }
        listToClear.clear()
        if (rayAnimationInfoList.isNotEmpty()) {
            invalidateSelf()
        }
    }

    private fun drawDebugContent(canvas: Canvas, ray: RayAnimationInfo) {
        canvas.drawCircle(
            ray.toCircle.center.x,
            ray.toCircle.center.y,
            ray.toCircle.radius,
            Paint().apply { color = Color.BLACK })
        canvas.drawCircle(
            ray.fromCircle.center.x,
            ray.fromCircle.center.y,
            ray.fromCircle.radius,
            Paint().apply { color = Color.GREEN })
    }

    override fun setAlpha(alpha: Int) {
        // skip
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // skip
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    fun addRay(fromCircle: Circle, toCircle: Circle): RayAnimationInfo {
        val animationInfo = RayAnimationInfo(fromCircle, toCircle)
        rayAnimationInfoList.add(animationInfo)
        return animationInfo
    }

    fun addRayForView(overlayView: View, fromView: View, toView: View): RayAnimationInfo {
        val fromCircle = fromView.getInnerCircle(overlayView)
        val toCircle = toView.getInnerCircle(overlayView)
        val info = RayViewAnimationInfo(fromCircle, toCircle)
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
        info.holder = RayViewHolder(fromView, toView, fromViewPreDrawListener, toViewPreDrawListener)
        rayAnimationInfoList.add(info)
        return info
    }

    fun removeRay(ray: RayAnimationInfo) {
        rayAnimationInfoList.remove(ray)
        invalidateSelf()
    }

    fun hasRay(ray: RayAnimationInfo): Boolean {
        return rayAnimationInfoList.contains(ray)
    }

    fun destroy() {
        rayAnimationInfoList.forEach {
            it.destroy()
        }
        rayAnimationInfoList.clear()
    }

    private fun drawRay(canvas: Canvas, ray: RayAnimationInfo) {
        canvas.save()
        Log.i("RayDrawable", "drawRay: degreeToVertical ${ray.degreeToVertical}")
        canvas.rotate(ray.degreeToVertical, ray.fromCircle.center.x, ray.fromCircle.center.y)

        val left = ray.fromCircle.center.x - rayBitmap.width / 2
        val right = ray.fromCircle.center.x + rayBitmap.width / 2
        val top = ray.fromCircle.center.y - (ray.distance - ray.toCircle.radius)
        val bottom = ray.fromCircle.center.y - ray.fromCircle.radius
        dstRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        clipRect.set(left, (top - bottom) * ray.fraction + bottom, right, bottom)
        canvas.clipRect(clipRect)
        canvas.drawBitmap(
            rayBitmap,
            bitmapRect,
            dstRect,
            null
        )
        canvas.drawRect(dstRect, Paint().apply { color = Color.RED })
        if (ray.started && ray.duration != 0L && ray.fraction == 1f) {
            doOnEnd(ray)
        }
        canvas.restore()
    }

    private fun doOnEnd(ray: RayAnimationInfo) {
        if (!ray.isPersistent) {
            listToClear.add(ray)
        }
        ray.onEnd()
    }

    open class RayAnimationInfo(
        val fromCircle: Circle,
        val toCircle: Circle,
    ) {
        private var startTime: Long = 0
        val degreeToVertical: Float
            get() {
                val dx = toCircle.center.x - fromCircle.center.x
                val dy = -(toCircle.center.y - fromCircle.center.y)
                val degree = if (dy == 0f) 90f else (atan(dx / dy) * 180 / PI).toFloat()
                return if (dy < 0) degree + 180f else degree
            }
        val distance: Float
            get() = fromCircle.distanceTo(toCircle)
        var isPersistent = false
        var started: Boolean = false
        var duration: Long = 0
        var doOnStart: () -> Unit = {}

        var doOnEnd: () -> Unit = {}
        val fraction: Float
            get() {
                return if (duration == 0L) 1f
                else (SystemClock.elapsedRealtime() - startTime).coerceAtMost(duration).toFloat() / duration
            }

        fun start() {
            startTime = SystemClock.elapsedRealtime()
            started = true
            doOnStart()
        }

        fun onEnd() {
            started = false
            doOnEnd()
        }

        open fun destroy() = Unit
    }

    class RayViewAnimationInfo(fromCircle: Circle, toCircle: Circle) :
        RayAnimationInfo(fromCircle, toCircle) {

        var holder: RayViewHolder? = null

        override fun destroy() {
            holder?.destroy()
        }
    }

    class RayViewHolder(
        fromView: View,
        toView: View,
        fromViewPreDrawListener: ViewTreeObserver.OnPreDrawListener,
        toViewPreDrawListener: ViewTreeObserver.OnPreDrawListener,
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

    class Circle(
        val center: PointF,
        val radius: Float
    ) {

        fun distanceTo(other: Circle): Float {
            val dx = center.x - other.center.x
            val dy = center.y - other.center.y
            return sqrt(dx.pow(2) + dy.pow(2))
        }

        override fun toString(): String {
            return "center: $center radius: $radius"
        }
    }
}

private fun View.getInnerCircle(containerView: View): RayDrawable.Circle {
    val bounds = getBoundsInContainer(containerView)
    return RayDrawable.Circle(
        PointF(bounds.centerX().toFloat(), bounds.centerY().toFloat()),
        min(bounds.width(), bounds.height()) / 2f
    )
}

private fun View.getOuterCircle(containerView: View): RayDrawable.Circle {
    val bounds = getBoundsInContainer(containerView)
    return RayDrawable.Circle(
        PointF(bounds.centerX().toFloat(), bounds.centerY().toFloat()),
        max(bounds.width(), bounds.height()) / 2f
    )
}

private fun View.getBoundsInContainer(containerView: View): Rect {
    return getBoundsInScreen().apply {
        val containerBounds = containerView.getBoundsInScreen()
        offset(-containerBounds.left, -containerBounds.top)
    }
}

private fun View.getCenterPoint(containerView: View): PointF {
    val bounds = getBoundsInScreen()
    return PointF(bounds.centerX().toFloat(), bounds.centerY().toFloat()).apply {
        val containerBounds = containerView.getBoundsInScreen()
        offset(-containerBounds.left.toFloat(), -containerBounds.top.toFloat())
    }
}

private fun View.getBoundsInScreen(): Rect {
    return Rect(0, 0, measuredWidth, measuredHeight).apply {
        val location = IntArray(2)
        getLocationOnScreen(location)
        offset(location[0], location[1])
    }
}
