package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import config.mock.ObjectMapperFactory
import config.mock.TrainerRoadApiClientMock
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClientService
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadCalendarTimelineDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRWorkoutResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfiguration
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.member.TRUsernameRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.util.ResourceUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime

class TrainerRoadWorkoutRepositoryTest {
    private val objectMapper = ObjectMapperFactory.objectMapper()

    private val trainerRoadApiClient = TrainerRoadApiClientMock(
        objectMapper,
        ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json").inputStream(),
        ResourceUtils.getFile("classpath:tr-workoutsdetails-complex.json").inputStream(),
        ResourceUtils.getFile("classpath:tr-workoutsdetails-another.json").inputStream(),
    )

    private val trainerRoadConfigurationRepository = trainerRoadConfigurationRepository()

    private val trainerRoadApiClientService =
        TrainerRoadApiClientService(trainerRoadApiClient, trainerRoadConfigurationRepository)

    private val trainerRoadWorkoutRepository =
        TrainerRoadWorkoutRepository(
            mock(TRUsernameRepository::class.java),
            trainerRoadApiClientService,
            trainerRoadConfigurationRepository,
        )

    @Test
    fun `should parse simple workout`() {
        // when
        val data = ExternalData(null, null, "simple")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        val structure = workout.structure!!

        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(11, structure.steps.size)
        assertEquals(5 * 60.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(50, (structure.steps[0] as SingleStep).target.start)
        assertEquals(50, (structure.steps[0] as SingleStep).target.end)
        assertEquals(3 * 60.toLong(), (structure.steps[1] as SingleStep).length.value)
        assertEquals(60, (structure.steps[1] as SingleStep).target.start)
        assertEquals(60, (structure.steps[1] as SingleStep).target.end)
        assertEquals(13 * 60.toLong(), (structure.steps[2] as SingleStep).length.value)
        assertEquals(50, (structure.steps[2] as SingleStep).target.start)
        assertEquals(50, (structure.steps[2] as SingleStep).target.end)
    }

    @Test
    fun `calendar retains planned activity identity through start time assignment`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val timeline = TrainerRoadCalendarTimelineDTO(listOf(
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "occurrence-1", "Workout", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 23), 5460,
            ),
        ), date.atStartOfDay(), date.plusDays(1).atStartOfDay())
        val details = objectMapper.readValue(
            ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json"), TRWorkoutResponseDTO::class.java,
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(timeline)
        `when`(api.getWorkout("5460")).thenReturn(details)
        val service = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)

        val workout = service.getWorkoutsFromCalendar(date, date, 123).single().withStartTime(LocalTime.NOON)

        assertEquals(date, workout.date)
    }

    @Test
    fun `calendar reads optional planned times per occurrence`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val timeline = objectMapper.readValue(
            """{"Start":"2026-11-23T00:00:00","End":"2026-11-24T00:00:00","PlannedActivities":[
                {"Id":"1","Title":"Workout","Date":{"Year":2026,"Month":11,"Day":23},"WorkoutId":5460,"TimeOfDay":"00:30:00"},
                {"Id":"2","Title":"Workout","Date":{"Year":2026,"Month":11,"Day":23},"WorkoutId":5460,"TimeOfDay":null},
                {"Id":"3","Title":"Workout","Date":{"Year":2026,"Month":11,"Day":23},"WorkoutId":5460}
            ]}""",
            TrainerRoadCalendarTimelineDTO::class.java,
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(timeline)
        `when`(api.getWorkout("5460")).thenReturn(objectMapper.readValue(
            ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json"), TRWorkoutResponseDTO::class.java,
        ))

        val workouts = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)
            .getWorkoutsFromCalendar(date, date, 123)

        assertEquals(listOf(LocalTime.of(0, 30), null, null), workouts.map { it.startTime })
    }

    @Test
    fun `calendar rejects invalid workout IDs`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val activity = TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
            "invalid", "Workout", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 23), 0,
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(
            TrainerRoadCalendarTimelineDTO(listOf(activity), date.atStartOfDay(), date.plusDays(1).atStartOfDay()),
        )

        assertThrows(PlatformException::class.java) {
            TrainerRoadApiClientService(api, trainerRoadConfigurationRepository).getWorkoutsFromCalendar(date, date, 123)
        }
    }

    @Test
    fun `reconciliation requires full timeline coverage even for an empty day`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val service = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)
        val start = date.atStartOfDay()
        val timeline = objectMapper.readValue(
            """{"Start":"2026-11-23T00:00:00","End":"2026-11-24T00:00:00","PlannedActivities":[]}""",
            TrainerRoadCalendarTimelineDTO::class.java,
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(timeline)
        assertTrue(service.getWorkoutsFromCalendar(date, date, 123, true).isEmpty())

        `when`(api.getCalendarTimeline(123)).thenReturn(
            TrainerRoadCalendarTimelineDTO(emptyList(), start, LocalDateTime.of(2026, 11, 23, 23, 59)),
        )
        assertThrows(PlatformException::class.java) {
            service.getWorkoutsFromCalendar(date, date, 123, true)
        }
        `when`(api.getCalendarTimeline(123)).thenReturn(TrainerRoadCalendarTimelineDTO(emptyList()))
        assertThrows(PlatformException::class.java) {
            service.getWorkoutsFromCalendar(date, date, 123, true)
        }
    }

    @Test
    fun `calendar copy rejects timelines that do not cover the requested range`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val service = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)
        `when`(api.getCalendarTimeline(123)).thenReturn(
            TrainerRoadCalendarTimelineDTO(emptyList(), date.atStartOfDay(), date.plusDays(1).atStartOfDay()),
        )
        assertTrue(service.getWorkoutsFromCalendar(date, date, 123).isEmpty())

        assertThrows(PlatformException::class.java) {
            service.getWorkoutsFromCalendar(date.minusDays(1), date, 123)
        }
        assertThrows(PlatformException::class.java) {
            service.getWorkoutsFromCalendar(date, date.plusDays(1), 123)
        }
        `when`(api.getCalendarTimeline(123)).thenReturn(TrainerRoadCalendarTimelineDTO(emptyList()))
        assertThrows(PlatformException::class.java) {
            service.getWorkoutsFromCalendar(date, date, 123)
        }
    }

    @Test
    fun `reconciliation rejects mismatched workout detail ID`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val activity = TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
            "occurrence", "Workout", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 23), 123,
        )
        val timeline = TrainerRoadCalendarTimelineDTO(listOf(activity), date.atStartOfDay(), date.plusDays(1).atStartOfDay())
        val details = objectMapper.readValue(
            ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json"), TRWorkoutResponseDTO::class.java,
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(timeline)
        `when`(api.getWorkout("123")).thenReturn(details)
        val service = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)
        assertThrows(PlatformException::class.java) {
            service.getWorkoutsFromCalendar(date, date, 123, true)
        }
    }

    @Test
    fun `reconciliation rejects a planned activity without a workout ID in the selected range`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val activity = TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
            "unknown", "Planned activity", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 23), null,
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(
            TrainerRoadCalendarTimelineDTO(listOf(activity), date.atStartOfDay(), date.plusDays(2).atStartOfDay()),
        )
        val service = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)

        assertThrows(PlatformException::class.java) {
            service.getWorkoutsFromCalendar(date, date, 123, true)
        }
        val result = service.getReconciliationSource(date, date, 123)
        assertTrue(result.workouts.isEmpty())
        assertEquals(listOf(date), result.unidentifiedActivityDates)
        assertTrue(service.getWorkoutsFromCalendar(date.plusDays(1), date.plusDays(1), 123, true).isEmpty())
        assertTrue(service.getReconciliationSource(date.plusDays(1), date.plusDays(1), 123).unidentifiedActivityDates.isEmpty())
    }

    @Test
    fun `ordinary calendar copy rejects unidentified activities before fetching workouts`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val activities = listOf(
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "known", "Workout", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 23), 5460,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "unknown", null, TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 23), null,
            ),
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(
            TrainerRoadCalendarTimelineDTO(activities, date.atStartOfDay(), date.plusDays(1).atStartOfDay()),
        )
        `when`(api.getWorkout("5460")).thenReturn(objectMapper.readValue(
            ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json"), TRWorkoutResponseDTO::class.java,
        ))
        val service = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)

        assertThrows(PlatformException::class.java) { service.getWorkoutsFromCalendar(date, date, 123) }
        org.mockito.Mockito.verify(api, org.mockito.Mockito.never()).getWorkout(org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    fun `reconciliation returns identifiable workouts and dates of unidentified activities`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val activities = listOf(
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "before", "Planned activity", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 22), null,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "unknown", "Planned activity", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 23), null,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "known", "Workout", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 24), 5460,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "end", "Planned activity", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 12, 6), null,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "after", "Planned activity", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 12, 7), null,
            ),
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(
            TrainerRoadCalendarTimelineDTO(activities, date.atStartOfDay(), date.plusDays(14).atStartOfDay()),
        )
        `when`(api.getWorkout("5460")).thenReturn(objectMapper.readValue(
            ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json"), TRWorkoutResponseDTO::class.java,
        ))
        val result = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)
            .getReconciliationSource(date, date.plusDays(13), 123)

        assertEquals(listOf(date, date.plusDays(13)), result.unidentifiedActivityDates)
        assertEquals(date.plusDays(1), result.workouts.single().date)
        assertEquals("5460", result.workouts.single().details.externalData.trainerRoadId)
    }

    @Test
    fun `blank titled entries without workout IDs are assumed rest days`() {
        val api = mock(TrainerRoadApiClient::class.java)
        val date = LocalDate.of(2026, 11, 23)
        val activities = listOf(
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "rest", "", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 23), null,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "blank", "  ", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 24), null,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "untitled-workout", "", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 24), 5460,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "unknown", "Planned activity", TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 25), null,
            ),
            TrainerRoadCalendarTimelineDTO.PlannedActivityDTO(
                "missing-title", null, TrainerRoadCalendarTimelineDTO.CalendarDateDTO(2026, 11, 26), null,
            ),
        )
        `when`(api.getCalendarTimeline(123)).thenReturn(
            TrainerRoadCalendarTimelineDTO(activities, date.atStartOfDay(), date.plusDays(4).atStartOfDay()),
        )
        `when`(api.getWorkout("5460")).thenReturn(objectMapper.readValue(
            ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json"), TRWorkoutResponseDTO::class.java,
        ))
        val service = TrainerRoadApiClientService(api, trainerRoadConfigurationRepository)

        val result = service.getReconciliationSource(date, date.plusDays(3), 123)

        assertEquals(listOf(date.plusDays(2), date.plusDays(3)), result.unidentifiedActivityDates)
        assertEquals(date.plusDays(1), result.workouts.single().date)
        assertTrue(service.getWorkoutsFromCalendar(date, date, 123, true).isEmpty())
    }

    @Test
    fun `should parse complex workout`() {
        // when
        val data = ExternalData(null, null, "complex")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        val structure = workout.structure!!

        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(23, structure.steps.size)
        assertEquals(4 * 60.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(50, (structure.steps[0] as SingleStep).target.start)
        assertEquals(50, (structure.steps[0] as SingleStep).target.end)
        assertEquals(2 * 60.toLong(), (structure.steps[1] as SingleStep).length.value)
        assertEquals(55, (structure.steps[1] as SingleStep).target.start)
        assertEquals(55, (structure.steps[1] as SingleStep).target.end)
        assertEquals(2 * 60.toLong(), (structure.steps[2] as SingleStep).length.value)
        assertEquals(60, (structure.steps[2] as SingleStep).target.start)
        assertEquals(60, (structure.steps[2] as SingleStep).target.end)
    }

    @Test
    fun `should print wrong rest api response`() {
        // when
        val data = ExternalData(null, null, "another")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, workout.structure!!.target)
        assertTrue(workout.structure!!.steps.isNotEmpty())
    }

    @Test
    fun `should exclude html tags from description`() {
        val data = ExternalData(null, null, "simple")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)
        assertTrue(workout.details.description!!.startsWith(
            "simple is 4x3-minute intervals of leg-speed drills at a very low 60% FTP"
        ))
        assertTrue(workout.details.description!!.contains("Goal:"))
        assertTrue(workout.details.description!!.contains("Zone: Endurance"))
    }

    @Test
    fun `should use WorkoutData targets instead of zero interval fallbacks`() {
        val dto = objectMapper.readValue(
            """
            {
              "Workout": {
                "Details": {
                  "Id": "265566",
                  "WorkoutName": "Galena -2",
                  "WorkoutDescription": "Synthetic workout",
                  "IsOutside": false,
                  "Tss": 72,
                  "Duration": 60
                },
                "IntervalData": [
                  {
                    "Start": 720,
                    "End": 1920,
                    "Name": "Sweet Spot 2",
                    "IsFake": false,
                    "TestInterval": false,
                    "StartTargetPowerPercent": 0,
                    "EndTargetPowerPercent": 0
                  }
                ],
                "WorkoutData": [
                  {"Tick": 720, "FtpPercent": 90},
                  {"Tick": 1919, "FtpPercent": 94}
                ]
              }
            }
            """.trimIndent(),
            TRWorkoutResponseDTO::class.java,
        )

        val workout = TrainerRoadWorkoutMapper().toWorkout(dto, true)
        val step = workout.structure!!.steps.single() as SingleStep
        assertEquals(90, step.target.start)
        assertEquals(94, step.target.end)
    }

    @Test
    fun `uses interval targets when workout samples belong to another interval`() {
        val dto = objectMapper.readValue(
            """
            {
              "Workout": {
                "Details": {
                  "Id": "123", "WorkoutName": "Test", "WorkoutDescription": "Test",
                  "IsOutside": false, "Tss": 40, "Duration": 30
                },
                "IntervalData": [
                  {
                    "Start": 0, "End": 150, "Name": "Warmup", "IsFake": false,
                    "TestInterval": false, "StartTargetPowerPercent": 50, "EndTargetPowerPercent": 55
                  },
                  {
                    "Start": 300, "End": 600, "Name": "Partial", "IsFake": false,
                    "TestInterval": false, "StartTarget": [60, 65]
                  },
                  {
                    "Start": 720, "End": 1920, "Name": "Work", "IsFake": false,
                    "TestInterval": false, "StartTargetPowerPercent": 0, "EndTargetPowerPercent": 0
                  }
                ],
                "WorkoutData": [
                  {"Tick": 300, "FtpPercent": 62},
                  {"Tick": 720, "FtpPercent": 90},
                  {"Tick": 1919, "FtpPercent": 94}
                ]
              }
            }
            """.trimIndent(),
            TRWorkoutResponseDTO::class.java,
        )

        val steps = TrainerRoadWorkoutMapper().toWorkout(dto, true).structure!!.steps
        val warmup = steps[0] as SingleStep
        val partial = steps[1] as SingleStep
        val work = steps[2] as SingleStep
        assertEquals(50, warmup.target.start)
        assertEquals(55, warmup.target.end)
        assertEquals(62, partial.target.start)
        assertEquals(65, partial.target.end)
        assertEquals(90, work.target.start)
        assertEquals(94, work.target.end)
    }

    @Test
    fun `rejects intervals with no start target but accepts explicit zero`() {
        fun workout(intervalTarget: String) = objectMapper.readValue(
            """
            {"Workout":{"Details":{"Id":"123","WorkoutName":"Test","WorkoutDescription":"Test",
              "IsOutside":false,"Tss":40,"Duration":30},
              "IntervalData":[{"Start":0,"End":60,"Name":"Step","IsFake":false,
                "TestInterval":false $intervalTarget}]}}
            """.trimIndent(),
            TRWorkoutResponseDTO::class.java,
        )
        val mapper = TrainerRoadWorkoutMapper()

        val error = assertThrows(PlatformException::class.java) { mapper.toWorkout(workout(""), true) }
        assertTrue(error.message!!.contains("power target"))
        val zero = mapper.toWorkout(workout(""", "StartTargetPowerPercent":0"""), true)
        assertEquals(0, (zero.structure!!.steps.single() as SingleStep).target.start)
    }

    @Test
    fun `should preserve workout without interval steps`() {
        val dto = objectMapper.readValue(
            """
            {
              "Workout": {
                "Details": {
                  "Id": "planned-no-steps",
                  "WorkoutName": "Planned workout",
                  "WorkoutDescription": "Synthetic unstructured workout",
                  "IsOutside": false,
                  "Tss": 40,
                  "Duration": 45
                },
                "IntervalData": []
              }
            }
            """.trimIndent(),
            TRWorkoutResponseDTO::class.java,
        )

        val workout = TrainerRoadWorkoutMapper().toWorkout(dto, true)

        assertEquals("Planned workout", workout.details.name)
        assertEquals(null, workout.structure)
    }

    @Test
    fun `should parse PascalCase startTarget range`() {
        val dto = objectMapper.readValue(
            """
            {
              "Workout": {
                "Details": {
                  "Id": "ramp-target",
                  "WorkoutName": "Ramped workout",
                  "WorkoutDescription": "Synthetic ramp",
                  "IsOutside": false,
                  "Tss": 50,
                  "Duration": 30
                },
                "IntervalData": [
                  {
                    "Start": 0,
                    "End": 600,
                    "Name": "Ramp",
                    "IsFake": false,
                    "TestInterval": false,
                    "StartTarget": [60, 90]
                  }
                ]
              }
            }
            """.trimIndent(),
            TRWorkoutResponseDTO::class.java,
        )

        val workout = TrainerRoadWorkoutMapper().toWorkout(dto, true)
        val step = workout.structure!!.steps.single() as SingleStep

        assertEquals(60, step.target.start)
        assertEquals(90, step.target.end)
    }

    private fun trainerRoadConfigurationRepository(): TrainerRoadConfigurationRepository {
        val mock = mock(TrainerRoadConfigurationRepository::class.java)
        `when`(mock.getConfiguration()).thenReturn(TrainerRoadConfiguration(null, true))
        return mock
    }
}
