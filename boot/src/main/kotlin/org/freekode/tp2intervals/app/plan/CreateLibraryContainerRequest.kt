package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform

data class CreateLibraryContainerRequest(
    val name: String,
    val platform: Platform,
)