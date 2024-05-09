package com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.weternityreadymedia.eternityready.eternityreadytv.R

class OnDemandRecyclerViewAdapter() : RecyclerView.Adapter<OnDemandRecyclerViewAdapter.ViewHolder>() {

    private var items: List<String> = arrayListOf()

    private var onCategoryFocused: ((category: String, position: Int) -> Unit)? = null

    constructor(items: List<String>) : this() {
        this.items = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.on_demand_category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item
        holder.onCategoryFocused = onCategoryFocused
    }

    @SuppressLint("NotifyDataSetChanged")
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
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) onCategoryFocused?.invoke(textView.text.toString(), layoutPosition)
            }
        }
    }
}