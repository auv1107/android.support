package com.antiless.base.widget.floatwindow

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.View
import android.view.ViewGroup
import com.antiless.base.widget.floatwindow.FloatWindowHelper

/**
 * 悬浮窗宿主
 */
abstract class FloatWindowHostService : Service() {

    private val floatWindowHelper = FloatWindowHelper()
    val contentView get() = floatWindowHelper.contentView

    override fun onCreate() {
        super.onCreate()
        floatWindowHelper.initWindow(applicationContext, getConfig())
        floatWindowHelper.contentView = buildContentView(floatWindowHelper.rootView)
        setDragView(getDragView())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        floatWindowHelper.showWindow()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        floatWindowHelper.hideWindow()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    abstract fun buildContentView(parent: ViewGroup): View
    open fun getDragView(): View? = null
    open fun getConfig() = FloatWindowHelper.Config()

    fun show() {
        floatWindowHelper.showWindow()
    }

    fun hide() {
        floatWindowHelper.hideWindow()
    }

    fun toggle() {
        floatWindowHelper.toggle()
    }

    fun setDragView(dragView: View?) {
        floatWindowHelper.setDragView(dragView)
    }

    fun setCloseToEdge(closeToEdge: Boolean) {
        floatWindowHelper.closeToEdge = closeToEdge
    }

    fun setGravity(gravity: Int) {
        floatWindowHelper.setGravity(gravity)
    }

    fun setX(x: Int) {
        floatWindowHelper.setX(x)
    }

    fun setY(y: Int) {
        floatWindowHelper.setY(y)
    }
}
