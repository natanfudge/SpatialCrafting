package spatialcrafting.util

import java.util.*

//TODO: turn this off in production
const val LogDebug = true
const val LogWarning = true

inline fun logDebug(lazyMessage: () -> String) = if (LogDebug) println("${Date()} [SC/DEBUG]: ${lazyMessage()}") else Unit
inline fun logWarning(lazyMessage: () -> String) = if (LogWarning) println("${Date()} [SC/WARN]: ${lazyMessage()}") else Unit