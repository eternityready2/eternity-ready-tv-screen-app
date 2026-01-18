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
    
    private data class CacheEntry(
        val data: List<OnDemand>,
        val timestamp: Long
    )
    
    private val onDemandCache = mutableMapOf<String, CacheEntry>()
    private val CACHE_DURATION = 60 * 60 * 1000L
    
    val apiLoader: TvApi = Retrofit
        .Builder()
        .baseUrl(TvApi.URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(TvApi::class.java)

    private fun isCacheValid(entry: CacheEntry?): Boolean {
        return entry != null && 
               (System.currentTimeMillis() - entry.timestamp) <= CACHE_DURATION
    }

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
        // Check cache first - handles both empty and expired cache
        val cachedEntry = onDemandCache[contentSelected]
        if (isCacheValid(cachedEntry)) {
            return cachedEntry!!.data
        }

        // Cache miss or expired - fetch fresh data
        val movieResponse = apiLoader.fetchMovies()
        val radioResponse = apiLoader.fetchRadio()
        val musicResponse = apiLoader.fetchMusic()
        val stations = apiLoader.fetchStations()

        val musicContent = musicResponse.music.flatMap { musicItem ->
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
        }.sortedBy { it.category?.lowercase() }

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
                    url = "https://eternityready.com/radio/stream-player.html?stream=https://proxy.eternityready.com/?url=${URLEncoder.encode(radioItem.src, StandardCharsets.UTF_8.toString())}&station=${URLEncoder.encode(radioItem.name, StandardCharsets.UTF_8.toString())}",
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
                url = "https://eternityready.com/radio/stream-player.html?stream=https://proxy.eternityready.com/?url=${URLEncoder.encode(station.url, StandardCharsets.UTF_8.toString())}&station=${URLEncoder.encode(station.name, StandardCharsets.UTF_8.toString())}",
                description = station.description,
                category = "Featured"
            )
        }
        
        val radioAndStation = stationsFeatured + radioContent
        val allContent = stationsFeatured + (radioContent + moviesContent + musicContent).sortedBy { it.category?.lowercase() }

        // Cache fresh data with timestamp
        val currentTime = System.currentTimeMillis()
        onDemandCache["radio"] = CacheEntry(radioAndStation, currentTime)
        onDemandCache["movies"] = CacheEntry(moviesContent, currentTime)
        onDemandCache["music"] = CacheEntry(musicContent, currentTime)
        onDemandCache["all"] = CacheEntry(allContent, currentTime)

        // Return requested content or fallback to all
        return onDemandCache[contentSelected]?.data ?: allContent
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

    // Manual cache clearing methods
    fun clearOnDemandCache() {
        onDemandCache.clear()
    }

    fun clearOnDemandCacheEntry(key: String) {
        onDemandCache.remove(key)
    }
}
