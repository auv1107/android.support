package com.antiless.base.widget.floatwindow

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * 悬浮窗 Helper
 *
 * @author lixindong
 */
class FloatWindowHelper : View.OnTouchListener {

    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    lateinit var rootView: FrameLayout
    var showing: Boolean = false
    var closeToEdge: Boolean = false
    var lastAnimator: ValueAnimator? = null

    private var startX = 0
    private var startY = 0
    private var lastX = 0
    private var lastY = 0
    private var currentX = 0
    private var currentY = 0
    private var isMoved = false

    var contentView: View? = null
        set(value) {
            field = value
            rootView.addView(value)
        }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMoved = false
                startX = event.rawX.toInt()
                startY = event.rawY.toInt()
                currentX = startX
                currentY = startY
            }
            MotionEvent.ACTION_MOVE -> {
                lastX = currentX
                lastY = currentY
                currentX = event.rawX.toInt()
                currentY = event.rawY.toInt()
                params.x += currentX - lastX
                params.y += currentY - lastY
                windowManager.updateViewLayout(rootView, params)
            }
            MotionEvent.ACTION_UP -> {
                currentX = event.rawX.toInt()
                currentY = event.rawY.toInt()
                if (abs(currentX - startX) >= 1 || abs(currentY - startY) >= 1) {
                    isMoved = true
                } else {
                    v.performClick()
                }
                if (isMoved && closeToEdge) {
                    animateToEdge()
                }
            }
        }
        return true
    }

    fun initWindow(context: Context, config: Config = Config()) {
        initWindowLayoutParams(config)
        windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        rootView = FrameLayout(context)
        if (!Settings.canDrawOverlays(context)) {
            // 启动Activity让用户授权
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName)).apply {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
            return
        }
    }

    fun showWindow() {
        if (!showing) {
            showing = true
            windowManager.addView(rootView, params)
        }
    }

    fun hideWindow() {
        if (showing) {
            showing = false
            windowManager.removeView(rootView)
        }
    }

    fun toggle() {
        if (showing) hideWindow() else showWindow()
    }

    fun setDragView(dragView: View?) {
        dragView?.setOnTouchListener(this)
    }

    fun setGravity(gravity: Int) {
        params.gravity = gravity
        relayoutRootView()
    }

    fun setX(x: Int) {
        params.x = x
        relayoutRootView()
    }

    fun setY(y: Int) {
        params.y = y
        relayoutRootView()
    }

    private fun initWindowLayoutParams(config: Config) {
        params = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            width = config.width
            height = config.height
            gravity = Gravity.START or Gravity.TOP
        }
    }

    private fun animateToEdge() {
        lastAnimator?.cancel()
        val screenWidth = getScreenWidthPx(rootView.context)
        val start = params.x
        val end = if (params.x < screenWidth / 2) 0 else (screenWidth - rootView.width).toInt()
        lastAnimator = ValueAnimator.ofInt(start, end).apply {
            addUpdateListener {
                params.x = it.animatedValue as Int
                windowManager.updateViewLayout(rootView, params)
            }
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun relayoutRootView() {
        if (rootView.isAttachedToWindow) windowManager.updateViewLayout(rootView, params)
    }

    /**
     * 获取屏幕的宽度，px
     */
    private fun getScreenWidthPx(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    class Config(
        val width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
