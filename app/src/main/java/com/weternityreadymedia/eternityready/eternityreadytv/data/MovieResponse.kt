package com.weternityreadymedia.eternityready.eternityreadytv.data

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    val movies: List<MovieItem>
)

data class MovieItem(
    val id: String?,
    val title: String?,
    val description: String?,
    val thumbnail: String?,
    val embed: String?,
    val categories: List<String>?,
    val rating: Double?,
    val tags: List<String>?
)
