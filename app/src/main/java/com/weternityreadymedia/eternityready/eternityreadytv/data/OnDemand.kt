package com.weternityreadymedia.eternityready.eternityreadytv.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@kotlinx.parcelize.Parcelize
data class OnDemand(
    val title: String?,
    @SerializedName("image") val logo: String?,
    @SerializedName("iframe") val url: String?,
    val description: String?,
    val category: String?
): Parcelable {
    override fun toString(): String {
        return "name: $title, image: $logo, url: $url, category: $category, description: $description"
    }
}