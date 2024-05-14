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
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.egeniq.androidtvprogramguide.ProgramGuideFragment
import com.egeniq.androidtvprogramguide.R
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.weternityreadymedia.eternityready.eternityreadytv.BuildConfig
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleProgram
import com.weternityreadymedia.eternityready.eternityreadytv.viewmodel.PresenterViewModel
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebViewDisplayFragment
import com.weternityreadymedia.eternityready.eternityreadytv.views.webview.WebviewActivity
import com.weternityreadymedia.eternityready.eternityreadytv.R as eternityR
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.Locale

class TvSchedulingFragment : ProgramGuideFragment<SimpleProgram>() {
    companion object {
        val TAG: String = TvSchedulingFragment::class.java.name
    }

    private lateinit var viewModel: PresenterViewModel

    override val USE_MILITARY_TIME: Boolean
        get() = false

    override val DISPLAY_TIMEZONE: ZoneId
        get() = ZoneId.systemDefault()

    override val DISPLAY_LOCALE: Locale
        get() = Locale.getDefault()

    override fun isTopMenuVisible(): Boolean = false

    override fun requestRefresh() {
        // You can refresh other data here as well.
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

    override fun onScheduleSelected(programGuideSchedule: ProgramGuideSchedule<SimpleProgram>?) {
        val innerSchedule = programGuideSchedule?.program

        val titleView = view?.findViewById<TextView>(R.id.programguide_detail_title)
        titleView?.text = programGuideSchedule?.displayTitle

        val metadataView = view?.findViewById<TextView>(R.id.programguide_detail_metadata)
        metadataView?.text = programGuideSchedule?.program?.metadata

        val descriptionView = view?.findViewById<TextView>(R.id.programguide_detail_description)
        descriptionView?.text = programGuideSchedule?.program?.description

        val imageView = view?.findViewById<ImageView>(R.id.programguide_detail_image) ?: return

        if (innerSchedule != null) {
            Glide.with(imageView)
                .load(innerSchedule.imageUrl)
                .centerCrop()
                .error(R.drawable.programguide_icon_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        } else {
            Glide.with(imageView).clear(imageView)
        }
    }

    override fun onScheduleClicked(programGuideSchedule: ProgramGuideSchedule<SimpleProgram>) {
        val innerSchedule = programGuideSchedule.program
        if (innerSchedule == null && BuildConfig.DEBUG) {
            // If this happens, then our data source gives partial info
            Log.e(TAG, "Unable to open schedule!")
            return
        }

        val intent = Intent(requireActivity(), WebviewActivity::class.java).apply {
            putExtra(WebViewDisplayFragment.URL_KEY, innerSchedule?.url)
        }
        startActivity(intent)

        /*if (programGuideSchedule.isCurrentProgram) {

        } else {
            Toast.makeText(context, "Open detail page", Toast.LENGTH_LONG).show()
        }*/

        // Example of how a program can be updated. You could also change the underlying program.
        // updateProgram(programGuideSchedule.copy(displayTitle = programGuideSchedule.displayTitle + " [clicked]"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showProgramGuideDayFilter = false
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