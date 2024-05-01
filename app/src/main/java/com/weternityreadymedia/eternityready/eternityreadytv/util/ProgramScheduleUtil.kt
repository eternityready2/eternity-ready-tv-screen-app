package com.weternityreadymedia.eternityready.eternityreadytv.util

import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleProgram
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.concurrent.TimeUnit

private fun createSchedule(
    id: String,
    scheduleName: String,
    startTime: ZonedDateTime,
    endTime: ZonedDateTime,
    description: String? = "",
    imageUrl: String? = "",
    url: String? = "",
): ProgramGuideSchedule<SimpleProgram> {
    val metadata = DateTimeFormatter.ofPattern("'Starts at' HH:mm").format(startTime)
    return ProgramGuideSchedule.createScheduleWithProgram(
        id.toLong(),
        startTime.toInstant(),
        endTime.toInstant(),
        true,
        scheduleName,
        SimpleProgram(
            id = id,
            description = description,
            metadata = metadata,
            imageUrl = imageUrl,
            url = url
        )
    )
}

fun processScheduleData(
    id: String,
    scheduleName: String,
    localDate: LocalDate,
    zoneId: ZoneId,
    startTime: String,
    showLength: Long,
    description: String? = "",
    imageUrl: String? = "",
    url: String? = "",
): ProgramGuideSchedule<SimpleProgram> {
    val maxShowEndTime = localDate.plusDays(1).atStartOfDay(zoneId)

    val showStartTime = LocalDateTime.of(localDate, LocalTime.parse(startTime)).atZone(zoneId)
    val tempShowEndTime = showStartTime.plusSeconds(TimeUnit.MINUTES.toSeconds(showLength))
    val showEndTime = if (tempShowEndTime.isBefore(maxShowEndTime)) tempShowEndTime else maxShowEndTime

    return createSchedule(
        id = id,
        scheduleName = scheduleName,
        startTime = showStartTime,
        endTime = showEndTime,
        description = description,
        imageUrl = imageUrl,
        url = url,
    )
}

fun channelListToMap(channels: List<Channel>?): Map<String?, Channel?> = run {
    val channelMap = mutableMapOf<String?, Channel?>()
    channels?.forEach {
        channelMap[it.number] = it
    }
    channelMap
}