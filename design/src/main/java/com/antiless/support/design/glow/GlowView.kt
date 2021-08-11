package com.antiless.support.design.glow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View

/**
 * 发光 View
 *
 * @author lixindong
 */
class GlowView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    val glowDrawable = GlowDrawable(Color.parseColor("#5724C789"), Color.parseColor("#5724C789"))
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        glowDrawable.setBounds(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        glowDrawable.draw(canvas)
    }
}
