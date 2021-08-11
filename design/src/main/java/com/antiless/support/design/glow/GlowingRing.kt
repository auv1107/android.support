package com.antiless.support.design.glow

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class GlowingRing : View {
    private val bodyColor = BODY_COLOR
    private val glowingColor = GLOW_COLOR_20P_ALPHA

    //geometry params in Px:
    private var ringRadius = 0f
    private var bodyStrokeWidthPx = 0f
    private var glowStrokeWidthPx = 0f
    private var blurRadiusPx = 0f

    //painting objects for blur ring, shadowLayer ring
    // and shaderGradientRing respectively:
    private var bodyPaint: Paint? = null
    private var glowingPaint: Paint? = null
    private var shadowLayerPaint: Paint? = null
    private var shaderPaint: Paint? = null
    private var centerBlurRingY = 0f
    private var centerShadowLayerRingY = 0f
    private var centerShaderRingY = 0f
    private var centerRingX = 0f

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerRingX = w / 2f
        centerBlurRingY = (h / 2f - ringRadius) / 2
        centerShadowLayerRingY = h / 2f
        centerShaderRingY = h - (h / 2f - ringRadius) / 2
        shaderPaint!!.shader = createRadialGradient(centerRingX, centerShaderRingY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBlurRing(canvas)
        drawShadowLayerRing(canvas)
        drawGradientShaderRing(canvas)
    }

    private fun drawGradientShaderRing(canvas: Canvas) {
        canvas.drawCircle(centerRingX, centerShaderRingY, ringRadius, shaderPaint!!)
        //        canvas.drawRect(0, 0, getRight(), getBottom(), shaderPaint);
    }

    private fun drawShadowLayerRing(canvas: Canvas) {
        canvas.drawCircle(
            centerRingX, centerShadowLayerRingY,
            ringRadius, shadowLayerPaint!!
        )
    }

    private fun drawBlurRing(canvas: Canvas) {
        canvas.drawCircle(centerRingX, centerBlurRingY, ringRadius, glowingPaint!!)
        canvas.drawCircle(centerRingX, centerBlurRingY, ringRadius, bodyPaint!!)
    }

    private fun init() {
        ringRadius = convertDpToPixel(RING_RADIUS, context)
        bodyStrokeWidthPx = convertDpToPixel(BODY_STROKE_WIDTH, context)
        glowStrokeWidthPx = convertDpToPixel(GLOW_STROKE_WIDTH, context)
        blurRadiusPx = convertDpToPixel(BLUR_RADIUS, context)
        initBlurPainting()
        initShadowLayerPainting()
        initShaderRingPainting()
    }

    private fun initShaderRingPainting() {
        shaderPaint = Paint()
        shaderPaint!!.isAntiAlias = true
        shaderPaint!!.strokeWidth = glowStrokeWidthPx + blurRadiusPx * 2
        shaderPaint!!.style = Paint.Style.STROKE
    }

    private fun initShadowLayerPainting() {
        shadowLayerPaint = Paint()
        shadowLayerPaint!!.isAntiAlias = true
        shadowLayerPaint!!.alpha = 255
        shadowLayerPaint!!.color = bodyColor
        shadowLayerPaint!!.strokeWidth = bodyStrokeWidthPx
        shadowLayerPaint!!.style = Paint.Style.STROKE
        shadowLayerPaint!!.setShadowLayer(blurRadiusPx * 2, 0f, 0f, bodyColor)
    }

    private fun initBlurPainting() {
        bodyPaint = Paint()
        bodyPaint!!.isAntiAlias = true
        bodyPaint!!.color = bodyColor
        bodyPaint!!.strokeWidth = bodyStrokeWidthPx
        bodyPaint!!.style = Paint.Style.STROKE
        glowingPaint = Paint()
        glowingPaint!!.isAntiAlias = true
        glowingPaint!!.color = glowingColor
        glowingPaint!!.maskFilter = BlurMaskFilter(
            blurRadiusPx,
            BlurMaskFilter.Blur.NORMAL
        )
        glowingPaint!!.strokeWidth = glowStrokeWidthPx
        glowingPaint!!.style = Paint.Style.STROKE
    }

    private fun createRadialGradient(centerRingX: Float, centerShaderRingY: Float): Shader {
        val gradientRadiusPx = ringRadius + glowStrokeWidthPx / 2 + blurRadiusPx
        // transparent
        val innerEndGlowingPx = ringRadius - glowStrokeWidthPx / 2 - blurRadiusPx
        // glowing color
        val innerStartGlowingPx = ringRadius - glowStrokeWidthPx / 4
        // glowing color
        val constantInnerGlowingPx = ringRadius - bodyStrokeWidthPx / 2
        // body color
        val innerBodyEndPx = ringRadius - bodyStrokeWidthPx / 2 - 1
        //body color
        val outerBodyEndPx = ringRadius + bodyStrokeWidthPx / 2 + 1
        // glowing color
        val constantOuterGlowingPx = ringRadius + bodyStrokeWidthPx / 2
        // glowing color
        val outerStartGlowingPx = ringRadius + glowStrokeWidthPx / 4
        // transparent
        val outerEndGlowingPx = ringRadius + glowStrokeWidthPx / 2 + blurRadiusPx


        //  normalized values in same order:
        val innerEndGlowingNormalized = innerEndGlowingPx / gradientRadiusPx
        val innerStartGlowingNormalized = innerStartGlowingPx / gradientRadiusPx
        val constantInnerGlowingNormalized = constantInnerGlowingPx / gradientRadiusPx
        val innerBodyEndNormalized = innerBodyEndPx / gradientRadiusPx
        val outerBodyEndNormalized = outerBodyEndPx / gradientRadiusPx
        val constantOuterGlowingNormalized = constantOuterGlowingPx / gradientRadiusPx
        val outerStartGlowingNormalized = outerStartGlowingPx / gradientRadiusPx
        val outerEndGlowingNormalized = outerEndGlowingPx / gradientRadiusPx
        val stops = floatArrayOf(
            innerEndGlowingNormalized,
            innerStartGlowingNormalized,
            constantInnerGlowingNormalized,
            innerBodyEndNormalized,
            outerBodyEndNormalized,
            constantOuterGlowingNormalized,
            outerStartGlowingNormalized,
            outerEndGlowingNormalized
        )
        val colors = intArrayOf(
            Color.TRANSPARENT,
            glowingColor,
            glowingColor,
            bodyColor,
            bodyColor,
            glowingColor,
            glowingColor,
            Color.TRANSPARENT
        )
        return RadialGradient(
            centerRingX, centerShaderRingY, gradientRadiusPx,
            colors,
            stops,
            Shader.TileMode.MIRROR
        )
    }

    companion object {
        //The coefficients for computation blur
        //and glowing values depending on body stroke width
        private const val GLOWING_MULTIPLIER = 4f
        private const val BLUR_MULTIPLIER = 2f

        //dp neon ring geometry params
        private const val RING_RADIUS = 70f
        private const val BODY_STROKE_WIDTH = 6f
        private const val GLOW_STROKE_WIDTH = GLOWING_MULTIPLIER * BODY_STROKE_WIDTH
        private const val BLUR_RADIUS = BLUR_MULTIPLIER * BODY_STROKE_WIDTH

        //colors foe body and glowing area
        private const val BODY_COLOR = -0x4100f7
        private const val GLOW_COLOR_20P_ALPHA = 0x33BCFF00

        //alternative color for glowing area
        private const val GLOW_COLOR_80P_ALPHA = -0x34430100

        // helper  util method
        private fun convertDpToPixel(valueDp: Float, context: Context): Float {
            val displayMetrics = context.resources.displayMetrics
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valueDp,
                displayMetrics
            )
        }
    }
}
