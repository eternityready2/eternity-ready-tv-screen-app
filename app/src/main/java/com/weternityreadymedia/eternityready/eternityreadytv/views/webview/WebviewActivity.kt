package com.weternityreadymedia.eternityready.eternityreadytv.views.webview

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import com.weternityreadymedia.eternityready.eternityreadytv.R

class WebviewActivity: FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val url = intent.getStringExtra(WebViewDisplayFragment.URL_KEY)
        val data = intent.getStringExtra(WebViewDisplayFragment.DATA_URL_KEY)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    if (url == null)
                        WebViewDisplayFragment.newInstanceWithData(data.toString())
                    else
                        WebViewDisplayFragment.newInstance(url.toString()),
                    WebViewDisplayFragment.TAG
                )
                .commitNow()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            val currentFragment = supportFragmentManager.fragments.firstOrNull()
            if (currentFragment != null) {
                (currentFragment as WebViewDisplayFragment).handleKeyEvent()
            }
        }
        return super.dispatchKeyEvent(event)
    }
}