package org.freekode.tp2intervals.infrastructure

import org.freekode.tp2intervals.domain.Platform


class PlatformException(
    val platform: Platform,
    message: String
) : RuntimeException(message)
