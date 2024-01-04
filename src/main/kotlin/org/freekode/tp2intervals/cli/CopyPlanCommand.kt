package org.freekode.tp2intervals.cli

import org.apache.commons.cli.Option
import org.freekode.tp2intervals.app.MainService
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CopyPlanCommand(val mainService: MainService) : CLICommand {
    override fun option(): Option = Option.builder().longOpt("copy-plan").numberOfArgs(2).build()

    override fun run(params: List<String>) {
        val startDate = LocalDate.parse(params[0])
        val endDate = LocalDate.parse(params[1])
        mainService.copyPlanFromThirdParty(startDate, endDate)
    }
}
