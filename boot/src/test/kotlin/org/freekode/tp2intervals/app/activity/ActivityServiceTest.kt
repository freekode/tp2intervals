package org.freekode.tp2intervals.app.activity

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ActivityServiceTest {

    private val trainingPeaksRepo: ActivityRepository = mockk()
    private val intervalsRepo: ActivityRepository = mockk()

    private lateinit var service: ActivityService

    @BeforeEach
    fun setup() {
        every { trainingPeaksRepo.platform() } returns Platform.TRAINING_PEAKS
        every { intervalsRepo.platform() } returns Platform.INTERVALS

        service = ActivityService(listOf(trainingPeaksRepo, intervalsRepo))
    }

    @Test
    fun `should sync activities with resource`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)
        val activity1 = createActivity(TrainingType.RIDE, "Ride 1", "resource-data-1")
        val activity2 = createActivity(TrainingType.RUN, "Run 1", "resource-data-2")
        every { trainingPeaksRepo.getActivities(startDate, endDate, TrainingType.DEFAULT_LIST) } returns listOf(activity1, activity2)

        val request = CopyActivitiesRequest(
            startDate, endDate,
            TrainingType.DEFAULT_LIST,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.syncActivities(request)

        assert(response.copied == 2)
        assert(response.filteredOut == 0)
        verify { intervalsRepo.saveActivities(listOf(activity1, activity2)) }
    }

    @Test
    fun `should filter activities without resource`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)
        val activityWithResource = createActivity(TrainingType.RIDE, "Ride", "resource")
        val activityWithoutResource = createActivity(TrainingType.RUN, "Run", null)
        every { trainingPeaksRepo.getActivities(startDate, endDate, TrainingType.DEFAULT_LIST) } returns listOf(activityWithResource, activityWithoutResource)

        val request = CopyActivitiesRequest(
            startDate, endDate,
            TrainingType.DEFAULT_LIST,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.syncActivities(request)

        assert(response.copied == 1)
        assert(response.filteredOut == 1)
        verify { intervalsRepo.saveActivities(match { it.first().title == "Ride" }) }
    }

    @Test
    fun `should return empty response when no activities`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)
        every { trainingPeaksRepo.getActivities(startDate, endDate, TrainingType.DEFAULT_LIST) } returns emptyList()

        val request = CopyActivitiesRequest(
            startDate, endDate,
            TrainingType.DEFAULT_LIST,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.syncActivities(request)

        assert(response.copied == 0)
        assert(response.filteredOut == 0)
    }

    private fun createActivity(type: TrainingType, title: String, resource: String?): Activity {
        return Activity(
            startedAt = LocalDateTime.now(),
            type = type,
            title = title,
            resource = resource
        )
    }
}
