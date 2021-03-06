package com.antiless.support.design.raylayout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 * 射线动画布局
 *
 * @author lixindong
 */
class RayAnimLayout(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    var rayDrawable: RayDrawable? = null
    var rayBitmap: Bitmap? = null
        set(value) {
            field = value
            value?.let {
                updateRayBitmap(it)
            }
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rayDrawable?.setBounds(0, 0, w, h)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroyRayDrawable()
    }

    fun addRay(fromView: View, toView: View): RayDrawable.RayAnimationInfo? {
        return rayDrawable?.addRayForView(this, fromView, toView)
    }

    fun setRayResource(resId: Int) {
        rayBitmap = BitmapFactory.decodeResource(resources, resId)
    }

    private fun updateRayBitmap(rayBitmap: Bitmap) {
        destroyRayDrawable()
        rayDrawable = RayDrawable(rayBitmap).apply {
            addRayDrawable(this)
        }
    }

    private fun destroyRayDrawable() {
        rayDrawable?.let { ray ->
            overlay.remove(ray)
            ray.destroy()
        }
        rayDrawable = null
    }

    private fun addRayDrawable(rayDrawable: RayDrawable) {
        post {
            overlay.add(rayDrawable)
        }
    }
}
