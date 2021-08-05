package com.antiless.base.mvp

import android.view.View

/**
 * MVP 的 View
 * [BaseModel] [BasePresenter]
 *
 * @author chentian
 */
interface BaseView {

    /**
     * 提供这个 view 的引用，保护实体 view
     */
    fun getView(): View
}
