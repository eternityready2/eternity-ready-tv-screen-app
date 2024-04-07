package com.weternityreadymedia.eternityready.eternityreadytv.views.search

import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchUtil() {
    constructor(channels: Array<Channel>) : this() {
        this.channels = channels.toList()
    }

    private var channels: List<Channel> = listOf()

    suspend fun filterMatchingList(query: String?) = withContext(Dispatchers.IO) {
        channels.filter { value ->
            !query.isNullOrBlank() && (value.name.toString().contains("$query", ignoreCase = true) ||
                    value.number.toString().contains("$query", ignoreCase = true))
        }.sortedBy { it.name }
    }
}