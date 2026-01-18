package com.weternityreadymedia.eternityready.eternityreadytv.data

import com.google.gson.annotations.SerializedName

data class StationItem(
    val id: Int?,
    val orderIndex: Int?,
    val name: String?,
    val refUrl: String?,
    val logo: String?,
    val thumbnail: String?,
    val backgroundImage: String?,
    val url: String?,
    val donateLink: String?,
    val gtm: String?,
    val analytics: String?,
    val advertisements: String?,
    val isDefault: Int?,
    val isActive: Int?,
    val metaPreset: String?,
    val version: String?,
    val email: String?,
    val homepage: String?,
    val location: String?,
    val address: String?,
    val telephone: String?,
    val callLetters: String?,
    val description: String?
)
