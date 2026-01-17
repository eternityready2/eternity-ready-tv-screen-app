package com.weternityreadymedia.eternityready.eternityreadytv.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.weternityreadymedia.eternityready.eternityreadytv.BuildConfig
import com.weternityreadymedia.eternityready.eternityreadytv.api.ChannelsList
import com.weternityreadymedia.eternityready.eternityreadytv.api.OnDemandList
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

    private val _channelsLiveData: MutableLiveData<ChannelsList> = MutableLiveData()
    private val _onDemandLiveData: MutableLiveData<OnDemandList> = MutableLiveData()

    val channelsLiveData: LiveData<ChannelsList> = _channelsLiveData
    val onDemandLiveData: LiveData<OnDemandList> = _onDemandLiveData

    private var loadingState: LoadingState = LoadingState.IDLE

    private var readScheduleData: Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>>? = null

    private var onLoadComplete: ((state: LoadingState) -> Unit)? = null

    suspend fun getData() {
        val data = withContext(Dispatchers.IO) {
            try {
                val remoteData = Repository.getChannels()
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

        _channelsLiveData.value = data
        getOnDemand()
    }

    private suspend fun getOnDemand() {
        val data = withContext(Dispatchers.IO) {
            try {
                val remoteData = Repository.apiLoader.fetchOnDemand()
                remoteData
            } catch (exception: HttpException) {
                if (BuildConfig.DEBUG) Log.e("exception", exception.message.toString())
                loadingState = LoadingState.ERROR
                OnDemandList(listOf())
            } catch (exception: IOException) {
                if (BuildConfig.DEBUG) Log.e("exception", exception.message.toString())
                loadingState = LoadingState.ERROR
                OnDemandList(listOf())
            }
        }

        if (data.channels.isNotEmpty() && loadingState == LoadingState.LOADED) {
            loadingState = LoadingState.LOADED
        }

        _onDemandLiveData.value = data

        onLoadComplete?.invoke(loadingState)
    }

    suspend fun getSchedulingData(
        context: Context,
        localDate: LocalDate,
        zoneId: ZoneId
    ): Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>> = run {
        if (readScheduleData == null || readScheduleData!!.first.isEmpty()) {
            val dataValue = Repository.openAndReadRawFile(
                context, localDate, zoneId, channelListToMap(channelsLiveData.value?.channels)
            )
            readScheduleData = dataValue
            dataValue
        } else {
            readScheduleData!!
        }
    }

    fun setOnLoadCompleteListener(onLoadComplete: (state: LoadingState) -> Unit) {
        this.onLoadComplete = onLoadComplete
    }

    fun removeOnLoadCompleteListener() {
        this.onLoadComplete = null
    }

    enum class LoadingState {
        IDLE,
        ERROR,
        LOADED
    }
}
