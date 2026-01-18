package com.weternityreadymedia.eternityready.eternityreadytv.ondemand

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.data.OnDemand
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandContentRecyclerViewAdapter
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandItemsRecyclerViewAdapter.OnItemFocusedListener
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandRecyclerViewAdapter
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.helper.CustomLinearSnapHelper
import com.weternityreadymedia.eternityready.eternityreadytv.viewmodel.PresenterViewModel
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebViewDisplayFragment
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebviewActivity

class OnDemandFragment : Fragment(), OnItemFocusedListener {

    private lateinit var viewModel: PresenterViewModel

    private val recyclerViewCategoriesAdapter = OnDemandRecyclerViewAdapter()
    private val recyclerViewCategoryItemsAdapter = OnDemandContentRecyclerViewAdapter(this)
    private var selectedTabIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.NewInstanceFactory()
        )[PresenterViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (viewModel.onDemandLiveData.value != null) {
            recyclerViewCategoriesAdapter.updateList(viewModel.onDemandLiveData.value!!.categories)
            recyclerViewCategoryItemsAdapter.updateItems(viewModel.onDemandLiveData.value!!.displayChannels.toList())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_on_demand, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs(view)
        setupCategories(view)
        setupItems(view)

        viewModel.onDemandLiveData.observe(viewLifecycleOwner) { onDemandData ->
            recyclerViewCategoriesAdapter.updateList(onDemandData.categories)
            recyclerViewCategoryItemsAdapter.updateItems(onDemandData.displayChannels.toList())
        }
    }

    private fun setupTabs(view: View) {
        val tabsContainer = view.findViewById<LinearLayout>(R.id.tabs_container)
        val tabTitles = listOf("All", "Movies", "Music", "Radio")
        
        tabsContainer.removeAllViews()
        
        tabTitles.forEachIndexed { index, title ->
            val tabButton = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_tab_button, tabsContainer, false) as TextView
            
            tabButton.text = title
            tabButton.tag = index
            
            val backgroundResId = when {
                index == 0 -> R.drawable.tab_button_background
                index == tabTitles.size - 1 -> R.drawable.tab_button_right_background
                else -> R.drawable.tab_button_middle_background
            }
            tabButton.setBackgroundResource(backgroundResId)
            
            tabButton.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) updateTabSelection(tabsContainer, index)
            }
            
            tabButton.setOnClickListener {
                updateTabSelection(tabsContainer, index)
                tabButton.requestFocus()
            }
            
            tabsContainer.addView(tabButton)
        }
        
        tabsContainer.runWhenReady {
            updateTabSelection(tabsContainer, 0)
            tabsContainer.getChildAt(0)?.requestFocus()
        }
    }

    private fun updateTabSelection(container: LinearLayout, selectedIndex: Int) {
        selectedTabIndex = selectedIndex

        val category = when (selectedIndex) {
            0 -> "all"
            1 -> "movies"
            2 -> "music"
            3 -> "radio"
            else -> "all"
        }
        viewModel.refreshOnDemandForCategory(category)

        val childCount = container.childCount
        
        for (i in 0 until childCount) {
            val tabButton = container.getChildAt(i) as TextView
            val isSelected = i == selectedIndex
            
            val backgroundResId = when {
                i == 0 && isSelected -> R.drawable.tab_button_active_left
                i == childCount - 1 && isSelected -> R.drawable.tab_button_active_right
                i == 0 -> R.drawable.tab_button_background
                i == childCount - 1 -> R.drawable.tab_button_right_background
                isSelected -> R.drawable.tab_button_active_middle
                else -> R.drawable.tab_button_middle_background
            }
            
            tabButton.setBackgroundResource(backgroundResId)
            tabButton.setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#9ca3af"))
            tabButton.elevation = if (isSelected) 8f else 0f
            tabButton.translationY = if (isSelected) -2f else 0f
        }
    }

    private fun setupCategories(view: View) {
        recyclerViewCategoriesAdapter.setOnCategoryFocused { _: String, position: Int ->
            val moreDetailsLayout = view.findViewById<ViewGroup>(R.id.on_demand_more_details_layout)
            if (moreDetailsLayout.visibility == ViewGroup.VISIBLE) moreDetailsLayout.visibility = ViewGroup.INVISIBLE
            (view.findViewById<RecyclerView>(R.id.recycler_category_items).layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(position, 0)
        }

        val recyclerViewCategories = view.findViewById<RecyclerView>(R.id.on_demand_categories)
        recyclerViewCategories.adapter = recyclerViewCategoriesAdapter
        recyclerViewCategories.runWhenReady {
            val item = (recyclerViewCategories.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            recyclerViewCategories.findViewHolderForAdapterPosition(item)?.itemView?.requestFocus()
        }
    }

    private fun setupItems(view: View) {
        val recyclerViewItems = view.findViewById<RecyclerView>(R.id.recycler_category_items)
        recyclerViewItems.adapter = recyclerViewCategoryItemsAdapter

        val snapHelper: SnapHelper = CustomLinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerViewItems)
    }

    override fun onItemFocused(data: OnDemand?) {
        val parentView = requireView().findViewById<ViewGroup>(R.id.on_demand_more_details_layout)
        if (parentView.visibility != ViewGroup.VISIBLE) parentView.visibility = ViewGroup.VISIBLE

        parentView.apply {
            findViewById<TextView>(R.id.detail_title).text = data?.title
            findViewById<TextView>(R.id.detail_metadata).text = data?.category
            findViewById<TextView>(R.id.detail_description).text = data?.description
        }
    }

    override fun onItemClicked(url: String?) {
        val intent = Intent(requireActivity(), WebviewActivity::class.java).apply {
            val containsIframe = url?.contains("<iframe", ignoreCase = true) ?: false
            if (containsIframe) putExtra(WebViewDisplayFragment.DATA_URL_KEY, url)
            else putExtra(WebViewDisplayFragment.URL_KEY, url)
        }
        startActivity(intent)
    }

    private fun View.runWhenReady(action: () -> Unit) {
        val globalLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                action()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    companion object {
        val TAG: String = OnDemandFragment::class.java.name
        fun instance() = OnDemandFragment()
    }
}
