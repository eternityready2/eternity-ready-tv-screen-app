package com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.data.OnDemand
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandItemsRecyclerViewAdapter.ViewHolder

class OnDemandItemsRecyclerViewAdapter() : RecyclerView.Adapter<ViewHolder>() {

    private var items: List<OnDemand> = arrayListOf()

    private var onItemFocusedListener: OnItemFocusedListener? = null

    constructor(items: List<OnDemand>) : this() {
        this.items = items
    }

    constructor(onItemFocusedListener: OnItemFocusedListener?) : this() {
        this.onItemFocusedListener = onItemFocusedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.on_demand_item_layout, parent, false)
        return ViewHolder(view, onItemFocusedListener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.title
        holder.onDemandItem = item
        Glide.with(holder.view.context)
            .load(item.logo)
            .placeholder(R.drawable.loading)
            .centerInside()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .error(R.drawable.movie)
            .into(holder.imageView)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(items: List<OnDemand>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setOnItemFocused(onItemFocusedListener: OnItemFocusedListener?) {
        this.onItemFocusedListener = onItemFocusedListener
    }

    inner class ViewHolder(val view: View, private val onItemFocusedListener: OnItemFocusedListener?) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.text_view)
        val imageView: ImageView = view.findViewById(R.id.image_view)

        var onDemandItem: OnDemand? = null

        init {
            view.apply {
                setOnClickListener { onItemFocusedListener?.onItemClicked(onDemandItem?.url) }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) onItemFocusedListener?.onItemFocused(onDemandItem)
                }
            }
        }
    }

    interface OnItemFocusedListener {
        fun onItemFocused(data: OnDemand?)

        fun onItemClicked(url: String?)
    }
}
