package org.freekode.tp2intervals

import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import org.freekode.tp2intervals.app.MainService
import org.freekode.tp2intervals.config.CLIOptions
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsProperties
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import kotlin.system.exitProcess

val log: Logger = LoggerFactory.getLogger(CLIApplication::class.java)

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(
    IntervalsProperties::class, ThirdPartyProperties::class
)
class CLIApplication(
    private val mainService: MainService
) : CommandLineRunner {
    override fun run(vararg args: String) {
        val options = CLIOptions().getCLIOptions()
        val parser: CommandLineParser = DefaultParser()
        val formatter = HelpFormatter()

        try {
            val cmd = parser.parse(options, args)
        } catch (e: ParseException) {
            log.error(e.message)
            formatter.printHelp("utility-name", options)
            exitProcess(1)
        }
    }
}


fun main(args: Array<String>) {
    log.info("Starting the app")
    runApplication<CLIApplication>(*args)
    log.info("The app finished")
}
