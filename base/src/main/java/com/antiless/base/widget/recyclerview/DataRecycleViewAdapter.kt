package com.antiless.base.widget.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.antiless.base.mvp.BaseModel

/**
 * RecyclerView 的数据 adapter
 * 外部使用 [BaseRecycleAdapter]
 *
 * @author liuxu@gotokeep.com (Liu Xu)
 */
abstract class DataRecycleViewAdapter<VH : RecyclerView.ViewHolder, M : BaseModel> : RecyclerView.Adapter<VH>() {

    protected var dataList = mutableListOf<M>()

    override fun getItemCount(): Int {
        return dataList.size
    }

    /**
     * 按照 data adapter 的方式重写方法
     */
    fun add(model: M, position: Int) {
        val newPosition = if (position == LAST_POSITION) itemCount else position
        dataList.add(newPosition, model)
        notifyItemInserted(newPosition)
    }

    fun remove(position: Int) {
        val newPosition = if (position == LAST_POSITION && itemCount > 0) itemCount - 1 else position
        if (newPosition in (LAST_POSITION + 1) until itemCount) {
            dataList.removeAt(newPosition)
            notifyItemRemoved(newPosition)
            notifyItemPositionChange(newPosition)
        }
    }

    fun remove(start: Int, end: Int) {
        setDataWithoutNotify(dataList.subList(0, start) + dataList.subList(end, dataList.size))
        notifyItemRangeRemoved(start, end - start)
    }

    fun replace(model: M, position: Int) {
        dataList[position] = model
        notifyItemChanged(position)
    }

    fun setData(dataList: List<M>) {
        this.dataList = dataList.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * 设置数据，但不notifyDataSetChanged()， 主要配合DiffUtil使用
     */
    fun setDataWithoutNotify(dataList: List<M>) {
        this.dataList = dataList.toMutableList()
    }

    fun getData(): MutableList<M> {
        return dataList
    }

    fun getItem(position: Int): M? {
        return dataList.getOrNull(position)
    }

    fun clear() {
        dataList.clear()
        notifyDataSetChanged()
    }

    private fun notifyItemPositionChange(position: Int) {
        for (i in position until itemCount) {
            notifyItemChanged(i)
        }
    }

    companion object {

        /**
         * 与 RecyclerView.Adapter 的 NO_POSITION 相等, 插入最后一个位置
         */
        private const val LAST_POSITION = -1
    }
}
