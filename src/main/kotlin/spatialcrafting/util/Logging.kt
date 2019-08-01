package spatialcrafting.util

import java.util.*

//TODO: turn this off in production
const val LogDebug = true

inline fun logDebug(lazyMessage: () -> String) = if (LogDebug) println("${Date()} [SC/DEBUG]: ${lazyMessage()}") else Unit