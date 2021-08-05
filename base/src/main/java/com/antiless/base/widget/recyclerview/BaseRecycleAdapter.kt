package com.antiless.base.widget.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.antiless.base.mvp.BaseModel
import com.antiless.base.mvp.BasePresenter
import com.antiless.base.mvp.BaseView
import kotlin.math.max

/**
 * 继承这个类之后，需要实现 [.registerMVP] 方法
 * 支持分页
 *
 * @author liuxu@gotokeep.com (Liu Xu)
 * @author chentian
 */
abstract class BaseRecycleAdapter :
    DataRecycleViewAdapter<BaseRecycleAdapter.BaseViewHolder, BaseModel>() {

    /**
     * 子类需要实现的方法，调用 register 逐个注册用到的 model
     */
    protected abstract fun registerMVP()

    var onLoadMoreListener: (() -> Unit)? = null

    private var canLoadMore = true
    private var isLoadingMore = false

    /**
     * Model -> ViewType
     */
    private val itemViewTypeMap = mutableMapOf<Class<out BaseModel>, Int>()

    /**
     * 创建 View 的方法列表
     * Index 即 ViewType
     */
    private val viewCreatorList = mutableListOf<(ViewGroup) -> Any>()

    /**
     * 创建 Presenter 的方法列表
     * Index 即 ViewType
     */
    private val presenterCreatorList = mutableListOf<(Any) -> BasePresenter<BaseView, BaseModel>?>()

    init {
        @Suppress("LeakingThis")
        registerMVP()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType >= 0) {
            val baseView = viewCreatorList[viewType].invoke(parent) as BaseView
            val presenter =
                presenterCreatorList[viewType].invoke(baseView)
            BaseViewHolder(baseView.getView(), presenter)
        } else {
            BaseViewHolder(View(parent.context), null)
        }
    }

    @CallSuper
    override fun onBindViewHolder(viewHolder: BaseViewHolder, position: Int) {
        viewHolder.presenter?.let {
            getItem(position)?.let { model: BaseModel ->
                // 由于 presenter 是重用的，所以先执行解绑，再绑定
                it.unbind()
                // 绑定数据
                it.bind(model)
            }
        }
        if (canLoadMore && !isLoadingMore && position >= max(dataList.size - LOAD_MORE_COUNT, 0)) {
            onLoadMoreListener?.invoke()
            isLoadingMore = true
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isNotEmpty()) {
            getItem(position)?.let { model ->
                (holder.presenter as? OnPayloadCallback)?.onPayload(model, payloads)
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val model = getItem(position) ?: return -1
        return run {
            val clazz: Class<out BaseModel> = model::class.java
            try {
                getViewTypeByModel(clazz)
            } catch (ex: NullPointerException) {
                val message = "model $clazz not registered in ${this.javaClass.name}"
                throw IllegalStateException(message)
            }
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        holder.presenter?.unbind()
    }

    /**
     * 分页加载结束后，更新状态
     */
    fun stopLoadAndUpdate(canLoadMore: Boolean) {
        this.isLoadingMore = false
        this.canLoadMore = canLoadMore
    }

    /**
     * 按注册的顺序对 dataList 排序
     */
    fun sortByRegisterOrder() {
        dataList.sortWith(RegisterOrderComparator())
    }

    /**
     * 注册所有用到的 model
     *
     * @param clazz            xxxModel.class
     * @param viewCreator      创建 View 的方法，xxxView::newInstance
     * @param presenterCreator 创建 Presenter 的方法，(PresenterCreator<xxxView>) xxxPresenter::new。
     * 传 null 表示这个 view 不需要 bind
     */
    fun <V : BaseView, M : BaseModel> register(
        clazz: Class<M>,
        viewCreator: (ViewGroup) -> V,
        presenterCreator: ((V) -> BasePresenter<V, M>)?,
    ) {
        if (itemViewTypeMap.containsKey(clazz)) {
            val message = "Model ${clazz.name} already registered in this adapter. Register each model only once. "
            throw IllegalStateException(message)
        }
        itemViewTypeMap[clazz] = viewCreatorList.size
        viewCreatorList.add(viewCreator)
        @Suppress("UNCHECKED_CAST")
        presenterCreatorList.add(
            presenterCreator as ((Any) -> BasePresenter<BaseView, BaseModel>?)? ?: EMPTY_PRESENTER_CREATOR
        )
    }

    fun isRegistered(clazz: Class<out BaseModel>): Boolean {
        return itemViewTypeMap.containsKey(clazz)
    }

    fun getViewTypeByModel(clazz: Class<out BaseModel>): Int {
        return if (itemViewTypeMap.containsKey(clazz)) itemViewTypeMap[clazz]!! else -1
    }

    class BaseViewHolder(itemView: View, val presenter: BasePresenter<BaseView, BaseModel>?) :
        RecyclerView.ViewHolder(itemView) {
        init {
            presenter?.viewHolder = this
        }
    }

    private inner class RegisterOrderComparator<M : BaseModel> : Comparator<M> {
        override fun compare(o1: M, o2: M): Int {
            val type1 = itemViewTypeMap[o1::class.java]
            val type2 = itemViewTypeMap[o2::class.java]
            return type1!!.compareTo(type2!!)
        }
    }

    companion object {

        private const val LOAD_MORE_COUNT = 10
        private val EMPTY_PRESENTER_CREATOR: (Any) -> BasePresenter<BaseView, BaseModel>? = { null }
    }
}
