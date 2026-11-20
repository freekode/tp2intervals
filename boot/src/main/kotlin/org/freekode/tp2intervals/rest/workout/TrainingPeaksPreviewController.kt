package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.InvalidTrainingPeaksPreview
import org.freekode.tp2intervals.app.workout.TrainingPeaksCalendarSync
import org.freekode.tp2intervals.app.workout.TrainingPeaksPreviewRequest
import org.freekode.tp2intervals.app.workout.TrainingPeaksPreviewInventory
import org.freekode.tp2intervals.app.workout.TrainingPeaksBaselineApproval
import org.freekode.tp2intervals.app.workout.TrainingPeaksBaselineApprovalRequest
import org.freekode.tp2intervals.app.workout.StaleTrainingPeaksInventory
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
    @PostMapping("/api/workout/trainingpeaks-sync/preview")
    fun preview(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        @RequestBody request: TrainingPeaksPreviewRequest,
    ): ResponseEntity<TrainingPeaksPreviewInventory> = respond(authorization) { sync.preview(request) }

    @PostMapping("/api/workout/trainingpeaks-sync/baseline/approve")
    fun approve(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        @RequestBody request: TrainingPeaksBaselineApprovalRequest,
    ): ResponseEntity<TrainingPeaksBaselineApproval> = respond(authorization) { sync.approve(request) }

    private fun <T> respond(authorization: String?, action: () -> T): ResponseEntity<T> {
        if (token.isBlank()) throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "PREVIEW_DISABLED")
        val bearer = authorization
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')
        if (bearer == null || !MessageDigest.isEqual(token.toByteArray(), bearer.toByteArray())) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "PREVIEW_AUTH_REQUIRED")
        }
        val results = try {
            action()
        } catch (error: InvalidTrainingPeaksPreview) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, error.reason)
        } catch (_: StaleTrainingPeaksInventory) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "STALE_INVENTORY_REVISION")
        } catch (error: ResponseStatusException) {
            throw error
        } catch (_: RuntimeException) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "BASELINE_REQUEST_FAILED")
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(results)
    }
}
