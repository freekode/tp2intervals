package org.freekode.tp2intervals.rest

import feign.FeignException
import org.slf4j.LoggerFactory
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(FeignException::class)
    fun providerException(exception: FeignException): ResponseEntity<ErrorResponseDTO> {
        val upstreamStatus = exception.status().takeIf { it > 0 }
        log.warn("Provider HTTP request failed: code=PROVIDER_REQUEST_FAILED status={}", upstreamStatus)
        val message = if (upstreamStatus == 401 || upstreamStatus == 403) "Access denied by provider"
        else "Provider request failed"
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponseDTO(message))
    }

    @ExceptionHandler(PlatformException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun platformException(
        exception: PlatformException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDTO> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponseDTO(exception.platform.title, exception.message!!))
    }
}
