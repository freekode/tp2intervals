package org.freekode.tp2intervals.infrastructure

import org.freekode.tp2intervals.domain.Platform

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CatchFeignException(
    val platform: Platform
)
