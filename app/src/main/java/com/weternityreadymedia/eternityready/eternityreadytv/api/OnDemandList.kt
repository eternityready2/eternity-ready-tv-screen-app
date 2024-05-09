package com.weternityreadymedia.eternityready.eternityreadytv.api

import com.google.gson.annotations.SerializedName
import com.weternityreadymedia.eternityready.eternityreadytv.data.OnDemand

data class OnDemandList(
    @SerializedName("channels") val channels: List<OnDemand>
) {
    val categories: List<String> get() = channels.mapNotNull { it.category }.distinct()

    val displayChannels: Map<String, List<OnDemand>> get() = categories.associateWith { category -> channels.filter { it.category == category} }

}