package spatialcrafting.util

import spatialcrafting.recipe.ComponentPosition
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.sqrt


infix fun IntRange.by(range: IntRange): List<Pair<Int, Int>> = this.flatMap { x -> range.map { y -> Pair(x, y) } }
fun IntRange.squared() = this by this

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

fun max(num1: Int, num2: Int, num3: Int): Int = max(max(num1, num2), num3)

infix fun Pair<Int, Int>.until(that: Pair<Int, Int>) = this..Pair(that.first - 1, that.second - 1)
data class Point(val x: Int, val y: Int, val z: Int)

fun cubeSized(size: Int): List<Point> {
    return (0 until size).flatMap { x -> (0 until size).flatMap { y -> (0 until size).map { z -> Point(x, y, z) } } }
}

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

//TODO: turn this on in debug
const val assertionsEnabled = false

inline fun assert(message: String = "Assertion failure", test: () -> Boolean) {
    if (assertionsEnabled && !test()) throw AssertionError(message)
}

inline fun Int.squared() = this * this
inline fun Double.squared() = this * this
inline fun sqrt(num: Int): Double = sqrt(num.toDouble())

//inline fun<T> min(comparable: Comparable<T>)

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

//TODO: turn this on in debug
const val LogDebug = false
const val LogWarning = false

inline fun logDebug(lazyMessage: () -> String) = if (LogDebug) println("${Date()} [SC/DEBUG]: ${lazyMessage()}") else Unit
inline fun logWarning(lazyMessage: () -> String) = if (LogWarning) println("${Date()} [SC/WARN]: ${lazyMessage()}") else Unit


fun allIndicesInCubeOfSize(size: Int) = (0 until size)
        .flatMap { x -> (0 until size).map { y -> x to y } }
        .flatMap { (x, y) -> (0 until size).map { z -> Triple(x, y, z) } }

fun x() {
    val oneRange = 0 until 5
    val listOfRanges = oneRange.map { 0 until 5 }
    val listOfListOfRanges = listOfRanges.map { range ->
        range.map { 0 until 5 }
    }

    val x = listOfListOfRanges
}

fun cubeOfSize(size: Int) = (0 until size)
        .map { 0 until size }
        .map { it.map { 0 until size } }

fun List<List<IntRange>>.mapCube(mapping: (ComponentPosition) -> Char) = mapIndexed { y, layer ->
    layer.mapIndexed { x, row ->
        row.map { z -> mapping(ComponentPosition(x, y, z)) }.joinToString("")
    }
}

inline fun <reified T, reified V> T.getPrivateField(name: String): V {
    val f = T::class.java.getDeclaredField(name)
    f.isAccessible = true
    return f.get(this) as V
}


operator fun String.times(num: Int) = (1..num).joinToString("") { this }