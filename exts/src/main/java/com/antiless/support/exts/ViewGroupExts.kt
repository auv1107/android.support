package com.antiless.support.exts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * 对 ViewGroup 类的扩展函数
 *
 * @author liuxu@gotokeep.com (Liu Xu)
 */

/**
 * inflate 一个 View 到 ViewGroup 上
 */
inline fun <reified T : View> ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): T {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot) as T
}

/**
 * 取 子 View 在 父 View 中的位置
 */
fun ViewGroup.getChildIndex(child: View?): Int {
    return if (childCount == 0) -1 else indexOfChild(child)
}

fun ViewGroup.foreach(action: (View) -> Unit) {
    foreachIndexed { _, view -> action(view) }
}

fun ViewGroup.foreachIndexed(action: (Int, View) -> Unit) {
    for (i in 0 until childCount) {
        action(i, getChildAt(i))
    }
}
