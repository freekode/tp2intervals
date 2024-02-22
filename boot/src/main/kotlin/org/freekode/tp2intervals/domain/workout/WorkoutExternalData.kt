package org.freekode.tp2intervals.domain.workout

data class WorkoutExternalData(
    val trainingPeaksId: String?,
    val intervalsId: String?,
    val trainerRoadId: String?,
) {
    companion object {
        fun trainingPeaks(tpId: String) = WorkoutExternalData(tpId, null, null)

        fun intervals(intervalsId: String) = WorkoutExternalData(null, intervalsId, null)

        fun trainerRoad(trainerRoadId: String) = WorkoutExternalData(null, null, trainerRoadId)
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
