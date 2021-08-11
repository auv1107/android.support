package com.antiless.support.design.glow;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;

public class GlowingRing extends View {

    //The coefficients for computation blur
    //and glowing values depending on body stroke width
    private static final float GLOWING_MULTIPLIER = 4;
    private static final float BLUR_MULTIPLIER = 2;

    //dp neon ring geometry params
    private static final float RING_RADIUS = 70;
    private static final float BODY_STROKE_WIDTH = 6;
    private static final float GLOW_STROKE_WIDTH
            = GLOWING_MULTIPLIER * BODY_STROKE_WIDTH;
    private static final float BLUR_RADIUS
            = BLUR_MULTIPLIER * BODY_STROKE_WIDTH;

    //colors foe body and glowing area
    private final static int BODY_COLOR = 0xFFBEFF09;
    private final static int GLOW_COLOR_20P_ALPHA = 0x33BCFF00;
    //alternative color for glowing area
    private final static int GLOW_COLOR_80P_ALPHA = 0xCBBCFF00;
    private int bodyColor = BODY_COLOR;
    private int glowingColor = GLOW_COLOR_20P_ALPHA;

    //geometry params in Px:
    private float ringRadius;
    private float bodyStrokeWidthPx;
    private float glowStrokeWidthPx;
    private float blurRadiusPx;

    //painting objects for blur ring, shadowLayer ring
    // and shaderGradientRing respectively:
    private Paint bodyPaint;
    private Paint glowingPaint;
    private Paint shadowLayerPaint;
    private Paint shaderPaint;

    private float centerBlurRingY;
    private float centerShadowLayerRingY;
    private float centerShaderRingY;
    private float centerRingX;

    public GlowingRing(Context context) {
        super(context);
        init();
    }

