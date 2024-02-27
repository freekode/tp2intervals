package org.freekode.tp2intervals.infrastructure

import feign.FeignException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component


@Aspect
@Component
class CatchFeignExceptionAspect {
    @Around("@annotation(CatchFeignException)")
    @Throws(Throwable::class)
    fun trace(joinPoint: ProceedingJoinPoint): Any? {
        val method = (joinPoint.signature as MethodSignature).method
        val annotation = method.getAnnotation(CatchFeignException::class.java)
        val platform = annotation.platform
        try {
            return joinPoint.proceed()
        } catch (e: FeignException) {
            throw PlatformException(platform, e.message ?: "Unknown error")
        }
    }
}
