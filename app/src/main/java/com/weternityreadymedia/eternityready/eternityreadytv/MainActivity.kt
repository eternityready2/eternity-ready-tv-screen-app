package com.weternityreadymedia.eternityready.eternityreadytv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.weternityreadymedia.eternityready.eternityreadytv.views.home.LoadingScreen
import com.weternityreadymedia.eternityready.eternityreadytv.views.home.LoadingScreen.NotifyActivityFetchFinished
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity(), NotifyActivityFetchFinished {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch(Dispatchers.IO) {
            this@MainActivity.cacheDir.deleteRecursively()
        }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoadingScreen(), LoadingScreen.TAG)
                .commitNow()
        }
    }

    override fun loadingDone() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment(), MainFragment.TAG)
            .commitNow()
    }
}