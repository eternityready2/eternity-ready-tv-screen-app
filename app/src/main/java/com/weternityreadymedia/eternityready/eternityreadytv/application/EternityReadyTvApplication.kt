package com.weternityreadymedia.eternityready.eternityreadytv.application

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class EternityReadyTvApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}