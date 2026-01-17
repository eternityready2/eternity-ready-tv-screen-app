package com.weternityreadymedia.eternityready.eternityreadytv.data

import com.google.gson.annotations.SerializedName

data class ChannelResponse(
    val Title: String?,
    @SerializedName("Number of channels") val numberOfChannels: Int?,
    val channels: List<ChannelItem>
)

data class ChannelItem(
    val name: String?,
    val logo: String?,
    val link: String?,
    val embed: String?,
    val keywords: List<String>?,
    val rating: Int?,
    val categories: List<String>?,
    val tags: List<String>?,
    val status: String?,
    val description: String?,
    val id: String?
)
