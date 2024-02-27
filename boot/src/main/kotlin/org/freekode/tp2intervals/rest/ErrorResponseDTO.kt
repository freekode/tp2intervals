package org.freekode.tp2intervals.rest

data class ErrorResponseDTO(
    val platform: String?,
    val message: String,
) {
    constructor(message: String) : this(null, message)
}
