package com.weternityreadymedia.eternityready.eternityreadytv.data

import android.text.Spanned
import com.egeniq.androidtvprogramguide.entity.ProgramGuideChannel

data class SimpleChannel(
    override val id: String,
    override val name: Spanned?,
    override val imageUrl: String?,
    override val channelNumber: String?
) : ProgramGuideChannel {
    override fun equals(other: Any?): Boolean {
        return other is SimpleChannel && this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

data class SimpleProgram(
    val id: String?,
    val description: String?,
    val metadata: String?,
    val imageUrl: String?,
    val url: String?
)