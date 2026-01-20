package com.weternityreadymedia.eternityready.eternityreadytv.streamer

import android.text.SpannedString
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel
import com.weternityreadymedia.eternityready.eternityreadytv.data.ScheduleChannel
import com.weternityreadymedia.eternityready.eternityreadytv.data.ScheduleShow
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleChannel
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleProgram
import com.weternityreadymedia.eternityready.eternityreadytv.util.processScheduleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import android.util.Log



suspend fun readDataFromJson(
    schedules: List<ScheduleChannel>,
    channelsMap: Map<String?, Channel?>,
    localDate: LocalDate,
    zoneId: ZoneId
): Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>> =
withContext(Dispatchers.IO) {
    val channelSet = mutableSetOf<SimpleChannel>()
    val channelMap = mutableMapOf<String, MutableList<ProgramGuideSchedule<SimpleProgram>>>()
    val todayDayOfWeek = localDate.dayOfWeek.toString()

    // Add ALL channels from channelsMap first (even those without schedules)
    channelsMap.forEach { (channelNumber, channelMatch) ->
        if (!channelNumber.isNullOrEmpty()) {
            channelSet.add(
                SimpleChannel(
                    id = channelNumber,
                    name = SpannedString(channelMatch?.name ?: "Channel $channelNumber"),
                    imageUrl = channelMatch?.logo?.replace("http://", "https://"),
                    channelNumber = channelNumber,
                    description = channelMatch?.notes,
                    url = channelMatch?.url
                )
            )
        }
    }

    // Match schedules to channels using channel numbers from channelsMap
    channelsMap.forEach { (channelNumber, channelMatch) ->
        if (channelNumber.isNullOrEmpty()) return@forEach
        
        // Find matching schedule by channel name or number
        val matchingSchedule = schedules.find { schedule ->
            schedule.channel_name?.equals(channelMatch?.name, ignoreCase = true) == true ||
            schedule.channel_name?.equals(channelNumber, ignoreCase = true) == true
        }
        
        matchingSchedule?.shows?.forEach { show ->
            if (show.day?.lowercase() == todayDayOfWeek.lowercase() &&
                show.start_time != null &&
                show.end_time != null
            ) {
                Log.d(
                    "ScheduleDebug",
                    "Today=$todayDayOfWeek channel=$channelNumber name=${channelMatch?.name} show=${show.show_name} start=${show.start_time} end=${show.end_time}"
                )

                val startTimeOnly = show.start_time.substringBeforeLast(":").padEnd(5, '0')
                val durationMinutes = calculateDurationMinutes(show.start_time, show.end_time)

                val schedule = processScheduleData(
                    id = channelNumber,
                    scheduleName = show.show_name ?: "Unknown Show",
                    localDate = localDate,
                    zoneId = zoneId,
                    startTime = startTimeOnly,
                    showLength = durationMinutes,
                    description = channelMatch?.notes,
                    imageUrl = channelMatch?.logo?.replace("http://", "https://"),
                    url = channelMatch?.url
                )

                channelMap.getOrPut(channelNumber) { mutableListOf() }.add(schedule)
            }
        }
    }

    Pair(channelSet.toList(), channelMap)
}

private fun calculateDurationMinutes(startTime: String, endTime: String): Long {
    val startParts = startTime.split(":")
    val endParts = endTime.split(":")

    if (startParts.size < 2 || endParts.size < 2) return 60L

    val startHour = startParts[0].toIntOrNull() ?: 0
    val startMin = startParts[1].toIntOrNull() ?: 0
    val endHour = endParts[0].toIntOrNull() ?: 0
    val endMin = endParts[1].toIntOrNull() ?: 0

    val startTotal = startHour * 60 + startMin
    val endTotal = endHour * 60 + endMin

    return if (endTotal >= startTotal) {
        (endTotal - startTotal).toLong()
    } else {
        (endTotal + 24 * 60 - startTotal).toLong()  // overnight
    }
}
