package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoItemNormalConfig
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoNormalVH
import kotlin.reflect.KClass

/**
 * 绑定adapter
 * @param context Context 对象
 * @param context Context object
 * @param life LifecycleOwner object
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterBinding(
    context: Context,
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
    //isFold: Boolean = false,
    rvListener: YasuoNormalRVAdapter.() -> YasuoNormalRVAdapter
): YasuoNormalRVAdapter {
    return YasuoNormalRVAdapter(context, life, itemList, headerItemList, footerItemList).bindLife().rvListener().attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterBinding(
    adapter: YasuoNormalRVAdapter,
    rvListener: YasuoNormalRVAdapter.() -> YasuoNormalRVAdapter
): YasuoNormalRVAdapter {
    return adapter.bindLife().rvListener().attach(this)
}

open class YasuoNormalRVAdapter(
    context: Context,
    val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
    //isFold: Boolean = false,
) : YasuoBaseRVAdapter<Any, YasuoNormalVH, YasuoItemNormalConfig<Any, YasuoNormalVH>>(context, itemList, headerItemList, footerItemList), LifecycleObserver {

    init {
        //如果是使用的ObservableArrayList，那么需要注册监听
        this.itemList.addOnListChangedCallback(itemListListener)
        this.headerList.addOnListChangedCallback(headerListListener)
        this.footerList.addOnListChangedCallback(footerListListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemsRemoveListener() {
        this.itemList.removeOnListChangedCallback(itemListListener)
        this.headerList.removeOnListChangedCallback(headerListListener)
        this.footerList.removeOnListChangedCallback(footerListListener)
    }

    /**
     * 绑定生命周期，初始化adapter之后必须调用
     */
    fun bindLife(): YasuoNormalRVAdapter {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

/*
    //内部holder创建的监听集合
    private val innerHolderCreateListenerMap: SparseArray<YasuoVHCreateListener<YasuoVH>> =
        SparseArray()

    //内部holder绑定的监听集合
    private val innerHolderBindListenerMap: SparseArray<YasuoVHBindListener<YasuoVH>> =
        SparseArray()
*/

/*    override fun <L : YasuoVHListener<YasuoVH>> setHolderCreateListener(type: Int, listener: L) {
        innerHolderCreateListenerMap.put(type, listener as YasuoVHCreateListener<YasuoVH>)
    }

    override fun <L : YasuoVHListener<YasuoVH>> setHolderBindListener(type: Int, listener: L) {
        innerHolderBindListenerMap.put(type, listener as YasuoVHBindListener<YasuoVH>)
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YasuoNormalVH {
        val holder = YasuoNormalVH(inflater.inflate(viewType, parent, false))
        itemIdTypes[viewType]?.createListener?.invoke(holder)
        //innerHolderCreateListenerMap[viewType]?.onCreateViewHolder(holder)
        return holder
    }

    override fun onBindViewHolder(holder: YasuoNormalVH, position: Int) {
        itemIdTypes.get(holder.bindingAdapterPosition)
        itemIdTypes[holder.itemViewType]?.bindListener?.invoke(getItem(position), holder)
        //innerHolderBindListenerMap[holder.itemViewType]?.onBindViewHolder(holder, getItem(position))
    }
}

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass 实体类::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
/*
fun <T : Any, Adapter : YasuoRVAdapter> Adapter.onHolderBindAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<T>,
    createListener: ((holder: YasuoVH) -> Unit)? = null,
    bindListener: (T.(holder: YasuoVH, payloads: List<Any>?) -> Unit)? = null
): Adapter {
    itemClassTypes[kClass] = YasuoItemType(itemLayoutId)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : YasuoVHCreateListener<YasuoVH> {
            override fun onCreateViewHolder(holder: YasuoVH) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : YasuoVHBindListener<YasuoVH> {
            override fun onBindViewHolder(holder: YasuoVH, item: Any, payloads: List<Any>?) {
                (item as T).bindListener(holder, payloads)
            }
        })
    }
    return this
}
*/

//TODO 改良api
fun <T : Any, Adapter : YasuoNormalRVAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    kClass: KClass<T>,
    block: YasuoItemNormalConfig<Any, YasuoNormalVH>.() -> Unit
): Adapter {
    val itemType = YasuoItemNormalConfig<Any, YasuoNormalVH>(itemLayoutId)
    itemClassTypes[kClass] = itemType
    itemIdTypes[itemLayoutId] = itemType
    itemType.block()
    return this
}

fun <T : Any, Adapter : YasuoNormalRVAdapter> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    block: YasuoItemNormalConfig<Any, YasuoNormalVH>.() -> Unit
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    val itemType = YasuoItemNormalConfig<Any, YasuoNormalVH>(loadMoreLayoutId)
    itemClassTypes[loadMoreLayoutItem::class] = itemType
    itemIdTypes[loadMoreLayoutId] = itemType
    itemType.block()
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param kClass 实体类::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
/*fun <T : Any, Adapter : YasuoRVAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    kClass: KClass<T>,
    createListener: ((holder: YasuoVH) -> Unit)? = null,
    bindListener: (T.(holder: YasuoVH) -> Unit)? = null
): Adapter {
    itemClassTypes[kClass] = YasuoItemType(itemLayoutId)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : YasuoVHCreateListener<YasuoVH> {
            override fun onCreateViewHolder(holder: YasuoVH) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : YasuoVHBindListener<YasuoVH> {
            override fun onBindViewHolder(holder: YasuoVH, item: Any, payloads: List<Any>?) {
                (item as T).bindListener(holder)
            }
        })
    }
    return this
}*/

/**
 * 建立loadMore数据类与布局文件之间的匹配关系
 * @param loadMoreLayoutId 加载更多布局id
 * @param loadMoreLayoutItem 加载更多布局对应的实体
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
/*
fun <T : Any, Adapter : YasuoRVAdapter> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    createListener: ((holder: YasuoVH) -> Unit)? = null,
    bindListener: (T.(holder: YasuoVH) -> Unit)? = null
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    itemClassTypes[loadMoreLayoutItem::class] = YasuoItemType(loadMoreLayoutId)
    if (createListener != null) {
        setHolderCreateListener(loadMoreLayoutId, object : YasuoVHCreateListener<YasuoVH> {
            override fun onCreateViewHolder(holder: YasuoVH) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(loadMoreLayoutId, object : YasuoVHBindListener<YasuoVH> {
            override fun onBindViewHolder(holder: YasuoVH, item: Any, payloads: List<Any>?) {
                (item as T).bindListener(holder)
            }
        })
    }
    return this
}*/