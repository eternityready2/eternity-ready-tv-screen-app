package com.weternityreadymedia.eternityready.eternityreadytv.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.weternityreadymedia.eternityready.eternityreadytv.BuildConfig
import com.weternityreadymedia.eternityready.eternityreadytv.api.ChannelsList
import com.weternityreadymedia.eternityready.eternityreadytv.data.Repository
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleChannel
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleProgram
import com.weternityreadymedia.eternityready.eternityreadytv.util.channelListToMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import retrofit2.HttpException
import java.io.IOException

class PresenterViewModel: ViewModel() {

    private val _liveData: MutableLiveData<ChannelsList> = MutableLiveData()

    val liveData: LiveData<ChannelsList> = _liveData
    var loadingState: LoadingState = LoadingState.IDLE
        private set

    private var readScheduleData: Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>>? = null

    suspend fun getData() {
        val data = withContext(Dispatchers.IO) {
            try {
                val remoteData = Repository.apiLoader.fetchData()
                ChannelsList(remoteData)
            } catch (exception: HttpException) {
                if (BuildConfig.DEBUG) Log.e("exception", exception.message.toString())
                loadingState = LoadingState.ERROR
                ChannelsList(listOf())
            } catch (exception: IOException) {
                if (BuildConfig.DEBUG) Log.e("exception", exception.message.toString())
                loadingState = LoadingState.ERROR
                ChannelsList(listOf())
            }
        }

        if (data.channels.isNotEmpty()) {
            loadingState = LoadingState.LOADED
        }

        _liveData.value = data
    }

    suspend fun getSchedulingData(
        context: Context,
        localDate: LocalDate,
        zoneId: ZoneId
    ): Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>> = run {
        if (readScheduleData == null || readScheduleData!!.first.isEmpty()) {
            val dataValue = Repository.openAndReadRawFile(
                context, localDate, zoneId, channelListToMap(liveData.value?.channels)
            )
            readScheduleData = dataValue
            dataValue
        } else {
            readScheduleData!!
        }
    }

    enum class LoadingState {
        IDLE,
        ERROR,
        LOADED
    }
}