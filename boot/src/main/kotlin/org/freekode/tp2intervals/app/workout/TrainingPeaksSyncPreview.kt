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
    val sourceDate: LocalDate,
    val status: Status,
    val reason: Reason,
    val candidates: List<Candidate> = emptyList(),
) {
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
    }
}

class InvalidTrainingPeaksPreview(val reason: String) : RuntimeException(reason)
