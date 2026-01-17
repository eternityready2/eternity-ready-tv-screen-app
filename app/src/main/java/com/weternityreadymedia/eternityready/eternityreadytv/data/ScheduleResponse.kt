package com.weternityreadymedia.eternityready.eternityreadytv.data

data class ScheduleChannel(
    val channel_name: String?,
    val shows: List<ScheduleShow>
)

data class ScheduleShow(
    val show_name: String?,
    val day: String?,
    val start_time: String?,
    val end_time: String?
)
