package com.weternityreadymedia.eternityready.eternityreadytv.ondemand

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.data.OnDemand
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandContentRecyclerViewAdapter
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandItemsRecyclerViewAdapter.OnItemFocusedListener
import com.weternityreadymedia.eternityready.eternityreadytv.ondemand.adapters.OnDemandRecyclerViewAdapter
import com.weternityreadymedia.eternityready.eternityreadytv.viewmodel.PresenterViewModel
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebViewDisplayFragment
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebviewActivity

class OnDemandFragment : Fragment(), OnItemFocusedListener {

    private lateinit var viewModel: PresenterViewModel

    private val recyclerViewCategoriesAdapter = OnDemandRecyclerViewAdapter()
    private val recyclerViewCategoryItemsAdapter = OnDemandContentRecyclerViewAdapter(this)

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
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_on_demand, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewCategoriesAdapter.setOnCategoryFocused { _: String, position: Int ->
            val moreDetailsLayout = view.findViewById<ViewGroup>(R.id.on_demand_more_details_layout)
            if (moreDetailsLayout.visibility == ViewGroup.VISIBLE) moreDetailsLayout.visibility = ViewGroup.INVISIBLE
            view.findViewById<RecyclerView>(R.id.recycler_category_items).smoothScrollToPosition(position)
        }

        val recyclerViewCategories = view.findViewById<RecyclerView>(R.id.on_demand_categories)
        recyclerViewCategories.adapter = recyclerViewCategoriesAdapter
        recyclerViewCategories.runWhenReady {
            val item = (recyclerViewCategories.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            recyclerViewCategories.findViewHolderForAdapterPosition(item)?.itemView?.requestFocus()
        }

        val recyclerViewItems = view.findViewById<RecyclerView>(R.id.recycler_category_items)
        recyclerViewItems.adapter = recyclerViewCategoryItemsAdapter
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
            putExtra(WebViewDisplayFragment.URL_KEY, url)
        }
        startActivity(intent)
    }

    private fun RecyclerView.runWhenReady(action: () -> Unit) {
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