package com.weternityreadymedia.eternityready.eternityreadytv.api
import com.weternityreadymedia.eternityready.eternityreadytv.data.ChannelResponse
import com.weternityreadymedia.eternityready.eternityreadytv.data.MovieResponse
import com.weternityreadymedia.eternityready.eternityreadytv.data.MusicResponse
import com.weternityreadymedia.eternityready.eternityreadytv.data.RadioResponse
import com.weternityreadymedia.eternityready.eternityreadytv.data.StationItem
import com.weternityreadymedia.eternityready.eternityreadytv.data.ScheduleChannel
import retrofit2.http.GET

interface TvApi {

    companion object {
        const val URL = "https://www.eternityready.com/"
    }

    @GET("data/channels.json")
    suspend fun fetchData() : ChannelResponse

    @GET("data/movies.json")
    suspend fun fetchMovies() : MovieResponse

    @GET("data/music.json")
    suspend fun fetchMusic() : MusicResponse

    @GET("data/radio.json")
    suspend fun fetchRadio() : RadioResponse

    @GET("https://listen.eternityready.com/api/station")
    suspend fun fetchStations() : List<StationItem>

    @GET("tv/tv-schedule-data.json")
    suspend fun fetchSchedule(): List<ScheduleChannel>
}
