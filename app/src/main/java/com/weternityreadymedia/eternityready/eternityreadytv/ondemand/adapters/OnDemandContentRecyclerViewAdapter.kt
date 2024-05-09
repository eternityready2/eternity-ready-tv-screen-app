package com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.data.OnDemand
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandContentRecyclerViewAdapter.ViewHolder
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandItemsRecyclerViewAdapter.OnItemFocusedListener

class OnDemandContentRecyclerViewAdapter() : RecyclerView.Adapter<ViewHolder>() {

    private var items: List<Pair<String, List<OnDemand>>> = listOf()

    private var onItemFocusedListener: OnItemFocusedListener? = null

    constructor(items: List<Pair<String, List<OnDemand>>>) : this() {
        this.items = items
    }

    constructor(onItemFocusedListener: OnItemFocusedListener?) : this() {
        this.onItemFocusedListener = onItemFocusedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.on_demand_content_layout, parent, false)
        return ViewHolder(view, onItemFocusedListener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.first
        (holder.recyclerView.adapter as OnDemandItemsRecyclerViewAdapter).updateList(item.second)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<Pair<String, List<OnDemand>>>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setOnItemFocused(onItemFocusedListener: OnItemFocusedListener?) {
        this.onItemFocusedListener = onItemFocusedListener
    }

    inner class ViewHolder(view: View, private val onItemFocusedListener: OnItemFocusedListener?) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.category_title)
        val recyclerView: RecyclerView = view.findViewById(R.id.items_recycler_view)

        init {
            recyclerView.adapter = OnDemandItemsRecyclerViewAdapter(onItemFocusedListener)
        }
    }
}