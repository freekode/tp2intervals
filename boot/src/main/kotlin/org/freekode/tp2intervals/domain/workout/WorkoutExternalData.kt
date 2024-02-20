package org.freekode.tp2intervals.domain.workout

data class WorkoutExternalData(
    val tpId: String?,
    val intervalsId: String?,
    val trainerRoadId: String?,
    val externalContent: String?
) {
    companion object {
        fun trainingPeaks(tpId: String, externalContent: String?) =
            WorkoutExternalData(tpId, null, null, externalContent)

        fun intervals(intervalsId: String) = WorkoutExternalData(null, intervalsId, null, null)

        fun trainerRoad(trainerRoadId: String) = WorkoutExternalData(null, null, trainerRoadId, null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkoutExternalData

        if (tpId != other.tpId) return false
        if (intervalsId != other.intervalsId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tpId?.hashCode() ?: 0
        result = 31 * result + (intervalsId?.hashCode() ?: 0)
        return result
    }


}
