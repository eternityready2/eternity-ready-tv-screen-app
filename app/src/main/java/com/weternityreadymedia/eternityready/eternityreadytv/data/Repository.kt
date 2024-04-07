package com.weternityreadymedia.eternityready.eternityreadytv.data

import com.weternityreadymedia.eternityready.eternityreadytv.api.TvApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Repository {

    val apiLoader: TvApi = Retrofit
        .Builder()
        .baseUrl(TvApi.URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TvApi::class.java)
}