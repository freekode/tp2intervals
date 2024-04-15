package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user

import java.io.Serializable

class TrainingPeaksUser(
    var userId: String,
    val isAthlete: Boolean,
) : Serializable
