package io.beatmaps.common

import java.util.logging.Level
import java.util.logging.Logger

fun setupLogging() {
    Logger.getLogger("").level = Level.OFF
    Logger.getLogger("bmio").level = Level.INFO
}
