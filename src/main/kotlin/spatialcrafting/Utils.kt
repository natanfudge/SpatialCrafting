package spatialcrafting

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
            } else {
                j++
            }

            return res
        }
    }

}

infix fun Pair<Int, Int>.until(that: Pair<Int, Int>) = this..Pair(that.first - 1, that.second - 1)
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