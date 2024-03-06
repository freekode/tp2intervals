package org.freekode.tp2intervals.rest.plan

import org.freekode.tp2intervals.app.plan.CopyPlanRequest
import org.freekode.tp2intervals.app.plan.CopyPlanResponse
import org.freekode.tp2intervals.app.plan.PlanService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PlanController(
    private val planService: PlanService
) {

    @GetMapping("/api/plan")
    fun getPlans(@RequestParam platform: Platform): List<Plan> {
        return planService.getPlans(platform)
    }

    @PostMapping("/api/plan/copy")
    fun copyPlan(@RequestBody request: CopyPlanRequest): CopyPlanResponse {
        return planService.copyPlan(request)
    }
}
