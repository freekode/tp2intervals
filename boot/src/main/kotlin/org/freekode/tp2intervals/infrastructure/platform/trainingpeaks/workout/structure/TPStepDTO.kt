package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

class TPStepDTO(
    var name: String?,
    var length: TPLengthDTO,
    var targets: List<TPTargetDTO>,
    var intensityClass: String?,
    var openDuration: Boolean?
)
