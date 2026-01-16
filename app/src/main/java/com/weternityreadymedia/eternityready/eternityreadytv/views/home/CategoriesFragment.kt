package com.weternityreadymedia.eternityready.eternityreadytv.views.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.BrowseFrameLayout
import androidx.leanback.widget.BrowseFrameLayout.OnFocusSearchListener
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.weternityreadymedia.eternityready.eternityreadytv.CardPresenter
import com.weternityreadymedia.eternityready.eternityreadytv.DetailsActivity
import com.weternityreadymedia.eternityready.eternityreadytv.Movie
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel
import com.weternityreadymedia.eternityready.eternityreadytv.viewmodel.PresenterViewModel
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebViewDisplayFragment
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebviewActivity
import java.util.Timer
import java.util.TimerTask


/**
 * Loads a grid of cards with movies to browse.
 */
class CategoriesFragment : BrowseSupportFragment() {

    private lateinit var viewModel: PresenterViewModel

    private val mHandler = Handler(Looper.myLooper()!!)
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        prepareBackgroundManager()

        setupUIElements()

        loadRows()

        setupEventListeners()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workaroundFocus()
        parentFragment?.view?.findViewById<Button>(R.id.categories_live)?.requestFocus() // this ain't working
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity(), NewInstanceFactory())[PresenterViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyBackgroundManager()
    }

    private fun prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(activity)
        if (!mBackgroundManager.isAttached) mBackgroundManager.attach(requireActivity().window)
        mDefaultBackground = ContextCompat.getDrawable(requireActivity(),
            R.drawable.default_background
        )
        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    private fun destroyBackgroundManager() {
        mBackgroundManager.release() //shouldn't release yet if multiple fragments update background
        mBackgroundTimer?.cancel()
    }

    private fun setupUIElements() {
        val appTitle = getString(R.string.browse_title)
        val image = resources.getDrawable(R.drawable.logo_s)
        image.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)

        val spannableString = SpannableString(appTitle)
        val imageSpan = ImageSpan(image, ImageSpan.ALIGN_BOTTOM)
        spannableString.setSpan(imageSpan, 0, appTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // title = spannableString
        // over title
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // set fastLane (or headers) background color
        // brandColor = ContextCompat.getColor(requireActivity(), R.color.fastlane_background)
        brandColor = ContextCompat.getColor(requireActivity(), android.R.color.black)
        // set search icon color
        searchAffordanceColor = ContextCompat.getColor(requireActivity(), R.color.search_opaque)
    }

    private fun loadRows() {

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()

        for (channelName in viewModel.channelsLiveData.value?.displayChannels?.keys!!) {
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)

            for (channel in viewModel.channelsLiveData.value!!.displayChannels[channelName]!!) {
                listRowAdapter.add(channel)
            }

            val header = HeaderItem(channelName)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }

        adapter = rowsAdapter

//        val gridHeader = HeaderItem(NUM_ROWS.toLong(), "PREFERENCES")

//        val mGridPresenter = GridItemPresenter()
//        val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
//        gridRowAdapter.add(resources.getString(R.string.grid_view))
//        gridRowAdapter.add(getString(R.string.error_fragment))
//        gridRowAdapter.add(resources.getString(R.string.personal_settings))
//        rowsAdapter.add(ListRow(gridHeader, gridRowAdapter))
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()

//        setOnSearchClickedListener {
//            val intent = Intent(requireActivity(), SearchActivity::class.java).apply {
//                putExtra(SearchActivity.SEARCHABLE_LIST, viewModel.channelsLiveData.value?.channels?.toTypedArray())
//            }
//            startActivity(intent)
//        }
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {

            if (item is Channel) {
                val intent = Intent(requireActivity(), WebviewActivity::class.java).apply {
                    putExtra(WebViewDisplayFragment.URL_KEY, item.url)
                }
                startActivity(intent)
            }

            if (item is Movie) {
                val intent = Intent(requireActivity(), DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, item)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    (itemViewHolder.view as ImageCardView).mainImageView,
                    DetailsActivity.SHARED_ELEMENT_NAME
                )
                    .toBundle()
                startActivity(intent, bundle)
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            if (item is Channel) {
                mBackgroundUri = item.logo
                startBackgroundTimer()
            }
        }
    }

    private fun updateBackground(uri: String?) {
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide.with(requireActivity())
            .load(uri)
            .centerCrop()
            .error(mDefaultBackground)
            .into<SimpleTarget<Drawable>>(
                object : SimpleTarget<Drawable>(width, height) {
                    override fun onResourceReady(
                        drawable: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        mBackgroundManager.drawable = drawable
                    }
                })
        mBackgroundTimer?.cancel()
    }

    private fun startBackgroundTimer() {
        mBackgroundTimer?.cancel()
        mBackgroundTimer = Timer()
        mBackgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class UpdateBackgroundTask : TimerTask() {

        override fun run() {
            mHandler.post { updateBackground(mBackgroundUri) }
        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.default_background))
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }

    private fun workaroundFocus() {
        if (view != null) {
            val viewToFocus = parentFragment?.view?.findViewById<Button>(R.id.categories_live)
            val browseFrameLayout =
                requireView().findViewById<BrowseFrameLayout>(androidx.leanback.R.id.browse_frame)
            browseFrameLayout.onFocusSearchListener =
                OnFocusSearchListener { _: View?, direction: Int ->
                    return@OnFocusSearchListener if (direction == View.FOCUS_UP) {
                        viewToFocus
                    } else {
                        null
                    }
                }
        }
    }

    companion object {
        val TAG: String = CategoriesFragment::class.java.name

        private const val BACKGROUND_UPDATE_DELAY = 300
        private const val GRID_ITEM_WIDTH = 200
        private const val GRID_ITEM_HEIGHT = 200
    }
}
