package org.freekode.tp2intervals.cli

import org.apache.commons.cli.Option

interface CLICommand {
    fun option(): Option

    fun run(params: List<String>)
}
