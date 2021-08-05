package com.antiless.support.business.main.mvp.presenter

import android.content.Intent
import com.antiless.base.mvp.BasePresenter
import com.antiless.support.business.main.mvp.model.MainActionItemModel
import com.antiless.support.business.main.mvp.view.MainActionItemView
import kotlinx.android.synthetic.main.view_main_action_item.view.textAction

/**
 * 主页面动作
 *
 * @author lixindong
 */
class MainActionItemPresenter(view: MainActionItemView) :
    BasePresenter<MainActionItemView, MainActionItemModel>(view) {

    override fun bind(model: MainActionItemModel) {
        view.textAction.text = model.action.title
        view.textAction.setOnClickListener {
            view.context.startActivity(Intent(view.context, model.action.activityClazz))
        }
    }
}
