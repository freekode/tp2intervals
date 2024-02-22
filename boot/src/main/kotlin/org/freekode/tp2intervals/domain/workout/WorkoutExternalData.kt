package org.freekode.tp2intervals.domain.workout

data class WorkoutExternalData(
    val trainingPeaksId: String?,
    val intervalsId: String?,
    val trainerRoadId: String?,
) {
    companion object {
        fun empty() = WorkoutExternalData(null, null, null)

        fun fromSimpleString(string: String): WorkoutExternalData {
            val fields = string.split("\n")
            var externalData = empty()
            fields.map {
                val field = it.split("=")
                externalData = when (field[0]) {
                    "trainingPeaksId" -> externalData.withTrainingPeaks(field[1])
                    "intervalsId" -> externalData.withIntervals(field[1])
                    "trainerRoadId" -> externalData.withTrainerRoad(field[1])
                    else -> throw IllegalArgumentException("Unknown filed $field for external data")
                }
            }
            return externalData
        }
    }

    fun withTrainingPeaks(trainingPeaksId: String) = WorkoutExternalData(trainingPeaksId, intervalsId, trainerRoadId)
    fun withIntervals(intervalsId: String) = WorkoutExternalData(trainingPeaksId, intervalsId, trainerRoadId)
    fun withTrainerRoad(trainerRoadId: String) = WorkoutExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun toSimpleString(): String {
        val outList = mutableListOf<String>()
        if (trainingPeaksId != null) {
            outList.add("trainingPeaksId=$trainingPeaksId")
        }
        if (intervalsId != null) {
            outList.add("intervalsId=$intervalsId")
        }
        if (trainerRoadId != null) {
            outList.add("trainerRoadId=$trainerRoadId")
        }
        return outList.joinToString(separator = "\n")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkoutExternalData

        if (trainingPeaksId != other.trainingPeaksId) return false
        if (intervalsId != other.intervalsId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = trainingPeaksId?.hashCode() ?: 0
        result = 31 * result + (intervalsId?.hashCode() ?: 0)
        return result
    }
}
