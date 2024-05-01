package com.weternityreadymedia.eternityready.eternityreadytv.data

import android.content.Context
import android.content.res.Resources
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.weternityreadymedia.eternityready.eternityreadytv.R
import com.weternityreadymedia.eternityready.eternityreadytv.api.TvApi
import com.weternityreadymedia.eternityready.eternityreadytv.streamer.readDataFromFile
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.InputStream

object Repository {

    val apiLoader: TvApi = Retrofit
        .Builder()
        .baseUrl(TvApi.URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TvApi::class.java)

    suspend fun openAndReadRawFile(
        context: Context,
        localDate: LocalDate,
        zoneId: ZoneId,
        channelsMap: Map<String?, Channel?>,
    ): Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>> {
        return try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.data)
            readDataFromFile(
                stream = inputStream,
                localDate, zoneId, channelsMap
            )
        } catch (_: IOException) {
            Pair(listOf(), mapOf())
        } catch (_: Resources.NotFoundException) {
            Pair(listOf(), mapOf())
        }

    }
}