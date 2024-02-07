package org.freekode.tp2intervals.rest.configuration

import org.freekode.tp2intervals.domain.TrainingType

class TrainingTypeDTO(
    val title: String,
    val value: String
) {
    constructor(trainingType: TrainingType) : this(trainingType.title, trainingType.name)
}
