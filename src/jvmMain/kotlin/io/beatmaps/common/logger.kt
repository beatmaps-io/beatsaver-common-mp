package io.beatmaps.common

import java.util.logging.Level
import java.util.logging.Logger

private val appLogger = Logger.getLogger("bmio")

fun setupLogging() {
    Logger.getLogger("").level = Level.OFF
    appLogger.level = Level.INFO
}
