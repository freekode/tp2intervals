package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform

data class DeleteLibraryRequest(
    val externalData: ExternalData,
    val platform: Platform,
)
