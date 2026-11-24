package org.freekode.tp2intervals.infrastructure

import feign.FeignException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.freekode.tp2intervals.domain.Platform
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component


@Aspect
@Component
class CatchFeignExceptionAspect {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Around("@annotation(CatchFeignException)")
    @Throws(Throwable::class)
    fun trace(joinPoint: ProceedingJoinPoint): Any? {
        val method = (joinPoint.signature as MethodSignature).method
        val annotation = method.getAnnotation(CatchFeignException::class.java)
        val platform = annotation.platform
        try {
            return joinPoint.proceed()
        } catch (e: FeignException) {
            log.warn("{} HTTP request failed with status {} during {} ({})",
                platform, e.status(), method.name, e.request()?.httpMethod())
            val message = when {
                e.status() == HttpStatus.FORBIDDEN.value() || e.status() == HttpStatus.UNAUTHORIZED.value() ->
                    "Access denied"
                platform == Platform.TRAINING_PEAKS && e.status() == HttpStatus.PAYMENT_REQUIRED.value() ->
                    "HTTP 402: Free accounts have limits on syncing future workouts. " +
                        "Tomorrow may be available later; try again then or sync today. " +
                        "Check your calendar before retrying, as some workouts may already have been synced."
                else -> "HTTP request failed with status ${e.status()}"
            }
            throw PlatformException(platform, message)
        }
    }
}
