package org.freekode.tp2intervals.domain

import java.io.Serializable

data class ExternalData(
    val trainingPeaksId: String?,
    val intervalsId: String?,
    val trainerRoadId: String?,
) : Serializable {
    companion object {
        const val externalDataDescriptionSeparator = "//////////"

        fun empty() = ExternalData(null, null, null)

        fun trainerRoadIdsInDescription(description: String?): Set<String> =
            description.orEmpty().split(externalDataDescriptionSeparator).drop(1)
                .flatMap { it.lines() }
                .map { it.trim() }
                .filter { it.startsWith("trainerRoadId=") }
                .map { it.substringAfter("=").trim() }
                .filter { (it.toLongOrNull() ?: 0) > 0 }
                .toSet()

        fun hasAmbiguousMetadata(description: String?): Boolean {
            val fields = description.orEmpty().substringAfter(externalDataDescriptionSeparator, "").lines()
                .map { it.trimEnd('\r').split("=", limit = 2) }
                .filter { it.size == 2 && it[0] in setOf("trainingPeaksId", "intervalsId", "trainerRoadId") }
                .groupBy({ it[0] }, { it[1] })
            return fields.values.any { values -> values.any { it.isBlank() } || values.distinct().size > 1 }
        }
    }

    fun withTrainingPeaks(trainingPeaksId: String) = ExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun withIntervals(intervalsId: String) = ExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun withTrainerRoad(trainerRoadId: String) = ExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun hasClearTrainerRoadId(description: String?): Boolean {
        val parts = description.orEmpty().split(externalDataDescriptionSeparator)
        if (parts.size != 2) return false
        val ids = parts[1].lines().map { it.trimEnd('\r') }
            .filter { it.startsWith("trainerRoadId=") }
            .map { it.substringAfter("=") }
        val id = ids.singleOrNull() ?: return false
        return (id.toLongOrNull() ?: 0) > 0 && id == trainerRoadId
    }

    fun fromSimpleString(string: String): ExternalData {
        val split = string.split(externalDataDescriptionSeparator)
        if (split.size != 2) {
            return this
        }

        val fields = split[1].trim().lines()
        var externalData = this
        fields.forEach {
            val field = it.split("=", limit = 2)
            if (field.size != 2) return@forEach
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

        other as ExternalData

        if (trainingPeaksId != other.trainingPeaksId) return false
        if (intervalsId != other.intervalsId) return false
        if (trainerRoadId != other.trainerRoadId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = trainingPeaksId?.hashCode() ?: 0
        result = 31 * result + (intervalsId?.hashCode() ?: 0)
        result = 31 * result + (trainerRoadId?.hashCode() ?: 0)
        return result
    }


}
