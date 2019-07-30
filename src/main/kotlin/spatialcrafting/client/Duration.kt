package spatialcrafting.client

import spatialcrafting.util.f
import kotlin.math.roundToInt

val Float.seconds get() = Duration.seconds(this)
val Int.seconds get() = Duration.seconds(this.f)
val Int.ticks get() = Duration.ticks(this)


@Suppress("DataClassPrivateConstructor")
inline class Duration  ( val inTicks: Int) {
    companion object {
        private const val TICKS_PER_SECOND = 20
        fun ticks(ticks: Int) = Duration(ticks)
        fun seconds(seconds: Float) = Duration((seconds * TICKS_PER_SECOND).roundToInt())
    }

    operator fun plus(other: Duration) = Duration(this.inTicks + other.inTicks)
    operator fun minus(other:Duration) = Duration(this.inTicks - other.inTicks)

    operator fun compareTo(other: Duration) = this.inTicks - other.inTicks
}