package com.weternityreadymedia.eternityready.eternityreadytv.api

import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel

data class ChannelsList(
    val channels: List<Channel>
) {
    private val categories: List<String> = channels.mapNotNull { it.category }.distinct()

    val displayChannels: Map<String, List<Channel>> = categories.associateWith { category -> channels.filter { it.category == category} }
}
