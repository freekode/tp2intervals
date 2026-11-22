package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

data class TrainingPeaksPreviewRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType> = TrainingType.DEFAULT_LIST,
)

data class TrainingPeaksSyncPreview(
    val sourceWorkoutId: String?,
    val sourceDate: LocalDate?,
    val status: Status,
    val reason: Reason,
    val candidates: List<Candidate> = emptyList(),
    val canResolveUnmatchedHold: Boolean = false,
    val missingLinkRepair: MissingLinkRepair? = null,
    val repairBlockedReason: String? = null,
) {
    data class MissingLinkRepair(val oldEventId: Long, val replacement: Candidate?)

    data class Candidate(val eventId: Long, val date: LocalDate?)

    enum class Status {
        VERIFIED_LINK,
        SINGLE_LEGACY_CANDIDATE,
        MULTIPLE_LEGACY_CANDIDATES,
        NO_MATCH_OBSERVED,
        UNRESOLVED_CLAIM,
        BROKEN_OR_UNVERIFIED_LINK,
    }

    enum class Reason {
        STORED_LINK_VERIFIED,
        LEGACY_ACCOUNT_UNVERIFIED,
        CANDIDATE_VERIFIED_BY_ID,
        MULTIPLE_MATCHES,
        LISTINGS_INCONCLUSIVE,
        CLAIM_PENDING,
        CLAIM_CANDIDATE_VERIFIED,
        LINK_MISSING,
        LINK_MARKER_CHANGED,
        LEGACY_REVIEW_REQUIRED,
        SOURCE_ID_MISSING,
        PREVIOUS_REVIEW_HOLD,
        LEGACY_DESTINATION_KEY_UNVERIFIED,
    }
}

class InvalidTrainingPeaksPreview(val reason: String) : RuntimeException(reason)

data class TrainingPeaksPreviewInventory(
    val revision: String,
    val sourceAccountId: String,
    val destinationAthleteId: String,
    val workouts: List<TrainingPeaksSyncPreview>,
    val approvedAt: String?,
    val previewOnly: Boolean,
    val activationEnabled: Boolean,
)

data class TrainingPeaksBaselineApprovalRequest(
    val selection: TrainingPeaksPreviewRequest,
    val revision: String,
    val acknowledgeInconclusive: Boolean,
    val legacyLinks: Map<String, Long> = emptyMap(),
)

data class TrainingPeaksBaselineApproval(val revision: String, val approvedAt: String, val heldWorkouts: Int)

class StaleTrainingPeaksInventory : RuntimeException("STALE_INVENTORY_REVISION")

data class TrainingPeaksHoldResolutionRequest(
    val selection: TrainingPeaksPreviewRequest,
    val revision: String,
    val sourceWorkoutId: String,
    val acknowledgeManualVerification: Boolean,
)

data class TrainingPeaksHoldResolution(val eventId: Long)

data class TrainingPeaksLinkRepairRequest(
    val selection: TrainingPeaksPreviewRequest,
    val revision: String,
    val sourceWorkoutId: String,
    val oldEventId: Long,
    val replacementEventId: Long?,
    val acknowledgeRepair: Boolean,
    val acknowledgeInconclusive: Boolean,
)

data class TrainingPeaksLinkRepair(val eventId: Long, val recreated: Boolean)
