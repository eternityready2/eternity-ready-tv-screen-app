package com.weternityreadymedia.eternityready.eternityreadytv.api
import com.weternityreadymedia.eternityready.eternityreadytv.data.ChannelResponse
import retrofit2.http.GET

interface TvApi {

    companion object {
        const val URL = "https://www.eternityready.com/"
    }

    @GET("data/channels.json")
    suspend fun fetchData(): ChannelResponse

    @GET("a/on_demand.json")
    suspend fun fetchOnDemand() : OnDemandList
}
