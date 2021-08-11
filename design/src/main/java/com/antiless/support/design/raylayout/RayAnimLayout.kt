package com.antiless.support.design.raylayout

import android.content.Context
import android.graphics.Bitmap
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

    fun addRay(fromView: View, toView: View): RayDrawable.RayAnimationInfo? {
        return rayDrawable?.addRayForView(this, fromView, toView)
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroyRayDrawable()
    }
}
