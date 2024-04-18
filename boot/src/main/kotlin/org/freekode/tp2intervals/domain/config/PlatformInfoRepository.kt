package org.freekode.tp2intervals.domain.config

import org.freekode.tp2intervals.domain.Platform

interface PlatformInfoRepository {
    fun platform(): Platform

    fun platformInfo(): PlatformInfo
}
