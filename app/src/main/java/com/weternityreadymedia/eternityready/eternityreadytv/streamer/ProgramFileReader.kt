package com.weternityreadymedia.eternityready.eternityreadytv.streamer

import android.text.SpannedString
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.opencsv.CSVReaderBuilder
import com.weternityreadymedia.eternityready.eternityreadytv.data.Channel
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleChannel
import com.weternityreadymedia.eternityready.eternityreadytv.data.SimpleProgram
import com.weternityreadymedia.eternityready.eternityreadytv.util.processScheduleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

@Throws(IOException::class)
suspend fun readDataFromFile(
    stream: InputStream,
    localDate: LocalDate,
    zoneId: ZoneId,
    tvChannelsMap: Map<String?, Channel?>,
): Pair<List<SimpleChannel>, Map<String, List<ProgramGuideSchedule<SimpleProgram>>>> = withContext(Dispatchers.IO) {
    val csvReader = CSVReaderBuilder(InputStreamReader(stream)).withSkipLines(1).build()
    var record: Array<String>?

    val channelSet = mutableSetOf<SimpleChannel>()
    val channelMap = mutableMapOf<String, MutableList<ProgramGuideSchedule<SimpleProgram>>>()

    while (true) {
        record = csvReader.readNext() ?: break

        val mapRecord = tvChannelsMap[record[0]]

        channelSet.add(
            SimpleChannel(
                id = record[0],
                name = SpannedString(record[1]),
                imageUrl = mapRecord?.logo,
                channelNumber = record[0]
            )
        )

        val schedule = processScheduleData(
            id = record[0],
            scheduleName = record[4],
            localDate = localDate,
            zoneId = zoneId,
            startTime = record[2],
            showLength = record[3].toDouble().toLong(),
            description = mapRecord?.notes,
            imageUrl = mapRecord?.logo,
            url = mapRecord?.url,
        )

        channelMap.getOrPut(record[0]) {mutableListOf()}.add(schedule)
    }
    Pair(channelSet.toList(), channelMap)
}