package com.weternityreadymedia.eternityready.eternityreadytv.data

import com.google.gson.annotations.SerializedName

data class RadioResponse(
    val Title: String?,
    @SerializedName("Number of channels") val numberOfChannels: Int?,
    val channels: List<RadioItem>
)


data class RadioItem(
    val id: String?,
    val name: String?,
    val description: String?,
    val logo: String?,
    val src: String?,
    val categories: List<String>?,
    val rating: Double?,
    val tags: List<String>?
)
