package com.antiless.base.widget.recyclerview

/**
 * 空状态 View 的接口，为了让 architecture 层也可以访问
 *
 * @author chentian
 */
interface EmptyViewInterface {

    fun updateViewByState(state: State)

    /**
     * 页面状态
     */
    enum class State {

        /**
         * 一切正常，隐藏当前 View
         */
        NORMAL,

        /**
         * 无网络
         */
        NO_NETWORK,

        /**
         * 直播中异常中断
         */
        LIVE_INTERRUPT,

        /**
         * 课程列表为空
         */
        EMPTY_COURSE,

        /**
         * 正在排课
         */
        EMPTY_PLANNING,

        /**
         * 暂无筛选结果
         */
        EMPTY_FILTER,

        /**
         * 服务器开小差
         */
        SERVER_ERROR,

        /**
         * Wi-Fi 修复中
         */
        WIFI_REPAIRING
    }
}
