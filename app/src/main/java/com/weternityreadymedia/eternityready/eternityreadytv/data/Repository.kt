package com.weternityreadymedia.eternityready.eternityreadytv.data

import android.content.Context
import android.content.res.Resources
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.google.gson.GsonBuilder
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.api.TvApi
import com.weternityreadymedia.eternityready.eternityreadytv.streamer.readDataFromJson
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.InputStream
import android.util.Log

object Repository {

    private val gson = GsonBuilder().create()

    val apiLoader: TvApi = Retrofit
        .Builder()
        .baseUrl(TvApi.URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(TvApi::class.java)

    suspend fun getChannels(): List<Channel> {
        return try {
            val response = apiLoader.fetchData()
            response.channels.mapIndexed { index, item ->
                Channel(
                    number = (index + 1).toString(),
                    name = item.name,
                    logo = item.logo,
                    url = item.embed,
                    notes = item.description,
                    category = item.categories?.joinToString(", ")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOnDemand(): List<OnDemand> {
        return try {
            val movieResponse = apiLoader.fetchMovies()
            val musicResponse = apiLoader.fetchMusic()

            val movies = movieResponse.movies.flatMap { movie ->
                val categories = movie.categories ?: listOf("Uncategorized")
                categories.map { category ->
                    OnDemand(
                        title = movie.title,
                        logo = movie.thumbnail,
                        url = movie.embed,
                        description = movie.description,
                        category = category
                    )
                }
            }

            val music = musicResponse.music.flatMap { musicItem ->
                val categories = musicItem.categories ?: listOf("Music")
                categories.map { category ->
                    OnDemand(
                        title = musicItem.title,
                        logo = musicItem.thumbnail?.replace("http://", "https://"),
                        url = musicItem.embed,
                        description = musicItem.description,
                        category = category
                    )
                }
            }

            movies + music
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSchedule(
        channelsMap: Map<String?, Channel?>,
        localDate: LocalDate,
        zoneId: ZoneId
    ): Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>> {
        return try {
            val schedules = apiLoader.fetchSchedule()
            readDataFromJson(schedules, channelsMap, localDate, zoneId)
        } catch (e: Exception) {
            Log.e("DEBUG - Repository", "Error loading TV schedule", e)
            Pair(listOf(), mapOf())
        }
    }

    suspend fun openAndReadRawFile(
        localDate: LocalDate,
        zoneId: ZoneId,
        channelsMap: Map<String?, Channel?>,
    ): Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>> {
        return try {
            Repository.getSchedule(channelsMap, localDate, zoneId)
        } catch (e: Exception) {
            Pair(listOf(), mapOf())
        }
    }
}
