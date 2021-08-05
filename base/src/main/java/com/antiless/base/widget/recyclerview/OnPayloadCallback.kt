package com.antiless.base.widget.recyclerview

/**
 * Recycler View Item 局部刷新
 *
 * @author chentian
 */
interface OnPayloadCallback {

    fun onPayload(model: Any, payloads: List<Any>)
}
