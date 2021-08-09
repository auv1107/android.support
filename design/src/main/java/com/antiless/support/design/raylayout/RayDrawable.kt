package com.antiless.support.design.raylayout

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log
import android.view.View
import kotlin.math.sqrt

/**
 * 射线 Drawable
 *
 * @author lixindong
 */
class RayDrawable : Drawable(), Animatable {

    private val rayAnimationInfoList = mutableListOf<RayAnimationInfo>()
    private var isRunning: Boolean = false

    override fun draw(canvas: Canvas) {
//        canvas.drawColor(Color.GRAY)
        Log.i("RayDrawable", "draw: bounds $bounds")
        canvas.drawRect(
            bounds,
            Paint().apply { color = Color.CYAN })
        rayAnimationInfoList.filter { it.started }.forEach {
            drawRay(canvas, it)
        }
    }

    private fun drawRay(canvas: Canvas, ray: RayAnimationInfo) {
        canvas.save()
        canvas.clipPath(Path().apply {
            addCircle(
                ray.fromRect.centerX().toFloat(),
                ray.fromRect.centerY().toFloat(),
                ray.distance * ray.fraction,
                Path.Direction.CW
            )
        })
        canvas.drawLine(
            ray.fromRect.centerX().toFloat(),
            ray.fromRect.centerY().toFloat(),
            ray.toRect.centerX().toFloat(),
            ray.toRect.centerY().toFloat(),
            Paint().apply { color = Color.RED }
        )
        if (ray.duration != 0L && ray.fraction == 1f) {
            doOnEnd(ray)
        }
        canvas.restore()
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

    override fun start() {
        // skip
    }

    override fun stop() {
        // skip
    }

    override fun isRunning(): Boolean {
        return isRunning
    }

    fun addRay(fromView: View, toView: View): RayAnimationInfo {
        val fr = fromView.getBoundsInScreen()
        val tr = toView.getBoundsInScreen()
        val info = addRay(fr, tr)
        fromView.viewTreeObserver.addOnPreDrawListener {
            info.fromRect.set(fromView.getBoundsInScreen())
            invalidateSelf()
            true
        }
        toView.viewTreeObserver.addOnPreDrawListener {
            info.toRect.set(toView.getBoundsInScreen())
            invalidateSelf()
            true
        }
        return info
    }

    fun addRay(fromRect: Rect, toRect: Rect): RayAnimationInfo {
        val animationInfo = RayAnimationInfo(fromRect, toRect)
        rayAnimationInfoList.add(animationInfo)
        return animationInfo
    }

    fun removeRay(ray: RayAnimationInfo) {
        rayAnimationInfoList.remove(ray)
        invalidateSelf()
    }

    private fun doOnEnd(ray: RayAnimationInfo) {
        removeRay(ray)
        ray.doOnEnd()
    }

    class RayAnimationInfo(
        val fromRect: Rect,
        val toRect: Rect,
    ) {
        private var startTime: Long = 0
        var started: Boolean = false
        var duration: Long = 0
        var listener: AnimationListener? = null

        val fraction: Float
            get() {
                return if (duration == 0L) 1f
                else (SystemClock.elapsedRealtime() - startTime).coerceAtMost(duration).toFloat() / duration
            }
        val distance: Float
            get() = sqrt(
                ((fromRect.centerX() - toRect.centerX()) * (fromRect.centerX() - toRect.centerX()) +
                        (fromRect.centerY() - toRect.centerY()) * (fromRect.centerY() - toRect.centerY())).toFloat()
            )

        fun start() {
            startTime = SystemClock.elapsedRealtime()
            started = true
            listener?.onStart()
        }

        fun doOnEnd() {
            started = false
            listener?.onEnd()
        }
    }

    interface AnimationListener {
        fun onStart()
        fun onEnd()
    }
}

fun View.getBoundsInScreen(): Rect {
    return Rect(0, 0, measuredWidth, measuredHeight).apply {
        offset(x.toInt(), y.toInt())
    }
}
