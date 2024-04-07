package com.weternityreadymedia.eternityready.eternityreadytv.api

import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel
import retrofit2.http.GET

interface TvApi {

    companion object {
        const val URL = "https://www.eternityready.com/"
    }

    @GET("a/android-app-data.json")
    suspend fun fetchData() : List<Channel>
}