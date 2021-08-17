package com.antiless.support.design.arclayout

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.OverScroller
import android.widget.Scroller
import com.antiless.support.design.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 弧形布局
 *
 * @author lixindong
 */
class ArcLayout(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    private val state = State()

    private val flingScroller = OverScroller(context)
    private val settleScroller = Scroller(context)

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val maximumVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private val minimumVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val velocityTracker = VelocityTracker.obtain()

    private val downPoint = PointF()
    private val lastPoint = PointF()

    private val scrollStateListeners = mutableListOf<OnScrollChangeListener>()

    /**
     * x 方向滚动角度
     */
    private var scrollRotation = 0f
        set(value) {
            if (field != value) {
                val delta = value - field
                field = value
                updateOffsetAndCurrentIndex(delta)
            }
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ArcLayout)
        state.degreeSpan = a.getFloat(R.styleable.ArcLayout_degreeSpan, state.degreeSpan)
        state.radiusRatioBasedOnWidth =
            a.getFloat(R.styleable.ArcLayout_radiusRatioBasedOnWidth, state.radiusRatioBasedOnWidth)
        state.radiusRatioBasedOnHeight =
            a.getFloat(R.styleable.ArcLayout_radiusRatioBasedOnHeight, state.radiusRatioBasedOnHeight)
        state.radius = a.getDimensionPixelSize(R.styleable.ArcLayout_android_radius, state.radius)
        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        state.centerX = w / 2
        when {
            state.radiusRatioBasedOnWidth != 0f -> {
                state.centerY = (w * state.radiusRatioBasedOnWidth).toInt()
            }
            state.radiusRatioBasedOnHeight != 0f -> {
                state.centerY = (h * state.radiusRatioBasedOnHeight).toInt()
            }
            state.radius != 0 -> {
                state.centerY = state.radius
            }
            else -> {
                state.centerY = h
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val frame = computeChildFrame(i)
            child.layout(frame.left, frame.top, frame.right, frame.bottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            val degree = computeChildDegree(i)
            val child = getChildAt(i)
            if (abs(degree) > 180) {
                child.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
                )
            }
        }

    }

    override fun computeScroll() {
        super.computeScroll()
        if (settleScroller.computeScrollOffset()) {
            scrollRotation = if (settleScroller.isFinished) {
                notifyScrollStateChanged(ScrollState.IDLE)
                settleScroller.finalX.toFloat()
            } else {
                settleScroller.currX.toFloat()
            }
            requestLayout()
            invalidate()
            postInvalidateOnAnimation()
        }
        if (flingScroller.computeScrollOffset()) {
            scrollRotation = if (flingScroller.isFinished) {
                flingScroller.finalX.toFloat()
            } else {
                flingScroller.currX.toFloat()
            }
            requestLayout()
            invalidate()
            if (flingScroller.currVelocity < minimumVelocity) {
                flingScroller.abortAnimation()
                autoSettle()
                postInvalidateOnAnimation()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        velocityTracker.recycle()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (state.dragging && ev.action == MotionEvent.ACTION_MOVE) return true

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                abortScroller()
                recycleVelocityTracker()

                downPoint.set(ev.x, ev.y)
                state.dragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                validateDragging(ev)
                lastPoint.set(ev.x, ev.y)
            }
        }
        return state.dragging
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                abortScroller()
                recycleVelocityTracker()
                velocityTracker.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker.addMovement(event)
                validateDragging(event)
                if (state.dragging) {
                    val xDelta = event.x - lastPoint.x
                    scrollRotation += xDelta * SCROLL_ROTATION_RATIO
                    requestLayout()
                }
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                velocityTracker.addMovement(event)
                if (state.dragging) {
                    velocityTracker.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                    if (abs(velocityTracker.xVelocity) <= minimumVelocity) {
                        notifyScrollStateChanged(ScrollState.SETTLING)
                        autoSettle()
                    } else {
                        notifyScrollStateChanged(ScrollState.FLING)
                        flingScroller.fling(
                            scrollRotation.toInt(),
                            0,
                            (velocityTracker.xVelocity * SCROLL_ROTATION_RATIO).toInt(),
                            velocityTracker.yVelocity.toInt(),
                            Int.MIN_VALUE,
                            Int.MAX_VALUE,
                            Int.MIN_VALUE,
                            Int.MAX_VALUE
                        )
                    }
                    postInvalidateOnAnimation()
                }
                recycleVelocityTracker()
                state.dragging = false
            }
        }
        lastPoint.set(event.x, event.y)
        super.onTouchEvent(event)
        return true
    }

    /**
     * 添加项
     */
    fun addItem(view: View) {
        if (childCount == 2) {
            state.currentIndex = 1
        }
        if (state.currentIndex == -1) {
            if (childCount == 0) {
                addView(view)
            } else if (childCount == 1) {
                addView(view, 0)
            }
        } else {
            if (childCount % 2 == 1) {
                state.currentIndex += 1
            }
            addView(view, state.currentIndex)
        }
    }

    fun removeItem(view: View) {
        if (childCount <= 3) {
            state.currentIndex = -1
            state.offset = 0f
            removeView(view)
            return
        }
        val index = indexOfChild(view)
        if (index == -1) return

        if (index < state.currentIndex) {
            state.currentIndex -= 1
        }
        if (state.currentIndex >= childCount - 2) {
            state.currentIndex = childCount - 3
        }
        if (state.currentIndex <= 1) {
            state.currentIndex = 1
        }
        removeView(view)
    }

    /**
     * 设置当前位置
     */
    fun setCurrentItem(index: Int) {
        smoothScrollTo(index)
    }

    fun getCurrentItem(): Int {
        return state.currentIndex
    }

    fun addScrollChangeListener(listener: OnScrollChangeListener) {
        scrollStateListeners.add(listener)
    }

    fun removeScrollChangeListener(listener: OnScrollChangeListener) {
        scrollStateListeners.remove(listener)
    }

    /**
     * 设置半径
     */
    fun setRadius(radius: Int) {
        if (radius <= 0f) {
            throw RuntimeException("Radius must > 0")
        }
        state.radius = radius
        state.centerY = radius
        requestLayout()
    }

    /**
     * 设置基于宽度比例的半径
     */
    fun setRadiusRatioBaseOnWidth(ratioBasedOnWidth: Float) {
        if (ratioBasedOnWidth <= 0f) {
            throw RuntimeException("Ratio must > 0f")
        }
        state.radiusRatioBasedOnWidth = ratioBasedOnWidth
        state.centerY = (measuredWidth * state.radiusRatioBasedOnWidth).toInt()
        requestLayout()
    }

    /**
     * 设置基于高度比例的半径
     */
    fun setRadiusRatioBaseOnHeight(ratioBasedOnHeight: Float) {
        if (ratioBasedOnHeight <= 0f) {
            throw RuntimeException("Ratio must > 0f")
        }
        state.radiusRatioBasedOnHeight = ratioBasedOnHeight
        state.centerY = (measuredHeight * state.radiusRatioBasedOnHeight).toInt()
        requestLayout()
    }

    private fun validateDragging(event: MotionEvent) {
        if (!state.dragging && childCount >= 3) {
            val xDelta = abs(event.x - downPoint.x)
            if (xDelta > touchSlop) {
                state.dragging = true
                notifyScrollStateChanged(ScrollState.DRAGGING)
            }
        }
    }

    private fun recycleVelocityTracker() {
        velocityTracker.clear()
    }

    private fun abortScroller() {
        if (!flingScroller.isFinished) {
            flingScroller.abortAnimation()
        }
        if (!settleScroller.isFinished) {
            settleScroller.abortAnimation()
        }
    }

    private fun updateOffsetAndCurrentIndex(delta: Float) {
        val oldOffset = state.offset
        val oldIndex = state.currentIndex
        state.offset += delta
        if (state.currentIndex == 1 && state.offset > 0) {
            state.offset = 0f
        }
        if (state.currentIndex == childCount - 2 && state.offset < 0) {
            state.offset = 0f
        }
        if (state.offset > state.degreeSpan / 2) {
            state.currentIndex--
            state.offset -= state.degreeSpan
        }
        if (state.offset < 0 && -state.offset > state.degreeSpan / 2) {
            state.currentIndex++
            state.offset += state.degreeSpan
        }
        if (oldOffset != state.offset || oldIndex != state.currentIndex) {
            notifyScrollOffsetChanged(oldIndex, oldOffset, state.currentIndex, state.offset)
        }
    }

    private fun autoSettle() {
        smoothScrollTo(state.currentIndex)
    }

    private fun smoothScrollTo(index: Int) {
        if (index <= 0 || index >= childCount - 1) {
            return
        }
        val dx = if (index == state.currentIndex) {
            -state.offset
        } else {
            -((index - state.currentIndex) * state.degreeSpan + state.offset)
        }
        settleScroller.startScroll(scrollRotation.toInt(), 0, dx.toInt(), 0)
        invalidate()
    }

    private fun computeChildFrame(index: Int): Rect {
        val child = getChildAt(index)
        val centerX = measuredWidth / 2
        val childWidth = child.measuredWidth
        val childHeight = child.measuredHeight
        if (childCount == 1) {
            return Rect(
                (centerX - childWidth / 2),
                0,
                (centerX + childWidth / 2),
                child.measuredHeight
            )
        }
        if (childCount == 2) {
            val degree = if (index == 0) -state.degreeSpan / 2 else state.degreeSpan / 2
            val left = computeChildLeft(centerX, childWidth, degree)
            return Rect(
                left, 0, left + childWidth, childHeight
            )
        }
        val degree = computeChildDegree(index)
        val left = computeChildLeft(centerX, childWidth, degree)
        val top = computeChildTop(degree)
        return Rect(0, 0, childWidth, childHeight).apply {
            offset(left, top)
        }
    }

    private fun computeChildDegree(index: Int): Float {
        val indexDelta = index - state.currentIndex
        return indexDelta * state.degreeSpan + state.offset
    }

    private fun computeChildLeft(centerX: Int, childWidth: Int, degree: Float): Int {
        val radians = Math.toRadians(degree.toDouble())
        return (centerX + state.centerY * sin(radians) - childWidth / 2).toInt()
    }

    private fun computeChildTop(degree: Float): Int {
        val radius = state.centerY
        val radians = Math.toRadians(degree.toDouble())
        return (radius - radius * cos(radians)).toInt()
    }

    private fun notifyScrollStateChanged(scrollState: ScrollState) {
        if (state.scrollState == scrollState) {
            return
        }

        val oldState = state.scrollState
        state.scrollState = scrollState
        post {
            scrollStateListeners.forEach { it.onScrollStateChanged(oldState, scrollState) }
        }
    }

    private fun notifyScrollOffsetChanged(oldIndex: Int, oldOffset: Float, newIndex: Int, newOffset: Float) {
        post {
            scrollStateListeners.forEach { it.onScrollOffsetChanged(oldIndex, oldOffset, newIndex, newOffset) }
        }
    }

    interface OnScrollChangeListener {

        fun onScrollOffsetChanged(oldIndex: Int, oldOffset: Float, newIndex: Int, newOffset: Float) = Unit
        fun onScrollStateChanged(oldState: ScrollState, newState: ScrollState) = Unit
    }

    private class State(

        /**
         * 元素不足 3 个时为 -1
         */
        var currentIndex: Int = -1,
        var centerX: Int = 0,
        var centerY: Int = 0,

        /**
         * 间隔角度
         */
        var degreeSpan: Float = 15f,

        /**
         * 圆心半径
         */
        var radius: Int = 0,

        /**
         * 半径相对于宽度比例
         */
        var radiusRatioBasedOnWidth: Float = 0f,

        /**
         * 半径相对于高度比例
         */
        var radiusRatioBasedOnHeight: Float = 0f,

        /**
         * 当前滚动状态
         */
        var scrollState: ScrollState = ScrollState.IDLE,

        /**
         * 当前偏移角度
         */
        var offset: Float = 0f,

        /**
         * 手势是否处于拖拽状态
         */
        var dragging: Boolean = false
    )

    enum class ScrollState {
        IDLE,
        DRAGGING,
        FLING,
        SETTLING
    }

    companion object {

        /**
         *  滚动角度系数
         *  每像素转动多少度
         */
        private const val SCROLL_ROTATION_RATIO = 360f / 3600
    }
}
