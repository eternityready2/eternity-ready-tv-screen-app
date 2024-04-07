package com.weternityreadymedia.eternityready.eternityreadytv.views.search

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.lifecycle.lifecycleScope
import com.weternityreadymedia.eternityready.eternityreadytv.CardPresenter
import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebViewDisplayFragment
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebviewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment: SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    private lateinit var mObjectAdapter: ArrayObjectAdapter
    private lateinit var searchUtil: SearchUtil

    private var queriedBefore: Boolean = false
    private val cardPresenter: CardPresenter = CardPresenter()

    private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: Array<Channel>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArray(SearchActivity.SEARCHABLE_LIST, Channel::class.java)
        } else {
            arguments?.getParcelableArray(SearchActivity.SEARCHABLE_LIST)?.map { it as Channel }?.toTypedArray()
        }

        searchUtil = if (args != null) {
            SearchUtil(args)
        } else {
            SearchUtil()
        }

        setSearchResultProvider(this)

        mObjectAdapter = ArrayObjectAdapter(ListRowPresenter())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListeners()
    }

    override fun getResultsAdapter() = mObjectAdapter

    override fun onQueryTextChange(newQuery: String?): Boolean {
        lifecycleScope.launch {
            if (queriedBefore) {
                delay(200L)
            }
            searchQuery = newQuery

            mObjectAdapter.clear()
            val rowPresenter = ArrayObjectAdapter(cardPresenter)

            for (channel in searchUtil.filterMatchingList(newQuery)) {
                rowPresenter.add(channel)
            }
            mObjectAdapter.add(ListRow(rowPresenter))

            if (!queriedBefore) {
                queriedBefore = true
            }
        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query == searchQuery) return false

        lifecycleScope.launch {
            mObjectAdapter.clear()
            val rowPresenter = ArrayObjectAdapter(cardPresenter)

            for (channel in searchUtil.filterMatchingList(query)) {
                rowPresenter.add(channel)
            }

            mObjectAdapter.add(ListRow(rowPresenter))

            if (queriedBefore) {
                queriedBefore = false
            }
        }
        return true
    }

    fun isKeyBoardVisible() = ViewCompat.getRootWindowInsets(view!!)?.isVisible(WindowInsetsCompat.Type.ime()) ?: true

    private fun setupListeners() {
        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Channel) {
                val intent = Intent(activity!!, WebviewActivity::class.java).apply {
                    putExtra(WebViewDisplayFragment.URL_KEY, item.url)
                }
                startActivity(intent)
            }
        }
    }

    companion object {
        @JvmField
        val TAG: String = SearchFragment::class.java.name

        @JvmStatic
        fun newInstance(channels: Array<out Parcelable>?) = SearchFragment().apply {
            arguments = Bundle().apply {
                putParcelableArray(SearchActivity.SEARCHABLE_LIST, channels)
            }
        }
    }
}