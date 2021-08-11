package com.antiless.support.business.main.utils

import com.antiless.support.business.animxml.AnimXmlActivity
import com.antiless.support.business.devicelayout.DevicesLayoutActivity
import com.antiless.support.business.main.mvp.model.MainActionItemModel
import com.antiless.support.business.raylayout.RayLayoutActivity

/**
 * 主页数据
 *
 * @author lixindong
 */

val MAIN_ACTIONS = listOf(
    MainAction("设备列表布局", DevicesLayoutActivity::class.java),
    MainAction("射线布局", RayLayoutActivity::class.java),
    MainAction("动画 xml 布局", AnimXmlActivity::class.java),
)

class MainAction(
    val title: String,
    val activityClazz: Class<*>
)

fun getMainActionModels(): List<MainActionItemModel> {
    return MAIN_ACTIONS.map { MainActionItemModel(it) }
}
