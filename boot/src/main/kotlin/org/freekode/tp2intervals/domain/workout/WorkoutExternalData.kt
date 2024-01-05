package org.freekode.tp2intervals.domain.workout

data class WorkoutExternalData(
    val tpId: String?,
    val intervalsId: String?,
    val externalContent: String?
) {
    companion object {
        fun thirdParty(tpId: String, externalContent: String?) = WorkoutExternalData(tpId, null, externalContent)

        fun intervals(intervalsId: String) = WorkoutExternalData(null, intervalsId, null)
    }
}
