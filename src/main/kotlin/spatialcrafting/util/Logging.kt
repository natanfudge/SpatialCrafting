package spatialcrafting.util

import org.apache.logging.log4j.LogManager
import spatialcrafting.ModId
import java.util.*

private const val LogDebug = true

fun logDebug(message: String)  = if(LogDebug) println("${Date()} [SC/DEBUG]: $message") else Unit