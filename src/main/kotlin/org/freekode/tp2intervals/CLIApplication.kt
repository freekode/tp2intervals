package org.freekode.tp2intervals

import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.freekode.tp2intervals.cli.CLICommand
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsProperties
import org.freekode.tp2intervals.infrastructure.trainingpeaks.TrainingPeaksProperties
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
    IntervalsProperties::class, TrainingPeaksProperties::class
)
class CLIApplication(
    private val cliCommands: List<CLICommand>
) : CommandLineRunner {
    override fun run(vararg args: String) {
        log.info("params ${args.toList()}")
        val optionMap = cliCommands.associateBy { it.option() }

        val options = Options()
        optionMap.keys.forEach { options.addOption(it) }

        val parser: CommandLineParser = DefaultParser()
        val formatter = HelpFormatter()

        try {
            val cmd = parser.parse(options, args)
            if (cmd.options.isEmpty()) {
                log.info("No commands passed")
                return
            }

            cmd.options.forEach {
                optionMap[it]!!.run(it.valuesList)
            }
        } catch (e: ParseException) {
            log.error(e.message)
            formatter.printHelp("docker run ...", options)
            exitProcess(1)
        }
    }
}


fun main(args: Array<String>) {
    log.info("Starting the app")
    runApplication<CLIApplication>(*args)
    log.info("The app finished")
}
