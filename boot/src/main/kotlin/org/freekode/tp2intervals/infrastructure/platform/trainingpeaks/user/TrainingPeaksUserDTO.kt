package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user

import com.fasterxml.jackson.annotation.JsonProperty

class TrainingPeaksUserDTO(
    var userId: String?,
    val accountStatus: TPUserAccountStatusDTO,
) {
    @JsonProperty("user")
    private fun mapUserId(map: Map<String, Any>) {
        this.userId = (map["userId"] as Int).toString()
    }

    class TPUserAccountStatusDTO(
        val isAthlete: Boolean,
        val isPremium: Boolean,
    )
}
