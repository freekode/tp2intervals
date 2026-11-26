package org.freekode.tp2intervals.infrastructure

import feign.FeignException
import feign.Request
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.freekode.tp2intervals.domain.Platform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class CatchFeignExceptionAspectTest {
    private val aspect = CatchFeignExceptionAspect()

    @Test
    fun `explains TrainingPeaks 402 and warns of partial sync`() {
        val error = invokeFailure("trainingPeaks", 402)

        assertEquals(Platform.TRAINING_PEAKS, error.platform)
        assertEquals(
            "HTTP 402: Free accounts have limits on syncing future workouts. " +
                "Tomorrow may be available later; try again then or sync today. " +
                "Check your calendar before retrying, as some workouts may already have been synced.",
            error.message,
        )
    }

    @Test
    fun `preserves generic errors for other platforms and authentication failures`() {
        assertEquals("HTTP request failed with status 402", invokeFailure("intervals", 402).message)
        assertEquals("Access denied", invokeFailure("trainingPeaks", 401).message)
        assertEquals("HTTP request failed with status 500", invokeFailure("trainingPeaks", 500).message)
    }

    private fun invokeFailure(methodName: String, status: Int): PlatformException {
        val joinPoint = mock(ProceedingJoinPoint::class.java)
        val signature = mock(MethodSignature::class.java)
        val exception = mock(FeignException::class.java)
        val request = mock(Request::class.java)
        `when`(joinPoint.signature).thenReturn(signature)
        `when`(signature.method).thenReturn(AnnotatedMethods::class.java.getDeclaredMethod(methodName))
        `when`(exception.status()).thenReturn(status)
        `when`(exception.request()).thenReturn(request)
        `when`(request.httpMethod()).thenReturn(Request.HttpMethod.POST)
        `when`(joinPoint.proceed()).thenThrow(exception)
        return assertThrows(PlatformException::class.java) { aspect.trace(joinPoint) }
    }

    private class AnnotatedMethods {
        @CatchFeignException(Platform.TRAINING_PEAKS)
        fun trainingPeaks() = Unit

        @CatchFeignException(Platform.INTERVALS)
        fun intervals() = Unit
    }
}
