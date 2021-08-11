package com.antiless.support.design.glow

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * 发光 drawable
 *
 * @author lixindong
 */
class GlowDrawable(val borderColor: Int, val glowColor: Int, val borderWidth: Int = 3, val glowWidth: Int = 20) :
    Drawable() {

    private val paint = Paint()

    override fun onBoundsChange(bounds: Rect?) {
        if (bounds?.isEmpty == true) {
            return
        }
        paint.shader = createShader()
    }

    private fun createShader(): RadialGradient {
        val radius = bounds.width() / 2f + borderWidth + glowWidth
        val borderEnd = radius - glowWidth
        val borderStart = borderEnd - borderWidth
        val glowStart = borderEnd
        return RadialGradient(
            bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius, intArrayOf(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                borderColor,
                borderColor,
                glowColor,
                Color.TRANSPARENT
            ), floatArrayOf(
                0f, borderStart / radius, borderStart / radius, borderEnd / radius, glowStart / radius, 1f
            ), Shader.TileMode.MIRROR
        )
    }

    override fun draw(canvas: Canvas) {
        val radius = bounds.width() / 2f + borderWidth + glowWidth
        canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius, paint)
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
