package com.weternityreadymedia.eternityready.eternityreadytv.views.webview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.weternityreadymedia.eternityready.eternityreadytv.BuildConfig
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.util.findLinksInText

class WebViewDisplayFragment: Fragment() {

    private var url: String = ""
    private var shouldLoadDataInstead: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var data = arguments?.getString(URL_KEY)
        if (data == null) {
            data = arguments?.getString(DATA_URL_KEY)
            shouldLoadDataInstead = true
        }
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
            // view.findViewById(R.id.progress_bar)
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
            if (shouldLoadDataInstead) loadData(this@WebViewDisplayFragment.url, "text/html; charset=utf-8", "UTF-8")
            else loadUrl(this@WebViewDisplayFragment.url)
        }

        if (BuildConfig.DEBUG) {
            Log.e("url", this@WebViewDisplayFragment.url)
        }
    }

    fun handleKeyEvent() {
        view?.findViewById<WebView>(R.id.display_webview).let {
            it?.evaluateJavascript("""var video = document.querySelector("video"); if (video.paused){video.play();} else {video.pause();}""", null)
        }
    }

    companion object {
        @JvmField
        val TAG: String = WebViewDisplayFragment::class.java.name

        const val URL_KEY: String = "URL_KEY"
        const val DATA_URL_KEY: String = "DATA_URL_KEY"

        @JvmStatic
        fun newInstance(url: String): WebViewDisplayFragment {
            return WebViewDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(URL_KEY, url)
                }
            }
        }

        @JvmStatic
        fun newInstanceWithData(element: String): WebViewDisplayFragment {
            return WebViewDisplayFragment().apply {
                val linksList = findLinksInText(element)
                if (linksList.isNotEmpty()) {
                    arguments = Bundle().apply {
                        putString(URL_KEY, (linksList.first() + "?autoplay=1"))
                    }
                }
            }
        }

        /*@JvmStatic
        private fun dummyWebPage(element: String): String {
            return """
                <!DOCTYPE html>
                <html lang="en">
                <meta charset="UTF-8">
                <title>Page Title</title>
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <link rel="stylesheet" href="">
                <style>
                </style>
                <script src=""></script>
                <body style="background-color:black;">
                    <center>
                       $element
                    </center>
                </body>
                </html>
            """.trimIndent()
        }*/
    }
}