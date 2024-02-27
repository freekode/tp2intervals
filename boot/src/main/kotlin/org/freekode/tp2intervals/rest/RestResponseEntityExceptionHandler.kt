package org.freekode.tp2intervals.rest

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
