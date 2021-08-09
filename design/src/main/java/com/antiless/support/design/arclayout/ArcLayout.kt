package com.antiless.support.design.arclayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.OverScroller
import android.widget.Scroller
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
    private val velocityTracker = VelocityTracker.obtain()
    private var contentWidth = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        state.centerX = w / 2
        state.centerY = (w * 0.7).toInt()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val frame = computeChildFrame(i)
            child.layout(frame.left, frame.top, frame.right, frame.bottom)
        }
        val firstFrame = computeChildFrame(0)
        val lastFrame = computeChildFrame(childCount - 1)
        contentWidth = lastFrame.right - firstFrame.left
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

    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        super.addView(child, index, params)
    }

    override fun removeView(view: View?) {
        super.removeView(view)
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
//        computeCurrentIndexAfterAdded(indexOfChild(child))
    }

    override fun computeScroll() {
        super.computeScroll()
        if (settleScroller.computeScrollOffset()) {
            arcScrollX = if (settleScroller.isFinished) {
//                validateOffsetAndCurrentIndex()
                settleScroller.finalX.toFloat()
            } else {
                settleScroller.currX.toFloat()
            }
//            Log.i("ArcLayout", "computeScroll: settleScroller offset $offset")
            requestLayout()
            invalidate()
        }
        if (flingScroller.computeScrollOffset()) {
            arcScrollX = if (flingScroller.isFinished) {
//                Log.i("ArcLayout", "computeScroll: isfinished")
                flingScroller.finalX.toFloat()
            } else {
                flingScroller.currX.toFloat()
            }
//            Log.i("ArcLayout", "computeScroll: currVelocity ${flingScroller.currVelocity}")
            validateOffsetAndCurrentIndex()
            requestLayout()
            invalidate()
            if (flingScroller.currVelocity < 2000f) {
                flingScroller.abortAnimation()
                autoSettle()
            }
        }
    }

    private fun validateOffsetAndCurrentIndex() {
        if (state.currentIndex == 1) {
            if (offset > 0) {
                offset = 0f
            }
        }
        if (state.currentIndex == childCount - 2) {
            if (offset < 0) {
                offset = 0f
            }
        }
        val frame = computeChildFrame(state.currentIndex)
        val anchorWidth = frame.width() / 2 + state.span / 2
        if (offset > anchorWidth) {
            state.currentIndex--
            offset -= anchorWidth * 2
        }
        if (offset < 0 && -offset > anchorWidth) {
            state.currentIndex++
            offset += anchorWidth * 2
        }
    }

    private fun autoSettle() {
        smoothScrollTo(state.currentIndex)
    }

    private fun smoothScrollTo(index: Int) {
        val dx = if (index == state.currentIndex) {
            -offset
        } else {
            val frame = computeChildFrame(index)
            val currentFrame = computeChildFrame(state.currentIndex)
            -(frame.centerX() - currentFrame.centerX() + offset)
        }
        Log.i("ArcLayout", "smoothScrollTo: offset $offset dx $dx")
        settleScroller.startScroll(arcScrollX.toInt(), 0, dx.toInt(), 0)
        invalidate()
    }

    private val downPoint = PointF()
    private var dragging = false
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downPoint.set(ev.x, ev.y)
                dragging = false
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
            }
            else -> {
                // skip
            }
        }
        return true
    }

    private val lastPoint = PointF()
    private var offset = 0f
    private var arcScrollX = 0f
        set(value) {
            if (field != value) {
                val delta = field - value
                field = value
                offset -= delta
                validateOffsetAndCurrentIndex()
            }
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!flingScroller.isFinished) {
                    flingScroller.abortAnimation()
                }
                dragging = false
                velocityTracker.clear()
                velocityTracker.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker.addMovement(event)
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                if (!dragging && childCount >= 3) {
                    val xDelta = abs(event.x - lastPoint.x)
                    if (xDelta > touchSlop) {
                        dragging = true
                    }
                }
                if (dragging) {
                    val xDelta = event.x - lastPoint.x
                    arcScrollX += xDelta
                    validateOffsetAndCurrentIndex()
                    requestLayout()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    Log.i("ArcLayout", "onTouchEvent: up dragging ${velocityTracker.xVelocity}")
                    if (velocityTracker.xVelocity < 2000) {
                        autoSettle()
                    } else {
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
                }
                velocityTracker.clear()
            }
        }
        lastPoint.set(event.x, event.y)
//        return super.onTouchEvent(event)
        return true
    }

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
        val childCenterX = centerX + indexDelta * (childWidth + state.span) + offset.toInt()
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

    private fun computeCurrentIndexAfterAdded(index: Int) {
        if (childCount == 3) {
            state.currentIndex = 1
        }
    }

    private fun computeCurrentIndexAfterRemove(index: Int) {
        if (childCount < 3) {
            state.currentIndex = -1
        }
        if (childCount == 3) {
            state.currentIndex = 1
        }
        if (index == -1) return
        when {
            index == state.currentIndex -> {
            }
            index < state.currentIndex -> {
            }
            index > state.currentIndex -> {
                // skip
            }
        }
    }

    fun setCurrentIndex(index: Int) {
        smoothScrollTo(index)
    }

    private class State(
        var currentIndex: Int = -1,
        var centerX: Int = 0,
        var centerY: Int = 0,
        var span: Int = 60
    )

    private class ViewHolder(
        val view: View,
        val info: Any,
    )
}
