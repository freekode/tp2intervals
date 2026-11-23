package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import com.fasterxml.jackson.databind.JsonMappingException
import config.mock.ObjectMapperFactory
import feign.Client
import feign.Feign
import feign.Response
import feign.codec.DecodeException
import java.time.LocalDate
import org.freekode.tp2intervals.app.workout.PreviewProvider
import org.freekode.tp2intervals.app.workout.PreviewStage
import org.freekode.tp2intervals.app.workout.TrainingPeaksPreviewFailure
import org.freekode.tp2intervals.infrastructure.ProviderReadFailure
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

class IntervalsCalendarDecodingTest {
    private val day = LocalDate.of(2026, 11, 21)
    private val workout = """{"id":12345,"category":"WORKOUT","type":"Ride","start_date_local":"2026-11-21T09:30:00","description":"//////////\ntrainingPeaksId=42"}"""

    @Test
    fun `calendar identity decodes an open workout document and a nameless non-workout event`() {
        val openDocument = workout.dropLast(1) + ""","name":null,"workout_doc":{"steps":null,"target":null}}"""
        val note = """{"id":12346,"category":"NOTE","start_date_local":"2026-11-21T00:00:00"}"""
        val repo = repository("[$openDocument,$note]")
        val events = repo.getCalendarSnapshot("synthetic", day, day)
        assertEquals(listOf(12345L, 12346L), events.map { it.id })
        assertEquals(day.atTime(9, 30), events[0].start_date_local)
        assertEquals("//////////\ntrainingPeaksId=42", events[0].description)
        assertEquals("Ride", events[0].type)
        assertEquals("NOTE", events[1].category)
        assertEquals(events[0].description, repository(openDocument).getCalendarEvent("synthetic", 12345)?.description)
    }

    @Test
    fun `the workout conversion DTO rejects documented open workout documents`() {
        val fullWorkout = workout.dropLast(1) + ""","name":"Synthetic","workout_doc":{"steps":null,"target":null}}"""
        val mapper = ObjectMapperFactory.objectMapper()
        val failure = assertThrows(JsonMappingException::class.java) {
            mapper.readValue(fullWorkout, IntervalsEventDTO::class.java)
        }
        assertEquals(listOf("workout_doc", "steps"), failure.path.map { it.fieldName })
        assertEquals(12345L, repository(fullWorkout).getCalendarEvent("synthetic", 12345)?.id)
    }

    @Test
    fun `invalid identity or date fails the entire listing with a sanitized Jackson field path`() {
        val invalidFields = listOf(
            "id" to workout.replace("12345", "1.5"),
            "id" to workout.replace("12345", "\"12345\""),
            "category" to workout.replace("\"WORKOUT\"", "42"),
            "description" to workout.replace("\"//////////\\ntrainingPeaksId=42\"", "{}"),
            "start_date_local" to workout.replace("2026-11-21T09:30:00", "private-fixture-data"),
        )
        for ((field, invalid) in invalidFields) {
            val failure = assertThrows(DecodeException::class.java) {
                repository("[$workout,$invalid]").getCalendarSnapshot("synthetic", day, day)
            }
            assertTrue(generateSequence<Throwable>(failure) { it.cause }.any { it is JsonMappingException })
            val diagnostic = TrainingPeaksPreviewFailure.from(
                PreviewStage.INTERVALS_CALENDAR_LEGACY_WINDOW, PreviewProvider.INTERVALS, failure
            )
            assertEquals(200, diagnostic.upstreamStatus)
            assertEquals("INTERVALS_INVALID_RESPONSE", diagnostic.code)
            assertEquals("[*].$field", diagnostic.decoder?.path)
            assertFalse(diagnostic.decoder.toString().contains("private-fixture-data"))
        }
    }

    @Test
    fun `only a valid empty array counts as an empty calendar`() {
        fun snapshot(body: String, status: Int = 200, headers: Map<String, Collection<String>> = emptyMap()) =
            repository(body, status, headers).getCalendarSnapshot("synthetic", day, day)

        assertTrue(snapshot("[]").isEmpty())
        for (body in listOf("", "null", "[null]", "[${workout.replace("12345", "0")}]")) {
            assertThrows(ProviderReadFailure::class.java) { snapshot(body) }
        }
        for (body in listOf(
            """{"events":[]}""", "[", "[${workout.replace("12345", "null")}]",
            "[${workout.replace("\"id\":12345,", "")}]",
            "[${workout.replace("\"category\":\"WORKOUT\",", "")}]",
            "[${workout.replace("\"start_date_local\":\"2026-11-21T09:30:00\",", "")}]",
        )) {
            assertThrows(DecodeException::class.java) { snapshot(body) }
        }
        assertThrows(ProviderReadFailure::class.java) { snapshot("[]", 206) }
        assertThrows(ProviderReadFailure::class.java) {
            snapshot("[]", headers = mapOf("Content-Range" to listOf("items 0-0/10")))
        }
    }

    @Test
    fun `an event lookup cannot confirm a different ID or a partial response`() {
        assertThrows(ProviderReadFailure::class.java) {
            repository(workout).getCalendarEvent("synthetic", 12346)
        }
        assertThrows(ProviderReadFailure::class.java) {
            repository(workout, 206).getCalendarEvent("synthetic", 12345)
        }
    }

    private fun repository(
        body: String, status: Int = 200, extraHeaders: Map<String, Collection<String>> = emptyMap(),
    ): IntervalsWorkoutRepository {
        val client = Client { request, _ ->
            val responseHeaders = mapOf("Content-Type" to listOf("application/json")) + extraHeaders
            Response.builder().request(request).status(status).headers(responseHeaders)
                .body(body, Charsets.UTF_8).build()
        }
        val converters = HttpMessageConverters(MappingJackson2HttpMessageConverter(ObjectMapperFactory.objectMapper()))
        val api = Feign.builder().client(client).contract(SpringMvcContract())
            .decoder(ResponseEntityDecoder(SpringDecoder { converters }))
            .target(IntervalsApiClient::class.java, "http://synthetic.invalid")
        return IntervalsWorkoutRepository(api, mock(IntervalsConfigurationRepository::class.java))
    }
}
