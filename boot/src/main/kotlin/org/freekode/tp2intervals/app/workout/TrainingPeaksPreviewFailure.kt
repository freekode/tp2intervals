package org.freekode.tp2intervals.app.workout

import feign.FeignException
import feign.codec.DecodeException
import org.freekode.tp2intervals.infrastructure.ProviderReadFailure
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

internal enum class PreviewStage {
    BASELINE_READ, TRAINING_PEAKS_ACCOUNT, INTERVALS_ACCOUNT, INTERVALS_CALENDAR_RANGE, INTERVALS_CALENDAR_LEGACY_WINDOW,
    TRAINING_PEAKS_CALENDAR, SOURCE_ROWS, LEDGER_MATCHING, INTERVALS_EVENT_LOOKUP,
    REVISION, APPROVAL_PERSIST, REQUEST
}

internal enum class PreviewProvider { TRAINING_PEAKS, INTERVALS, INTERNAL }

internal class TrainingPeaksPreviewFailure(
    val stage: PreviewStage,
    val provider: PreviewProvider,
    val exceptionClass: String,
    val upstreamStatus: Int?,
    val code: String,
    val status: HttpStatus,
) : RuntimeException(code) {
    companion object {
        fun from(stage: PreviewStage, provider: PreviewProvider, error: Exception): TrainingPeaksPreviewFailure {
            val upstream = (error as? FeignException)?.status()?.takeIf { it > 0 }
                ?: (error as? ProviderReadFailure)?.upstreamStatus
            val upstreamCode = when {
                error is ProviderReadFailure && error.kind == ProviderReadFailure.Kind.CONFIGURATION ->
                    "CONFIGURATION_REQUIRED"
                upstream == 401 || upstream == 403 -> "AUTH_FAILED"
                error is DecodeException || error is ProviderReadFailure && error.kind == ProviderReadFailure.Kind.RESPONSE ->
                    "INVALID_RESPONSE"
                error is FeignException || error is ProviderReadFailure -> "REQUEST_FAILED"
                else -> null
            }
            if (provider != PreviewProvider.INTERNAL && upstreamCode != null) {
                val status = if (upstreamCode == "CONFIGURATION_REQUIRED") HttpStatus.SERVICE_UNAVAILABLE
                else HttpStatus.BAD_GATEWAY
                return TrainingPeaksPreviewFailure(stage, provider, error.javaClass.simpleName, upstream, "${provider}_$upstreamCode", status)
            }
            val code = if (error is DataAccessException) "PREVIEW_STORAGE_ERROR" else "PREVIEW_INTERNAL_ERROR"
            return TrainingPeaksPreviewFailure(
                stage, PreviewProvider.INTERNAL, error.javaClass.simpleName, null, code, HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }
}

internal inline fun <T> previewStage(
    stage: PreviewStage, provider: PreviewProvider = PreviewProvider.INTERNAL, action: () -> T,
): T = try {
    action()
} catch (error: Exception) {
    when (error) {
        is InvalidTrainingPeaksPreview, is StaleTrainingPeaksInventory,
        is ResponseStatusException, is TrainingPeaksPreviewFailure -> throw error
        else -> throw TrainingPeaksPreviewFailure.from(stage, provider, error)
    }
}
