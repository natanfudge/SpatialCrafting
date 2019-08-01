@file:Suppress("NOTHING_TO_INLINE")

package spatialcrafting.client

import spatialcrafting.util.d

inline val Int.bpt get() = Speed(this.d)
inline val Double.bpt get() = Speed(this)
inline val Int.bps get() = Speed(this.d / Duration.TicksPerSecond)
inline val Double.bps get() = Speed(this / Duration.TicksPerSecond)


inline class Speed(val blocksPerTick: Double) {
    inline operator fun plus(otherSpeed: Speed) = Speed(this.blocksPerTick + otherSpeed.blocksPerTick)
}