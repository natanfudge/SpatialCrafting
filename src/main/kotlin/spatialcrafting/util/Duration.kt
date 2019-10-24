@file:Suppress("NOTHING_TO_INLINE")

package spatialcrafting.util

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.TickScheduler
import net.minecraft.world.World
import spatialcrafting.client.Speed
import kotlin.math.roundToLong

inline val World.durationTime get() = time.ticks


fun CompoundTag.putDuration(key: String, duration: Duration) = putLong(key, duration.inTicks)
fun CompoundTag.getDuration(key: String) = getLong(key).ticks



inline val Float.seconds get() = Duration.seconds(this)
inline val Double.seconds get() = Duration.seconds(this.toFloat())
inline val Int.seconds get() = Duration.seconds(this.f)
inline val Int.ticks get() = Duration.ticks(this)
inline val Long.seconds get() = Duration.seconds(this.d)
inline val Long.ticks get() = Duration.ticks(this)

inline fun distancePassedIn(calc: () -> Double) = calc()


inline class Duration(val inTicks: Long) {
    companion object {
        const val TicksPerSecond = 20
        private const val MillisecondsPerSecond = 1000
        const val MillisecondsPerTick = MillisecondsPerSecond / TicksPerSecond
        inline fun ticks(ticks: Int) = Duration(ticks.toLong())
        inline fun ticks(ticks: Long) = Duration(ticks)
        inline fun seconds(seconds: Float) = Duration((seconds * TicksPerSecond).roundToLong())
        inline fun seconds(seconds: Double) = Duration((seconds * TicksPerSecond).roundToLong())

    }

    inline infix fun with(speed: Speed): Double = this.inTicks * speed.blocksPerTick


    inline val inSeconds: Double
        get() = inTicks.d / TicksPerSecond

    inline val inMilliseconds: Long
        get() = inTicks * MillisecondsPerTick.toLong()

    inline operator fun plus(other: Duration) = Duration(this.inTicks + other.inTicks)
    inline operator fun minus(other: Duration) = Duration(this.inTicks - other.inTicks)

    inline operator fun compareTo(other: Duration) = (this.inTicks - other.inTicks).toInt()
}

