package com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters

import android.app.Activity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandContentRecyclerViewAdapter

class OnDemandRecyclerViewAdapter() :
    RecyclerView.Adapter<OnDemandRecyclerViewAdapter.ViewHolder>() {

    private var items: List<String> = arrayListOf()
    private var onCategoryFocused: ((category: String, position: Int) -> Unit)? = null

    constructor(items: List<String>) : this() {
        this.items = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.on_demand_category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item
        holder.onCategoryFocused = onCategoryFocused
    }

    fun updateList(items: List<String>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setOnCategoryFocused(onCategoryFocused: (category: String, position: Int) -> Unit) {
        this.onCategoryFocused = onCategoryFocused
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.on_demand_category_text)
        var onCategoryFocused: ((category: String, position: Int) -> Unit)? = null

        init {
            view.isFocusable = true
            view.isFocusableInTouchMode = true

            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    onCategoryFocused?.invoke(textView.text.toString(), layoutPosition)
                }
            }

            view.setOnKeyListener { v, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false

                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        val activity = v.context as? Activity ?: return@setOnKeyListener false
                        val tabsContainer =
                            activity.findViewById<LinearLayout>(R.id.tabs_container) ?: return@setOnKeyListener false
                        tabsContainer.post {
                            tabsContainer.getChildAt(0)?.requestFocus()
                        }
                        true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // FIXED: Use layoutPosition (current category) instead of hardcoded 0
                        val activity = v.context as? Activity ?: return@setOnKeyListener false
                        val verticalRv =
                            activity.findViewById<RecyclerView>(R.id.recycler_category_items)
                                ?: return@setOnKeyListener false
                        val lm = verticalRv.layoutManager as? LinearLayoutManager ?: return@setOnKeyListener false
                        
                        lm.scrollToPositionWithOffset(layoutPosition, 0)
                        verticalRv.post {
                            val rowVH =
                                verticalRv.findViewHolderForAdapterPosition(layoutPosition)
                                        as? OnDemandContentRecyclerViewAdapter.ViewHolder
                            val innerRv = rowVH?.recyclerView
                            val innerLm = innerRv?.layoutManager as? LinearLayoutManager
                            innerLm?.scrollToPositionWithOffset(0, 0)
                            innerRv?.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }
}
