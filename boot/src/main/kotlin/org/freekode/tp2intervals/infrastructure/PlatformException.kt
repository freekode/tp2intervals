package org.freekode.tp2intervals.infrastructure

import org.freekode.tp2intervals.domain.Platform


open class PlatformException(
    val platform: Platform,
    message: String
) : RuntimeException(message)

class ProviderReadFailure(
    platform: Platform,
    val kind: Kind,
    val upstreamStatus: Int? = null,
) : PlatformException(platform, kind.name) {
    enum class Kind { CONFIGURATION, RESPONSE, HTTP }
}
