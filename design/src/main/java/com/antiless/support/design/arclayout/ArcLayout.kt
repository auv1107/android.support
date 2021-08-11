package com.antiless.support.design.arclayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import kotlin.math.sqrt

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

    private val lastPoint = PointF()

    private val scrollStateListeners = mutableListOf<OnScrollChangeListener>()

    /**
     * x 方向滚动距离
     */
    private var arcScrollX = 0f
        set(value) {
            if (field != value) {
                val delta = value - field
                field = value
                updateOffsetAndCurrentIndex(delta)
            }
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ArcLayout)
        state.span = a.getDimensionPixelSize(R.styleable.ArcLayout_span, state.span)
        state.radiusRatioBasedOnWidth =
            a.getFloat(R.styleable.ArcLayout_radiusRatioBasedOnWidth, state.radiusRatioBasedOnWidth)
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
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.drawColor(
            Color.RED
        )
        super.dispatchDraw(canvas)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (settleScroller.computeScrollOffset()) {
            arcScrollX = if (settleScroller.isFinished) {
                notifyScrollStateChanged(ScrollState.IDLE)
                settleScroller.finalX.toFloat()
            } else {
                settleScroller.currX.toFloat()
            }
            requestLayout()
            invalidate()
        }
        if (flingScroller.computeScrollOffset()) {
            arcScrollX = if (flingScroller.isFinished) {
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!flingScroller.isFinished) {
                    flingScroller.abortAnimation()
                }
                state.dragging = false
                velocityTracker.clear()
                velocityTracker.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker.addMovement(event)
                if (!state.dragging && childCount >= 3) {
                    val xDelta = abs(event.x - lastPoint.x)
                    if (xDelta > touchSlop) {
                        state.dragging = true
                        notifyScrollStateChanged(ScrollState.DRAGGING)
                    }
                }
                if (state.dragging) {
                    val xDelta = event.x - lastPoint.x
                    arcScrollX += xDelta
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
                            arcScrollX.toInt(),
                            0,
                            velocityTracker.xVelocity.toInt(),
                            velocityTracker.yVelocity.toInt(),
                            Int.MIN_VALUE,
                            Int.MAX_VALUE,
                            Int.MIN_VALUE,
                            Int.MAX_VALUE
                        )
                    }
                    postInvalidateOnAnimation()
                }
                velocityTracker.clear()
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
    fun setCurrentIndex(index: Int) {
        smoothScrollTo(index)
    }

    fun getCurrentIndex(): Int {
        return state.currentIndex
    }

    fun addScrollChangeListener(listener: OnScrollChangeListener) {
        scrollStateListeners.add(listener)
    }

    fun removeScrollChangeListener(listener: OnScrollChangeListener) {
        scrollStateListeners.remove(listener)
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
        val frame = computeChildFrame(state.currentIndex)
        val anchorWidth = frame.width() / 2 + state.span / 2
        if (state.offset > anchorWidth) {
            state.currentIndex--
            state.offset -= anchorWidth * 2
        }
        if (state.offset < 0 && -state.offset > anchorWidth) {
            state.currentIndex++
            state.offset += anchorWidth * 2
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
            val frame = computeChildFrame(index)
            val currentFrame = computeChildFrame(state.currentIndex)
            -(frame.centerX() - currentFrame.centerX() + state.offset)
        }
        settleScroller.startScroll(arcScrollX.toInt(), 0, dx.toInt(), 0)
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
            return if (index == 0) {
                val left = centerX - childWidth - state.span
                Rect(
                    left, 0, left + childWidth, childHeight
                )
            } else {
                val left = centerX + state.span
                Rect(
                    left, 0, left + childWidth, childHeight
                )
            }
        }
        val indexDelta = index - state.currentIndex
        val childCenterX = centerX + indexDelta * (childWidth + state.span) + state.offset.toInt()
        val left = childCenterX - childWidth / 2
        val top = computeChildTop(left, childWidth, childHeight)
        return Rect(
            left, top, left + childWidth, top + childHeight
        )
    }

    private fun computeChildTop(left: Int, childWidth: Int, childHeight: Int): Int {
        val radius = state.centerY - childHeight / 2
        val xDelta = abs(state.centerX - (left + childWidth / 2))
        val yDelta = sqrt((radius * radius - xDelta * xDelta).toDouble()).toInt()
        return state.centerY - (yDelta + childHeight / 2)
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
         * 项水平间距
         */
        var span: Int = 60,

        /**
         * 圆心半径
         */
        var radius: Int = 0,

        /**
         * 半径相对于宽度比例
         */
        var radiusRatioBasedOnWidth: Float = 0f,

        /**
         * 当前滚动状态
         */
        var scrollState: ScrollState = ScrollState.IDLE,

        /**
         * 当前偏移量
         */
        var offset: Float = 0f,

        /**
         * 手势是否处于拖拽状态
         */
        var dragging: Boolean = false
    )

    private class ViewHolder(
        val view: View,
        val info: Any,
    )

    enum class ScrollState {
        IDLE,
        DRAGGING,
        FLING,
        SETTLING
    }
}
