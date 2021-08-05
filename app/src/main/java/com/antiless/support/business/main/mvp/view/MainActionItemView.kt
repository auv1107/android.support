package com.antiless.support.business.main.mvp.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.antiless.base.mvp.BaseView
import com.antiless.support.R
import com.antiless.support.business.main.mvp.presenter.MainActionItemPresenter
import com.antiless.support.exts.inflate

/**
 * 主页面动作
 * [MainActionItemPresenter]
 *
 * @author lixindong
 */
class MainActionItemView : ConstraintLayout, BaseView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun getView() = this

    companion object {

        fun newInstance(parent: ViewGroup): MainActionItemView {
            return parent.inflate(R.layout.view_main_action_item)
        }
    }
}
