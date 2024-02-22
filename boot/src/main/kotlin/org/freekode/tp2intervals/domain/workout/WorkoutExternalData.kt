package org.freekode.tp2intervals.domain.workout

data class WorkoutExternalData(
    val trainingPeaksId: String?,
    val intervalsId: String?,
    val trainerRoadId: String?,
) {
    private val externalDataDescriptionSeparator = "//////////"


    companion object {
        fun empty() = WorkoutExternalData(null, null, null)
    }

    fun withTrainingPeaks(trainingPeaksId: String) = WorkoutExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun withIntervals(intervalsId: String) = WorkoutExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun withTrainerRoad(trainerRoadId: String) = WorkoutExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun withSimpleString(string: String): WorkoutExternalData {
        val split = string.split(externalDataDescriptionSeparator)
        if (split.size != 2) {
            return this
        }

        val fields = split[1].trim().split("\n")
        var externalData = this
        fields.map {
            val field = it.split("=")
            if (field[0] == "trainingPeaksId" && externalData.trainingPeaksId == null) {
                externalData = externalData.withTrainingPeaks(field[1])
            } else if (field[0] == "intervalsId" && externalData.intervalsId == null) {
                externalData = externalData.withIntervals(field[1])
            } else if (field[0] == "trainerRoadId" && externalData.trainerRoadId == null) {
                externalData = externalData.withTrainerRoad(field[1])
            }
        }
        return externalData
    }

    fun toSimpleString(): String {
        val outList = mutableListOf<String>()
        if (trainingPeaksId != null) outList.add("trainingPeaksId=$trainingPeaksId")
        if (intervalsId != null) outList.add("intervalsId=$intervalsId")
        if (trainerRoadId != null) outList.add("trainerRoadId=$trainerRoadId")
        val simpleString = outList.joinToString(separator = "\n")
        return """
                $externalDataDescriptionSeparator
                $simpleString
            """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkoutExternalData

        if (trainingPeaksId != null && trainingPeaksId == other.trainingPeaksId) return true
        if (intervalsId != null && intervalsId == other.intervalsId) return true
        if (trainerRoadId != null && trainerRoadId == other.trainerRoadId) return true

        return false
    }

    override fun hashCode(): Int {
        var result = trainingPeaksId?.hashCode() ?: 0
        result = 31 * result + (intervalsId?.hashCode() ?: 0)
        result = 31 * result + (trainerRoadId?.hashCode() ?: 0)
        return result
    }


}
