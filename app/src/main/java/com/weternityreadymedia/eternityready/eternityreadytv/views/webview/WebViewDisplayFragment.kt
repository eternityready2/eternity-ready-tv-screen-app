package com.weternityreadymedia.eternityready.eternityreadytv.views.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.weternityreadymedia.eternityready.eternityreadytv.R

class WebViewDisplayFragment: Fragment() {

    private var url: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = arguments?.getString(URL_KEY)
        url = data.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_webview_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = view.findViewById<WebView>(R.id.display_webview)

        webView.settings.apply {
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadsImagesAutomatically = true
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.setFocusableInTouchMode(true)
        webView.isFocusable = true
        webView.clearCache(true)

        webView.setWebChromeClient(object : VideoEnabledWebChromeClient(
            webView,
            view.findViewById(R.id.fullscreen_html5_player_view),
            view.findViewById(R.id.progress_bar)
        ) {
        })

        webView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<WebView>(R.id.display_webview)?.apply {
            requestFocus()
            loadUrl(this@WebViewDisplayFragment.url)
        }
    }

    companion object {
        @JvmField
        val TAG: String = WebViewDisplayFragment::class.java.name

        const val URL_KEY: String = "URL_KEY"

        @JvmStatic
        fun newInstance(url: String): WebViewDisplayFragment {
            return WebViewDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(URL_KEY, url)
                }
            }
        }
    }
}