    public GlowingRing(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GlowingRing(Context context, @Nullable AttributeSet attrs,
                       int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerRingX = w / 2f;

        centerBlurRingY = (h / 2f - ringRadius) / 2;
        centerShadowLayerRingY = h / 2f;
        centerShaderRingY = h - (h / 2f - ringRadius) / 2;
        shaderPaint.setShader(createRadialGradient(centerRingX, centerShaderRingY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBlurRing(canvas);
        drawShadowLayerRing(canvas);
        drawGradientShaderRing(canvas);
    }

    private void drawGradientShaderRing(Canvas canvas) {
        canvas.drawCircle(centerRingX, centerShaderRingY, ringRadius, shaderPaint);
//        canvas.drawRect(0, 0, getRight(), getBottom(), shaderPaint);
    }

    private void drawShadowLayerRing(Canvas canvas) {
        canvas.drawCircle(centerRingX, centerShadowLayerRingY,
                ringRadius, shadowLayerPaint);
    }

    private void drawBlurRing(Canvas canvas) {
        canvas.drawCircle(centerRingX, centerBlurRingY, ringRadius, glowingPaint);
        canvas.drawCircle(centerRingX, centerBlurRingY, ringRadius, bodyPaint);
    }

    private void init() {
        ringRadius = convertDpToPixel(RING_RADIUS, getContext());
        bodyStrokeWidthPx = convertDpToPixel(BODY_STROKE_WIDTH, getContext());
        glowStrokeWidthPx = convertDpToPixel(GLOW_STROKE_WIDTH, getContext());
        blurRadiusPx = convertDpToPixel(BLUR_RADIUS, getContext());

        initBlurPainting();
        initShadowLayerPainting();
        initShaderRingPainting();
    }

    private void initShaderRingPainting() {
        shaderPaint = new Paint();
        shaderPaint.setAntiAlias(true);
        shaderPaint.setStrokeWidth(glowStrokeWidthPx + blurRadiusPx * 2);
        shaderPaint.setStyle(Paint.Style.STROKE);
    }

    private void initShadowLayerPainting() {
        shadowLayerPaint = new Paint();
        shadowLayerPaint.setAntiAlias(true);
        shadowLayerPaint.setAlpha(255);
        shadowLayerPaint.setColor(bodyColor);
        shadowLayerPaint.setStrokeWidth(bodyStrokeWidthPx);
        shadowLayerPaint.setStyle(Paint.Style.STROKE);
        shadowLayerPaint.setShadowLayer(blurRadiusPx * 2, 0, 0, bodyColor);
    }

    private void initBlurPainting() {
        bodyPaint = new Paint();
        bodyPaint.setAntiAlias(true);
        bodyPaint.setColor(bodyColor);
        bodyPaint.setStrokeWidth(bodyStrokeWidthPx);
        bodyPaint.setStyle(Paint.Style.STROKE);

        glowingPaint = new Paint();
        glowingPaint.setAntiAlias(true);
        glowingPaint.setColor(glowingColor);
        glowingPaint.setMaskFilter(new BlurMaskFilter(blurRadiusPx,
                BlurMaskFilter.Blur.NORMAL));
        glowingPaint.setStrokeWidth(glowStrokeWidthPx);
        glowingPaint.setStyle(Paint.Style.STROKE);
    }

    private Shader createRadialGradient(float centerRingX, float centerShaderRingY) {

        float gradientRadiusPx = ringRadius + glowStrokeWidthPx / 2 + blurRadiusPx;
        // transparent
        float innerEndGlowingPx = ringRadius - glowStrokeWidthPx / 2 - blurRadiusPx;
        // glowing color
        float innerStartGlowingPx = ringRadius - glowStrokeWidthPx / 4;
        // glowing color
        float constantInnerGlowingPx = ringRadius - bodyStrokeWidthPx / 2;
        // body color
        float innerBodyEndPx = (ringRadius - bodyStrokeWidthPx / 2) - 1;
        //body color
        float outerBodyEndPx = (ringRadius + bodyStrokeWidthPx / 2) + 1;
        // glowing color
        float constantOuterGlowingPx = ringRadius + bodyStrokeWidthPx / 2;
        // glowing color
        float outerStartGlowingPx = ringRadius + glowStrokeWidthPx / 4;
        // transparent
        float outerEndGlowingPx = ringRadius + glowStrokeWidthPx / 2 + blurRadiusPx;


        //  normalized values in same order:

        float innerEndGlowingNormalized = innerEndGlowingPx / gradientRadiusPx;
        float innerStartGlowingNormalized = innerStartGlowingPx / gradientRadiusPx;
        float constantInnerGlowingNormalized = constantInnerGlowingPx / gradientRadiusPx;
        float innerBodyEndNormalized = innerBodyEndPx / gradientRadiusPx;
        float outerBodyEndNormalized = outerBodyEndPx / gradientRadiusPx;
        float constantOuterGlowingNormalized = constantOuterGlowingPx / gradientRadiusPx;
        float outerStartGlowingNormalized = outerStartGlowingPx / gradientRadiusPx;
        float outerEndGlowingNormalized = outerEndGlowingPx / gradientRadiusPx;

        float[] stops = {
                innerEndGlowingNormalized,
                innerStartGlowingNormalized,
                constantInnerGlowingNormalized,
                innerBodyEndNormalized,
                outerBodyEndNormalized,
                constantOuterGlowingNormalized,
                outerStartGlowingNormalized,
                outerEndGlowingNormalized
        };

        int[] colors = {
                Color.TRANSPARENT,
                glowingColor,
                glowingColor,
                bodyColor,
                bodyColor,
                glowingColor,
                glowingColor,
                Color.TRANSPARENT
        };
        return new RadialGradient(centerRingX, centerShaderRingY, gradientRadiusPx,
                colors,
                stops,
                Shader.TileMode.MIRROR);
    }

    // helper  util method
    private static float convertDpToPixel(float valueDp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueDp,
                displayMetrics);
    }
}
