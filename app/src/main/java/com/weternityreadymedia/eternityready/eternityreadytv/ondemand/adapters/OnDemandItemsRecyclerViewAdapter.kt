package com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters

import android.app.Activity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.data.OnDemand
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandItemsRecyclerViewAdapter.ViewHolder

class OnDemandItemsRecyclerViewAdapter() : RecyclerView.Adapter<ViewHolder>() {

    private var items: List<OnDemand> = emptyList()
    private var onItemFocusedListener: OnItemFocusedListener? = null

    constructor(items: List<OnDemand>) : this() {
        this.items = items
    }

    constructor(onItemFocusedListener: OnItemFocusedListener?) : this() {
        this.onItemFocusedListener = onItemFocusedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.on_demand_item_layout, parent, false)
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

    fun updateList(items: List<OnDemand>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setOnItemFocused(onItemFocusedListener: OnItemFocusedListener?) {
        this.onItemFocusedListener = onItemFocusedListener
    }

    inner class ViewHolder(
        val view: View,
        private val onItemFocusedListener: OnItemFocusedListener?
    ) : RecyclerView.ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.text_view)
        val imageView: ImageView = view.findViewById(R.id.image_view)
        var onDemandItem: OnDemand? = null

        init {
            view.isFocusable = true
            view.isFocusableInTouchMode = true

            view.setOnClickListener {
                onItemFocusedListener?.onItemClicked(onDemandItem?.url)
            }

            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) onItemFocusedListener?.onItemFocused(onDemandItem)
            }

            view.setOnKeyListener { v, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false

                val horizontalRv = v.parent as? RecyclerView ?: return@setOnKeyListener false
                val contentRowView = horizontalRv.parent as? View ?: return@setOnKeyListener false
                val verticalRv = contentRowView.parent as? RecyclerView ?: return@setOnKeyListener false
                val verticalLm = verticalRv.layoutManager as? LinearLayoutManager
                    ?: return@setOnKeyListener false
                val currentRow = verticalRv.getChildAdapterPosition(contentRowView)
                if (currentRow == RecyclerView.NO_POSITION) return@setOnKeyListener false

                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        if (currentRow > 0) {
                            // Go to previous content row, first item
                            val targetRow = currentRow - 1
                            verticalLm.scrollToPositionWithOffset(targetRow, 0)
                            verticalRv.post {
                                val rowVH =
                                    verticalRv.findViewHolderForAdapterPosition(targetRow)
                                            as? OnDemandContentRecyclerViewAdapter.ViewHolder
                                val innerRv = rowVH?.recyclerView
                                val innerLm = innerRv?.layoutManager as? LinearLayoutManager
                                innerLm?.scrollToPositionWithOffset(0, 0)
                                innerRv?.findViewHolderForAdapterPosition(0)
                                    ?.itemView?.requestFocus()
                            }
                            true
                        } else {
                            // First content row -> Category 1 item 0
                            val activity = v.context as? Activity
                                ?: return@setOnKeyListener false
                            val categoriesRv =
                                activity.findViewById<RecyclerView>(R.id.on_demand_categories)
                                    ?: return@setOnKeyListener false
                            val catLm = categoriesRv.layoutManager as? LinearLayoutManager
                            catLm?.scrollToPositionWithOffset(0, 0)
                            categoriesRv.post {
                                categoriesRv.findViewHolderForAdapterPosition(0)
                                    ?.itemView?.requestFocus()
                            }
                            true
                        }
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // Go to next content row, first item if exists
                        val targetRow = currentRow + 1
                        val totalRows = verticalRv.adapter?.itemCount ?: 0
                        if (targetRow < totalRows) {
                            verticalLm.scrollToPositionWithOffset(targetRow, 0)
                            verticalRv.post {
                                val rowVH =
                                    verticalRv.findViewHolderForAdapterPosition(targetRow)
                                            as? OnDemandContentRecyclerViewAdapter.ViewHolder
                                val innerRv = rowVH?.recyclerView
                                val innerLm = innerRv?.layoutManager as? LinearLayoutManager
                                innerLm?.scrollToPositionWithOffset(0, 0)
                                innerRv?.findViewHolderForAdapterPosition(0)
                                    ?.itemView?.requestFocus()
                            }
                            true
                        } else {
                            false
                        }
                    }

                    else -> false
                }
            }
        }
    }

    interface OnItemFocusedListener {
        fun onItemFocused(data: OnDemand?)
        fun onItemClicked(url: String?)
    }
}
