package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

class IntervalsWorkoutZoneDTO(
    val id: String,
    val max: Int,
    val name: String,
    val secs: Long,
    val zone: Int,
    val color: String,
    val maxWatts: Int,
    val minWatts: Int,
    val percentRange: String,
) {
    private val percentageRangeRegex = "(\\d*)% - (\\d*)%".toRegex()

    fun getPercentagePair(): Pair<Int, Int> {
        val findResults = percentageRangeRegex.find(percentRange)
            ?: throw RuntimeException("Can't find percentage range for zone in $percentRange")
        return findResults.groups[1]!!.value.toInt() to findResults.groups[2]!!.value.toInt()
    }
}
