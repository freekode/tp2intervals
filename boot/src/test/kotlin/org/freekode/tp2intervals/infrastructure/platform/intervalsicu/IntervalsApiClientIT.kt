package org.freekode.tp2intervals.infrastructure.platform.intervalsicu

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.workout.TrainerRoadSyncMode
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateEventRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.TrainerRoadSyncedEventRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("it")
class IntervalsApiClientIT {
    companion object {
        @JvmStatic
        @RegisterExtension
        val wireMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build()

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("app.intervals.api-url") { wireMock.baseUrl() }
        }
    }

    @Autowired
    lateinit var client: IntervalsApiClient

    @Autowired
    lateinit var workouts: IntervalsWorkoutRepository

    @Autowired
    lateinit var trackedEvents: TrainerRoadSyncedEventRepository

    @Test
    fun `update sends PUT and decodes event id, while 404 is not a successful update`() {
        val path = "/api/v1/athlete/i112233/events/42"
        wireMock.stubFor(put(urlEqualTo(path)).willReturn(okJson("""{"id":42,"name":"Workout","category":"WORKOUT"}""")))
        val request = CreateEventRequestDTO("2026-11-23T18:00:00", "Workout", "Ride", "WORKOUT", "description")

        assertEquals(42L, client.updateEvent("i112233", "42", request)?.id)
        wireMock.verify(putRequestedFor(urlEqualTo(path)).withRequestBody(matchingJsonPath("$.name", equalTo("Workout"))))

        wireMock.stubFor(put(urlEqualTo("/api/v1/athlete/i112233/events/43")).willReturn(notFound()))
        assertNull(client.updateEvent("i112233", "43", request))
    }

    @Test
    fun `created event ownership persists per athlete and is removed after deletion`() {
        val collection = "/api/v1/athlete/i112233/events"
        val event = "$collection/142"
        wireMock.stubFor(post(urlEqualTo(collection)).willReturn(okJson("""{"id":142}""")))
        wireMock.stubFor(delete(urlEqualTo(event)).willReturn(ok()))
        val source = Workout(
            WorkoutDetails(TrainingType.BIKE, "Workout", null, null, null, ExternalData.empty().withTrainerRoad("123")),
            LocalDate.of(2026, 11, 23), null,
        )
        val target = source.copy(details = source.details.copy(
            description = source.details.externalData.toSimpleString(),
            externalData = source.details.externalData.withIntervals("142"),
        ))

        workouts.saveTrainerRoadWorkoutsToCalendar(listOf(source))

        wireMock.verify(postRequestedFor(urlEqualTo(collection)))
        assertNotNull(trackedEvents.findByAthleteIdAndEventIdAndTrainerRoadId("i112233", "142", "123"))
        assertNull(trackedEvents.findByAthleteIdAndEventIdAndTrainerRoadId("another-athlete", "142", "123"))
        assertTrue(workouts.isTrackedTrainerRoadWorkout(target))
        assertFalse(workouts.isTrackedTrainerRoadWorkout(target.copy(details = target.details.copy(
            externalData = ExternalData.empty().withTrainerRoad("other").withIntervals("142"),
        ))))

        wireMock.stubFor(get(urlPathEqualTo(collection)).willReturn(okJson(
            """[{"id":142,"name":"Edited","description":"No sync metadata","start_date_local":"2026-11-23T18:00:00","category":"WORKOUT","type":"Ride","workout_doc":{"steps":"unsupported"}}]""",
        )))
        val origin = mock(WorkoutRepository::class.java)
        val date = requireNotNull(source.date)
        `when`(origin.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(origin.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(source))
        val service = WorkoutService(listOf(origin, workouts), emptyList())
        val preview = service.previewTrainerRoadSync(CopyFromCalendarToCalendarRequest(
            date, date, listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS,
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        ))
        assertEquals(0, preview.copied)
        assertEquals(0, preview.updated)
        assertEquals(1, preview.skipped)

        workouts.deleteSyncedWorkout(target)

        wireMock.verify(deleteRequestedFor(urlEqualTo(event)))
        assertNull(trackedEvents.findByAthleteIdAndEventIdAndTrainerRoadId("i112233", "142", "123"))
    }
}
