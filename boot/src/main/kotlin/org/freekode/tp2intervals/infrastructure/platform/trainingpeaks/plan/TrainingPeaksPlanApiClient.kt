package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClientConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "TrainingPeaksPlanApiClient",
    url = "\${app.training-peaks.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainingPeaksApiClientConfig::class]
)
interface TrainingPeaksPlanApiClient {
    @GetMapping("/plans/v1/plans/{planId}")
    fun getPlan(@PathVariable planId: String): TPPlanDto

    @GetMapping("/plans/v1/plans")
    fun getPlans(): List<TPPlanDto>

    @PostMapping("plans/v1/commands/applyplan")
    fun applyPlan(@RequestBody applyTPPlanRequestDTO: List<ApplyTPPlanRequestDTO>): List<ApplyTPPlanResponseDTO>

    @PostMapping("plans/v1/commands/removeplan")
    fun removePlan(@RequestBody request: Map<String, String>)
}
