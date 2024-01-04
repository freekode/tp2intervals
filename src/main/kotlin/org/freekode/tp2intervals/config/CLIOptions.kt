package org.freekode.tp2intervals.config

import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

class CLIOptions {
    fun getCLIOptions(): Options {
        val options = Options()

        var option = Option.builder().longOpt("copy-plan").numberOfArgs(2).build()
        options.addOption(option)

        option = Option.builder().longOpt("plan-workout").build()
        options.addOption(option)

        return options
    }
}
