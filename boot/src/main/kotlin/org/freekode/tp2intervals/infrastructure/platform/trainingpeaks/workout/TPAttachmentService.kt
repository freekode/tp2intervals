package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.domain.workout.Attachment
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.springframework.stereotype.Service

@Service
class TPAttachmentService(
    private val trainingPeaksApiClient: TrainingPeaksApiClient
) {
    fun getAttachments(userId: String, workoutId: String): List<Attachment> {
        val workoutDetails = trainingPeaksApiClient.getWorkoutDetails(userId, workoutId)
        return workoutDetails.attachmentFileInfos
            .map {
                val attachment = trainingPeaksApiClient.downloadWorkoutAttachment(userId, workoutId, it.fileId)
                Attachment(it.fileName, attachment)
            }
    }
}