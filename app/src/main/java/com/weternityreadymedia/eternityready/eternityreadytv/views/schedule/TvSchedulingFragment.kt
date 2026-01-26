package com.weternityreadymedia.eternityready.eternityreadytv.views.schedule

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.egeniq.androidtvprogramguide.ProgramGuideFragment
import com.egeniq.androidtvprogramguide.R
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.egeniq.androidtvprogramguide.entity.ProgramGuideChannel
import com.weternityreadymedia.eternityready.eternityreadytv.BuildConfig
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleProgram
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleChannel
import com.weternityreadymedia.eternityready.eternityreadytv.viewmodel.PresenterViewModel
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebViewDisplayFragment
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebviewActivity
import com.weternityreadymedia.eternityready.eternityreadytv.R as eternityR
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.Locale
import androidx.constraintlayout.widget.ConstraintLayout
import android.graphics.Color

class TvSchedulingFragment : ProgramGuideFragment<SimpleProgram>() {
    companion object {
        val TAG: String = TvSchedulingFragment::class.java.name
    }

    private lateinit var viewModel: PresenterViewModel

    override val CAN_FOCUS_CHANNEL: Boolean
        get() = true

    override val DISPLAY_TIMEZONE: ZoneId
        get() = ZoneId.systemDefault()

    override val DISPLAY_LOCALE: Locale
        get() = Locale.getDefault()

    override fun isTopMenuVisible(): Boolean = false

    override fun requestRefresh() {
        setState(State.Loading)
        requestingProgramGuideFor(currentDate)
    }

    override fun requestingProgramGuideFor(localDate: LocalDate) {
        lifecycleScope.launch {
            val value = viewModel.getSchedulingData(requireContext(), localDate, DISPLAY_TIMEZONE)
            if (value.first.isEmpty() || value.second.isEmpty()) {
                setState(State.Error("Unable to load TV Schedule"))
            } else {
                setData(value.first, value.second, localDate)
                setState(State.Content)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.programguide_day_filter)?.visibility = View.GONE
        val constraintRoot = view.findViewById<ConstraintLayout>(R.id.programguide_constraint_root)
        constraintRoot.setBackgroundColor(Color.BLACK)

        val imageView = view?.findViewById<ImageView>(R.id.programguide_detail_image) ?: return
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        Glide.with(imageView)
            .load(eternityR.drawable.logo_s)
            .error(R.drawable.programguide_icon_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)

        // Set bottom_detail height to 115dp after layout inflates
        view.post {
            setBottomDetailHeight(view)

            val menuMargin = view.findViewById<View>(R.id.programguide_menu_visible_margin)
            menuMargin.layoutParams.height = (menuMargin.layoutParams.height / 2).toInt()
            
            val timelineMargin = view.findViewById<View>(R.id.programguide_timeline_row_negative_margin)
            timelineMargin.layoutParams.width = (timelineMargin.layoutParams.width / 2).toInt()
            
            val timeOffset = view.findViewById<View>(R.id.programguide_current_time_indicator_top_offset)
            timeOffset.layoutParams.height = (timeOffset.layoutParams.height / 2).toInt()

            val timeRow = view.findViewById<View>(R.id.programguide_time_row)
            val timeRowParams = timeRow.layoutParams as ViewGroup.MarginLayoutParams
            timeRowParams.topMargin = 0
            timeRowParams.topMargin = (25 * resources.displayMetrics.density).toInt()
            timeRow.alpha = 1f
            timeRow.isHorizontalFadingEdgeEnabled = false
            timeRow.layoutParams = timeRowParams
            
            // Force layout refresh
            view.requestLayout()
        }
    }

    private fun setBottomDetailHeight(view: View) {
        val bottomDetail = view.findViewById<View>(R.id.bottom_detail)
        bottomDetail?.let { detail ->
            val heightPx = (115 * resources.displayMetrics.density).toInt()
            val params = detail.layoutParams
            params.height = heightPx
            detail.layoutParams = params
            detail.requestLayout()
            Log.d(TAG, "Set bottom_detail height to 115dp")
        }
    }

    override fun onScheduleSelected(programGuideSchedule: ProgramGuideSchedule<SimpleProgram>?) {
        val innerSchedule = programGuideSchedule?.program

        val titleView = view?.findViewById<TextView>(R.id.programguide_detail_title)
        titleView?.text = programGuideSchedule?.displayTitle

        val metadataView = view?.findViewById<TextView>(R.id.programguide_detail_metadata)
        metadataView?.text = programGuideSchedule?.program?.metadata

        val descriptionView = view?.findViewById<TextView>(R.id.programguide_detail_description)
        descriptionView?.text = programGuideSchedule?.program?.description

        val imageView = view?.findViewById<ImageView>(R.id.programguide_detail_image) ?: return

        // Ensure bottom_detail height stays 115dp on every selection
        view?.let { setBottomDetailHeight(it) }

        imageView.scaleType = ImageView.ScaleType.FIT_XY

        if (innerSchedule != null) {
            Glide.with(imageView)
                .load(innerSchedule.imageUrl)
                .error(R.drawable.programguide_icon_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        } else {
            Glide.with(imageView)
                .load(eternityR.drawable.logo_s)
                .error(R.drawable.programguide_icon_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }

    override fun onScheduleClicked(programGuideSchedule: ProgramGuideSchedule<SimpleProgram>) {
        val innerSchedule = programGuideSchedule.program
        if (innerSchedule == null && BuildConfig.DEBUG) {
            Log.e(TAG, "Unable to open schedule!")
            return
        }

        val intent = Intent(requireActivity(), WebviewActivity::class.java).apply {
            putExtra(WebViewDisplayFragment.URL_KEY, innerSchedule?.url)
        }
        startActivity(intent)
    }


    override fun onChannelSelected(channel: ProgramGuideChannel) {
        if (channel is SimpleChannel) {
            val titleView = view?.findViewById<TextView>(R.id.programguide_detail_title)
            titleView?.text = channel.name

            val descriptionView = view?.findViewById<TextView>(R.id.programguide_detail_description)
            descriptionView?.text = channel.description

            val imageView = view?.findViewById<ImageView>(R.id.programguide_detail_image) ?: return
            Glide.with(imageView)
                .load(channel.imageUrl)
                .error(R.drawable.programguide_icon_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }

    override fun onChannelClicked(channel: ProgramGuideChannel) {
        if (channel is SimpleChannel) {
            val intent = Intent(requireActivity(), WebviewActivity::class.java).apply {
                putExtra(WebViewDisplayFragment.URL_KEY, channel.url)
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory())[PresenterViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val themeWrapper = ContextThemeWrapper(activity, eternityR.style.ScheduleTheme)
        val localInflater = inflater.cloneInContext(themeWrapper)
        return super.onCreateView(localInflater, container, savedInstanceState)
    }
}
