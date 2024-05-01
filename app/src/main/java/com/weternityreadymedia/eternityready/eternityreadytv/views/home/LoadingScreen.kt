package com.weternityreadymedia.eternityready.eternityreadytv.views.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.viewmodel.PresenterViewModel
import com.weternityreadymedia.eternityready.eternityreadytv.viewmodel.PresenterViewModel.LoadingState
import kotlinx.coroutines.launch

class LoadingScreen: Fragment() {

    private lateinit var viewModel: PresenterViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.NewInstanceFactory()
        )[PresenterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_loading_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retryButton = view.findViewById<Button>(R.id.retry_button)
        val imageView = view.findViewById<ImageView>(R.id.image_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)

        retryButton.apply {
            setOnClickListener {
                imageView?.visibility = View.GONE
                progressBar?.visibility = View.VISIBLE
                retryButton?.visibility = View.INVISIBLE

                lifecycleScope.launch { viewModel.getData() }
            }

            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    override fun onStart() {
        super.onStart()

        val imageView = view?.findViewById<ImageView>(R.id.image_view)
        val progressBar = view?.findViewById<ProgressBar>(R.id.progress_bar)
        val retryButton = view?.findViewById<Button>(R.id.retry_button)

        viewModel.liveData.observe(this) {
            if (viewModel.loadingState == LoadingState.LOADED) {
                val parentActivity = activity
                if (parentActivity != null && parentActivity is NotifyActivityFetchFinished) {
                    parentActivity.loadingDone()
                }
            } else if (viewModel.loadingState == LoadingState.ERROR) {
                imageView?.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
                retryButton?.visibility = View.VISIBLE

                Glide.with(imageView!!).load(R.drawable.network_error).into(imageView)
                retryButton?.requestFocus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.getData()
        }
    }

    override fun onStop() {
        viewModel.liveData.removeObservers(this)
        super.onStop()
    }

    companion object {
        @JvmField
        val TAG: String = LoadingScreen::class.java.name
    }

    interface NotifyActivityFetchFinished {
        fun loadingDone()
    }
}