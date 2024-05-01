package com.weternityreadymedia.eternityready.eternityreadytv.views.webview

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.weternityreadymedia.eternityready.eternityreadytv.R

class WebviewActivity: FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val url = intent.getStringExtra(WebViewDisplayFragment.URL_KEY)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    WebViewDisplayFragment.newInstance(url.toString()),
                    WebViewDisplayFragment.TAG
                )
                .commitNow()
        }
    }
}