package spatialcrafting.util

import spatialcrafting.recipe.ComponentPosition
import java.lang.management.ManagementFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.sqrt


infix fun IntRange.by(range: IntRange): List<Pair<Int, Int>> = this.flatMap { x -> range.map { y -> Pair(x, y) } }

operator fun Pair<Int, Int>.rangeTo(that: Pair<Int, Int>) = object : Iterable<Pair<Int, Int>> {
    override fun iterator() = object : Iterator<Pair<Int, Int>> {
        var i = this@rangeTo.first
        var j = this@rangeTo.second

        val m = that.first - i
        val n = that.second - j

        override fun hasNext() = i <= m && j <= n

        override fun next(): Pair<Int, Int> {
            val res = i to j

            if (j == n) {
                j = 0
                i++
            }
            else {
                j++
            }

            return res
        }
    }
}


fun Double.isWholeNumber() = this.toInt().toDouble() == this

fun max3(num1: Int, num2: Int, num3: Int): Int = max(max(num1, num2), num3)

infix fun Pair<Int, Int>.until(that: Pair<Int, Int>) = this..Pair(that.first - 1, that.second - 1)


/**
 * Appends all elements yielded from results of [transform] function being invoked on each element of original collection, to the given [destination].
 */
inline fun <T, R> Iterable<T>.flatMapIndexed(transform: (Int, T) -> Iterable<R>): List<R> {
    val destination = ArrayList<R>()
    var index = 0
    for (item in this)
        destination.addAll(transform(index++, item))

    return destination
}

/**
 * Appends all elements yielded from results of [transform] function being invoked on each element of original collection, to the given [destination].
 */
inline fun <T, R> Iterable<T>.mapIndexed(transform: (Int, T) -> R): List<R> {
    val destination = ArrayList<R>()
    var index = 0
    for (item in this)
        destination.add(transform(index++, item))

    return destination
}



inline fun Int.squared() = this * this
inline fun Double.squared() = this * this


val Int.d get() = this.toDouble()
val Long.d get() = this.toDouble()
val Int.f get() = this.toFloat()
val Float.d get() = this.toDouble()
val Int.l get() = this.toLong()

inline fun <T, R : Comparable<R>> Iterable<T>.maxValueBy(selector: (T) -> R): R? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    val maxElem = iterator.next()
    if (!iterator.hasNext()) return selector(maxElem)
    var maxValue = selector(maxElem)
    do {
        val e = iterator.next()
        val v = selector(e)
        if (maxValue < v) {
            maxValue = v
        }
    } while (iterator.hasNext())
    return maxValue
}

 val LogDebug = ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0


const val LogInfo = true
const val LogWarning = true

inline fun logDebug(lazyMessage: () -> String) = if (LogDebug) println("${Date()} [SC/DEBUG]: ${lazyMessage()}") else Unit
inline fun logInfo(lazyMessage: () -> String) = if (LogInfo) println("${Date()} [SC/INFO]: ${lazyMessage()}") else Unit
inline fun logWarning(lazyMessage: () -> String) = if (LogWarning) println("${Date()} [SC/WARN]: ${lazyMessage()}") else Unit

val assertionsEnabled = ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0

inline fun assert(message: String = "Assertion failure", test: () -> Boolean) {
    if (assertionsEnabled && !test()) throw AssertionError(message)
}

operator fun String.times(num: Int) = (1..num).joinToString("") { this }