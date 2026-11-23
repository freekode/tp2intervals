package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.InvalidTrainingPeaksPreview
import org.freekode.tp2intervals.app.workout.TrainingPeaksCalendarSync
import org.freekode.tp2intervals.app.workout.TrainingPeaksPreviewRequest
import org.freekode.tp2intervals.app.workout.TrainingPeaksPreviewInventory
import org.freekode.tp2intervals.app.workout.TrainingPeaksBaselineApproval
import org.freekode.tp2intervals.app.workout.TrainingPeaksBaselineApprovalRequest
import org.freekode.tp2intervals.app.workout.TrainingPeaksLinkRepair
import org.freekode.tp2intervals.app.workout.TrainingPeaksLinkRepairRequest
import org.freekode.tp2intervals.app.workout.TrainingPeaksHoldResolutionRequest
import org.freekode.tp2intervals.app.workout.TrainingPeaksHoldResolution
import org.freekode.tp2intervals.app.workout.StaleTrainingPeaksInventory
import org.freekode.tp2intervals.app.workout.PreviewStage
import org.freekode.tp2intervals.app.workout.TrainingPeaksPreviewFailure
import org.freekode.tp2intervals.app.workout.previewStage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest

@RestController
class TrainingPeaksPreviewController(
    private val sync: TrainingPeaksCalendarSync,
    @Value("\${app.training-peaks.preflight-token:}") private val token: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/workout/trainingpeaks-sync/preview")
    fun preview(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        @RequestBody request: TrainingPeaksPreviewRequest,
    ): ResponseEntity<TrainingPeaksPreviewInventory> = respond(authorization, "preview") { sync.preview(request) }

    @PostMapping("/api/workout/trainingpeaks-sync/baseline/approve")
    fun approve(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        @RequestBody request: TrainingPeaksBaselineApprovalRequest,
    ): ResponseEntity<TrainingPeaksBaselineApproval> = respond(authorization, "approve") { sync.approve(request) }

    @PostMapping("/api/workout/trainingpeaks-sync/holds/resolve")
    fun resolveHold(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        @RequestBody request: TrainingPeaksHoldResolutionRequest,
    ): ResponseEntity<TrainingPeaksHoldResolution> =
        respond(authorization, "resolveHold") { sync.resolveUnmatchedHold(request) }

    @PostMapping("/api/workout/trainingpeaks-sync/links/repair")
    fun repairLink(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        @RequestBody request: TrainingPeaksLinkRepairRequest,
    ): ResponseEntity<TrainingPeaksLinkRepair> =
        respond(authorization, "repairLink") { sync.repairMissingLink(request) }

    private fun <T> respond(authorization: String?, operation: String, action: () -> T): ResponseEntity<T> {
        if (token.isBlank()) throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "PREVIEW_DISABLED")
        val bearer = authorization
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')
        if (bearer == null || !MessageDigest.isEqual(token.toByteArray(), bearer.toByteArray())) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "PREVIEW_AUTH_REQUIRED")
        }
        val results = try {
            previewStage(PreviewStage.REQUEST, action = action)
        } catch (error: InvalidTrainingPeaksPreview) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, error.reason)
        } catch (_: StaleTrainingPeaksInventory) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "STALE_INVENTORY_REVISION")
        } catch (error: TrainingPeaksPreviewFailure) {
            log.warn(
                "TrainingPeaks inventory failed operation={} stage={} provider={} exception={} upstreamStatus={} code={} decoder={} fieldPath={}",
                operation, error.stage, error.provider, error.exceptionClass, error.upstreamStatus ?: "-", error.code,
                error.decoder?.type ?: "-", error.decoder?.path ?: "-",
            )
            throw ResponseStatusException(error.status, error.code)
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(results)
    }
}
