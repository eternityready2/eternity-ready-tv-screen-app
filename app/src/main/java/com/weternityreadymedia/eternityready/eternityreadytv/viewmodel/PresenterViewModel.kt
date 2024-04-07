package com.weternityreadymedia.eternityready.eternityreadytv.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.weternityreadymedia.eternityready.eternityreadytv.api.ChannelsList
import com.weternityreadymedia.eternityready.eternityreadytv.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class PresenterViewModel: ViewModel() {

    private val _liveData: MutableLiveData<ChannelsList> = MutableLiveData()

    val liveData: LiveData<ChannelsList> = _liveData
    var loadingState: LoadingState = LoadingState.IDLE
        private set

    suspend fun getData() {
        val data = withContext(Dispatchers.IO) {
            try {
                val remoteData = Repository.apiLoader.fetchData()
                ChannelsList(remoteData)
            } catch (exception: HttpException) {
                loadingState = LoadingState.ERROR
                ChannelsList(listOf())
            } catch (exception: IOException) {
                loadingState = LoadingState.ERROR
                ChannelsList(listOf())
            }
        }

        if (data.channels.isNotEmpty()) {
            loadingState = LoadingState.LOADED
        }

        _liveData.value = data
    }

    enum class LoadingState {
        IDLE,
        ERROR,
        LOADED
    }
}