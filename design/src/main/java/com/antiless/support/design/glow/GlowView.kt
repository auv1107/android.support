package com.antiless.support.design.glow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.antiless.support.design.R

/**
 * 发光 View
 *
 * @author lixindong
 */
class GlowView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val state = State()

    var glowDrawable: GlowDrawable

    init {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.GlowView)
        state.borderColor = a.getColor(R.styleable.GlowView_borderColor, state.borderColor)
        state.glowColor = a.getColor(R.styleable.GlowView_glowColor, state.glowColor)
        state.borderWidth = a.getDimensionPixelSize(R.styleable.GlowView_borderWidth, state.borderWidth)
        state.glowWidth = a.getDimensionPixelSize(R.styleable.GlowView_glowWidth, state.glowWidth)
        a.recycle()

        glowDrawable = GlowDrawable(state.borderColor, state.glowColor, state.borderWidth, state.glowWidth)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        glowDrawable.setBounds(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        glowDrawable.draw(canvas)
    }

    private class State {
        var borderColor: Int = Color.parseColor("#5724C789")
        var glowColor: Int = Color.parseColor("#5724C789")
        var borderWidth: Int = 3
        var glowWidth: Int = 20
    }
}
