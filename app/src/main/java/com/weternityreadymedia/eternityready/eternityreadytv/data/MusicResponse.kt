package com.weternityreadymedia.eternityready.eternityreadytv.data

import com.google.gson.annotations.SerializedName

data class MusicResponse(
    val music: List<MusicItem>
)

data class MusicItem(
    val id: String?,
    val title: String?,
    val description: String?,
    val thumbnail: String?,
    val embed: String?,
    val categories: List<String>?,
    val tags: List<String>?,
    val rating: Int?
)
