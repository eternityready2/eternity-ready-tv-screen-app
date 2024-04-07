package com.weternityreadymedia.eternityready.eternityreadytv.views.search

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.fragment.app.FragmentActivity
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel

class SearchActivity: FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayExtra(SEARCHABLE_LIST, Channel::class.java)
        } else {
            intent?.getParcelableArrayExtra(SEARCHABLE_LIST)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.search_frag_container, SearchFragment.newInstance(data), SearchFragment.TAG)
                commit()
            }
        }
    }

    override fun onBackPressed() {
        Log.e("back", "on back pressed called")
        val fragment = supportFragmentManager.findFragmentByTag(SearchFragment.TAG) as? SearchFragment
        Log.e("back", "${fragment?.isKeyBoardVisible()}")
        if (fragment?.isKeyBoardVisible() != true) super.onBackPressed()
    }

    companion object {
        const val SEARCHABLE_LIST: String = "SEARCHABLE_LIST"
    }
}