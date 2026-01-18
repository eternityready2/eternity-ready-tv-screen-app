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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Repository {

    private val gson = GsonBuilder().create()
    private val onDemandCache = HashMap<String, List<OnDemand>>()
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

    suspend fun getOnDemand(contentSelected: String): List<OnDemand> {
        if (onDemandCache.isNotEmpty()) {
            return onDemandCache[contentSelected] ?: onDemandCache["all"].orEmpty()
        }

        val movieResponse = apiLoader.fetchMovies()
        val radioResponse = apiLoader.fetchRadio()
        val stations = apiLoader.fetchStations()

        val moviesContent = movieResponse.movies.flatMap { movie ->
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
        }.sortedBy { it.category?.lowercase() }

        val radioContent = radioResponse.channels.flatMap { radioItem ->
            val categories = radioItem.categories ?: listOf("Uncategorized")
            categories.map { category ->
                OnDemand(
                    title = radioItem.name,
                    logo = when {
                        radioItem.logo == null -> null
                        radioItem.logo.trim().startsWith("http://") ->
                            radioItem.logo.trim().replace("http://".toRegex(), "https://")
                        radioItem.logo.trim().startsWith("/") || !radioItem.logo.trim().contains("://") ->
                            "https://eternityready.com/radio/${radioItem.logo.trim()}"
                        else -> radioItem.logo.trim()
                    },
                    url = radioItem.src,
                    description = radioItem.description,
                    category = category
                )
            }
        }.sortedBy { it.category?.lowercase() }

        val stationsFeatured = stations.map { station ->
            OnDemand(
                title = station.name,
                logo = when {
                    station.logo == null -> null
                    station.logo.trim().startsWith("http://") ->
                        station.logo.trim().replace("http://".toRegex(), "https://")
                    station.logo.trim().startsWith("/") || !station.logo.trim().contains("://") ->
                        "https://listen.eternityready.com/${station.logo.trim()}"
                    else -> station.logo.trim()
                },
                url = "https://proxy.eternityready.com/?url=${URLEncoder.encode(station.url, StandardCharsets.UTF_8.toString())}",
                description = station.description,
                category = "Featured"
            )
        }
        val radioAndStation = stationsFeatured + radioContent
        val allContent = stationsFeatured + (radioContent + moviesContent).sortedBy { it.category?.lowercase() }

        onDemandCache["radio"] = radioAndStation
        onDemandCache["movies"] = moviesContent
        onDemandCache["all"] = allContent

        return onDemandCache[contentSelected] ?: allContent
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
