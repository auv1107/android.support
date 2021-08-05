package com.antiless.base.widget.recyclerview

import com.antiless.base.mvp.BaseModel

/**
 * 列表加载的数据
 *
 * @author chentian
 */
class PagingDataContent(

    /**
     * 当前页的数据
     * null 表示拉取列表失败
     */
    val modelList: List<BaseModel>? = listOf(),

    /**
     * 是否是刷新操作
     */
    val isRefresh: Boolean = true,

    /**
     * 是否有下一页
     */
    val canLoadMore: Boolean = false
)
