package com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.weternityreadymedia.eternityready.eternityreadytv.R

class OnDemandTabsAdapter : RecyclerView.Adapter<OnDemandTabsAdapter.ViewHolder>() {
    
    private val tabs = mutableListOf<String>()
    private var selectedPosition = 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tab_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_on_demand_tab, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tab = tabs[position]
        holder.title.text = tab

        // Apply active/inactive styling
        if (position == selectedPosition) {
            holder.title.setBackgroundResource(R.drawable.tab_background_active)
            holder.title.setTextColor(Color.WHITE)
            holder.title.elevation = 8f
        } else {
            holder.title.setBackgroundResource(R.drawable.tab_background)
            holder.title.setTextColor(Color.parseColor("#9ca3af"))
            holder.title.elevation = 0f
        }

        // Handle focus change for selection
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                selectedPosition = position
                notifyDataSetChanged()
            }
        }

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = tabs.size

    fun updateTabs(newTabs: List<String>) {
        tabs.clear()
        tabs.addAll(newTabs)
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
}
