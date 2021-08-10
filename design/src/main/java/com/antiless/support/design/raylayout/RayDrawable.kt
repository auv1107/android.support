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
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log
import android.view.View
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 射线 Drawable
 *
 * @author lixindong
 */
class RayDrawable(val rayBitmap: Bitmap) : Drawable(), Animatable {

    private val rayAnimationInfoList = mutableListOf<RayAnimationInfo>()
    private val bitmapRect = Rect(0, 0, rayBitmap.width, rayBitmap.height)
    private var isRunning: Boolean = false

    init {
        Log.i("RayDrawable1", "rayBitmap: $bitmapRect")
    }

    override fun draw(canvas: Canvas) {
//        canvas.drawColor(Color.GRAY)
        Log.i("RayDrawable", "draw: bounds $bounds")
        canvas.drawRect(
            bounds,
            Paint().apply { color = Color.CYAN })
        rayAnimationInfoList.forEach {
            drawDebugContent(canvas, it)
            drawRay(canvas, it)
        }
    }

    private fun drawRay(canvas: Canvas, ray: RayAnimationInfo) {
        Log.i("RayDrawable", "drawRay: circle ${ray.fromCircle} ${ray.toCircle} ${ray.degreeToVertical}")
        canvas.save()
        canvas.rotate(ray.degreeToVertical, ray.fromCircle.center.x, ray.fromCircle.center.y)

        val left = ray.fromCircle.center.x - rayBitmap.width / 2
        val right = ray.fromCircle.center.x + rayBitmap.width / 2
        val top = ray.fromCircle.center.y - (ray.distance - ray.toCircle.radius)
        val bottom = ray.fromCircle.center.y - ray.fromCircle.radius
        canvas.clipRect(RectF(left, (top - bottom) * ray.fraction + bottom, right, bottom))
        canvas.drawBitmap(
            rayBitmap,
            bitmapRect,
            Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()),
            Paint(Paint.FILTER_BITMAP_FLAG).apply { }
        )
        if (ray.duration != 0L && ray.fraction == 1f) {
            doOnEnd(ray)
        }
        canvas.restore()
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
        val fromCircle = Circle(
            center = PointF(
                fromView.getBoundsInScreen().centerX().toFloat(),
                fromView.getBoundsInScreen().centerY().toFloat(),
            ),
            radius = fromView.getBoundsInScreen().height().toFloat() / 2
        )
        val toCircle = Circle(
            center = PointF(
                toView.getBoundsInScreen().centerX().toFloat(),
                toView.getBoundsInScreen().centerY().toFloat(),
            ),
            radius = toView.getBoundsInScreen().height().toFloat() / 2
        )
        val info = addRay(fromCircle, toCircle)
        fromView.viewTreeObserver.addOnPreDrawListener {
            info.fromCircle.center.x = fromView.getBoundsInScreen().centerX().toFloat()
            info.fromCircle.center.y = fromView.getBoundsInScreen().centerY().toFloat()
            invalidateSelf()
            true
        }
        toView.viewTreeObserver.addOnPreDrawListener {
            info.toCircle.center.x = toView.getBoundsInScreen().centerX().toFloat()
            info.toCircle.center.y = toView.getBoundsInScreen().centerY().toFloat()
            invalidateSelf()
            true
        }
        return info
    }

    fun addRay(fromRect: Circle, toRect: Circle): RayAnimationInfo {
        val animationInfo = RayAnimationInfo(fromRect, toRect)
        rayAnimationInfoList.add(animationInfo)
        return animationInfo
    }

    fun removeRay(ray: RayAnimationInfo) {
        rayAnimationInfoList.remove(ray)
        invalidateSelf()
    }

    private fun doOnEnd(ray: RayAnimationInfo) {
        if (!ray.isPersistent) {
            removeRay(ray)
        }
        ray.doOnEnd()
    }

    class RayAnimationInfo(
        val fromCircle: Circle,
        val toCircle: Circle,
    ) {
        val degreeToVertical: Float
            get() {
                val dx = toCircle.center.x - fromCircle.center.x
                val dy = -(toCircle.center.y - fromCircle.center.y)
                if (dy == 0f) {
                    return if (dx > 0) 90f else 270f
                }
                return (atan(dx / dy) * 180 / PI).toFloat()
            }
        private var startTime: Long = 0
        var isPersistent = false
        var started: Boolean = false
        var duration: Long = 0
        var listener: AnimationListener? = null

        val fraction: Float
            get() {
                return if (duration == 0L) 1f
                else (SystemClock.elapsedRealtime() - startTime).coerceAtMost(duration).toFloat() / duration
            }
        val distance: Float
            get() = fromCircle.distanceTo(toCircle)

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

    open class Animation : Animatable {

        private var startTime: Long = 0L
        protected var running: Boolean = false
        var doOnEnd: () -> Unit = {}
        var doOnStart: () -> Unit = {}
        var duration: Long = 0L

        val fraction: Float
            get() {
                return if (duration == 0L) 1f
                else (SystemClock.elapsedRealtime() - startTime).coerceAtMost(duration).toFloat() / duration
            }

        override fun start() {
            running = true
            startTime = SystemClock.elapsedRealtime()
        }

        override fun stop() {
            TODO("Not yet implemented")
        }

        override fun isRunning(): Boolean {
            return running
        }
    }

    class AnimationSet : Animation() {

        private val list = mutableListOf<Animation>()

        override fun start() {
            running = true
            doOnStart()
            startAnimation(0)
        }

        private fun startAnimation(index: Int) {
            list[index].apply {
                doOnEnd = {
                    if (index == list.size - 1) {
                        onEnd()
                    } else {
                        startAnimation(index + 1)
                    }
                }
                start()
            }
        }

        private fun onEnd() {
            doOnEnd()
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

fun Rect.distanceTo(other: Rect): Float {
    val deltaX = centerX() - other.centerX()
    val deltaY = centerY() - other.centerY()
    return sqrt(deltaX.toFloat().pow(2) + deltaY.toFloat().pow(2))
}
