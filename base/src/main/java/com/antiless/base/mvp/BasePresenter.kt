package com.antiless.base.mvp

import androidx.recyclerview.widget.RecyclerView

/**
 * 描述了将一个 Model 对象绑定到 View 上的逻辑. Presenter 描述了这个 View 的业务逻辑，
 * 对于业务逻辑，我们采用组合模式来大幅度提升 Presenter 的复用性.
 *
 * Presenter 的基础类，所有 Presenter 需要继承此类，重写 bind 方法，对 BaseView 和 BaseModel 进行绑定
 *
 * @author liuxu@gotokeep.com (Liu Xu)
 */
abstract class BasePresenter<V : BaseView, M>(val view: V) {

    var viewHolder: RecyclerView.ViewHolder? = null

    /**
     * 在数据到来之前，绑定一些常量到 View 上
     */
    open fun preBind() = Unit

    /**
     * 将 Model 绑定到 View 单元上。
     *
     * @param model model
     */
    abstract fun bind(model: M)

    /**
     * 在 Presenter 被遗弃时被调用。
     *
     *
     * 一些 Presenter，在遇到重用的场景，比如 ListView 或者 RecyclerView 时，
     * 这时旧的Presenter就会被遗弃或者暂时回收，此时这个方法会被调用。
     *
     *
     * 如果你的 Presenter 在被丢弃时还可能工作(例如反注册listener)，那么你需要重写这个方法，
     * 并保证调用完这个方法能正常的回收或遗弃此 Presenter。
     */
    open fun unbind() = Unit

    /**
     * 获取当前位置
     *
     * @return -1 表示获取失败
     */
    protected fun getAdapterPosition(): Int {
        return viewHolder?.adapterPosition ?: -1
    }
}
