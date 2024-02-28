package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClientConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    value = "TrainingPeaksPlanApiClient",
    url = "\${training-peaks.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainingPeaksApiClientConfig::class]
)
interface TrainingPeaksPlanApiClient {
    @GetMapping("/plans/v1/plans")
    fun getPlans(): List<TPPlanDto>
}
