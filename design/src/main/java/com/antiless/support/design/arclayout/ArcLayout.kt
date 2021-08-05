package com.antiless.support.design.arclayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 弧形布局
 *
 * @author lixindong
 */
class ArcLayout(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {


    private val state = State()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        state.centerX = w / 2
        state.centerY = (w * 0.7).toInt()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.i("ArcLayout", "onLayout: measured $measuredWidth $measuredHeight")
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            Log.i("ArcLayout", "onLayout: ${child.measuredWidth} ${child.measuredHeight}")
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
        val x = centerX - childWidth / 2 + indexDelta * (childWidth + state.span)
        val top = computeChildTop(x, childWidth, childHeight)
        return Rect(
            x, top, x + childWidth, top + childHeight
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
