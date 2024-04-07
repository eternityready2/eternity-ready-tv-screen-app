package com.weternityreadymedia.eternityready.eternityreadytv.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@kotlinx.parcelize.Parcelize
data class Channel(
    @SerializedName("#") val number: String?,
    @SerializedName("channel name") val name: String?,
    @SerializedName("image or logo") val logo: String?,
    @SerializedName("video embeds for iframe") val url: String?,
    @SerializedName("Notes") val notes: String?,
    @SerializedName("Categories") val category: String?
): Parcelable
