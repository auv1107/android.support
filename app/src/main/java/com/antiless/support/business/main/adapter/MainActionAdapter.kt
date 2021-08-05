package com.antiless.support.business.main.adapter

import com.antiless.base.widget.recyclerview.BaseRecycleAdapter
import com.antiless.support.business.main.mvp.model.MainActionItemModel
import com.antiless.support.business.main.mvp.presenter.MainActionItemPresenter
import com.antiless.support.business.main.mvp.view.MainActionItemView

/**
 * 主页项目列表
 *
 * @author lixindong
 */
class MainActionAdapter : BaseRecycleAdapter() {

    override fun registerMVP() {
        register(
            MainActionItemModel::class.java,
            { MainActionItemView.newInstance(it) },
            { MainActionItemPresenter(it) }
        )
    }
}
