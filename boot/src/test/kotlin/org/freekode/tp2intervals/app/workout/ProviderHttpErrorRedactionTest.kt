package org.freekode.tp2intervals.app.workout

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import config.mock.ObjectMapperFactory
import feign.FeignException
import feign.Request
import feign.Response
import org.freekode.tp2intervals.rest.RestResponseEntityExceptionHandler
import org.freekode.tp2intervals.rest.workout.WorkoutController
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ProviderHttpErrorRedactionTest {
    @Test
    fun `manual copy does not expose upstream bodies URLs or exception stacks`() {
        val secret = "fixture-private-provider-values"
        val request = Request.create(Request.HttpMethod.GET, "https://provider.example.test/?value=$secret",
            mapOf("Authorization" to listOf(secret)), null, Charsets.UTF_8, null)
        val failure = FeignException.errorStatus("synthetic", Response.builder().request(request)
            .status(403).reason(secret).body(secret, Charsets.UTF_8).build())
        val service = mock(WorkoutService::class.java) { throw failure }
        val mapper = ObjectMapperFactory.objectMapper()
        val mvc = MockMvcBuilders.standaloneSetup(WorkoutController(service))
            .setControllerAdvice(RestResponseEntityExceptionHandler())
            .setMessageConverters(MappingJackson2HttpMessageConverter(mapper)).build()
        val logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler::class.java) as Logger
        val logs = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(logs)
        try {
            val body = mvc.perform(post("/api/workout/copy-calendar-to-calendar").contentType(MediaType.APPLICATION_JSON)
                .content("""{"startDate":"2026-01-01","endDate":"2026-01-01","types":["BIKE"],
                    "skipSynced":true,"sourcePlatform":"TRAINING_PEAKS","targetPlatform":"INTERVALS"}"""))
                .andExpect(status().isBadGateway).andReturn().response.contentAsString
            assertEquals("Access denied by provider", mapper.readTree(body)["message"].asText())
            assertFalse(secret in body)
            assertTrue("status=403" in logs.list.single().formattedMessage)
            assertFalse(secret in logs.list.single().formattedMessage)
            assertNull(logs.list.single().throwableProxy)
        } finally {
            logger.detachAppender(logs)
            logs.stop()
        }
    }
}
