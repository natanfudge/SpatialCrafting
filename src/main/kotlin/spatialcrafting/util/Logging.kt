package spatialcrafting.util

import org.apache.logging.log4j.LogManager
import spatialcrafting.ModId
import java.util.*

 const val LogDebug = true

inline fun logDebug(lazyMessage: () -> String)  = if(LogDebug) println("${Date()} [SC/DEBUG]: ${lazyMessage()}") else Unit