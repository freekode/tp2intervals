package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.WorkoutExternalData

class ExternalDataConverter {
    private val externalDataDescriptionSeparator = "//////////"

    fun toSimpleString(externalData: WorkoutExternalData): String {
        val externalDataString = externalData.toSimpleString()
        return """
                $externalDataDescriptionSeparator
                $externalDataString
            """.trimIndent()
    }

    fun toExternalData(string: String): WorkoutExternalData? {
        val split = string.split(externalDataDescriptionSeparator)
        if (split.size != 2) {
            return null
        }
        val externalDataSplit = split[1].trim()
        return WorkoutExternalData.fromSimpleString(externalDataSplit)
    }
}
